package net.jeongmin.modid.mission;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.billboard.MineFluenceBillboardImageResolver;
import net.jeongmin.modid.billboard.MineFluenceBillboards;
import net.jeongmin.modid.billboard.PostingStyle;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ending.MineFluenceEndingManager;
import net.jeongmin.modid.fan.MineFluenceFanVillagers;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.weapon.MineFluenceWeaponManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluencePostingService {
	private MineFluencePostingService() {
	}

	public static boolean postMission(ServerPlayerEntity player, boolean exaggerated) {
		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		if (!data.isWaitingForPostingChoice()) {
			MineFluenceDisplay.sendChat(player, "No completed mission is waiting for a posting choice.");
			MineFluenceDisplay.sendActionBar(player, "No pending post");
			return false;
		}

		MineFluenceMission mission = missionOrFallback(data.getPendingPostingMissionIndex(), data.getPendingPostingMissionRoute());
		RewardPreview preview = rewardPreview(mission, exaggerated);

		int beforeFollower = data.getFollower();
		int beforeSocial = data.getSocialCredibility();

		MineFluencePlayerData updatedData = state.updatePlayerData(player, playerData -> {
			playerData.addFollower(preview.followerReward());
			playerData.addSocialCredibility(preview.socialCredibilityReward());
			if (preview.lieValueIncrease() > 0) {
				playerData.addLieValue(preview.lieValueIncrease());
			}
			playerData.addCompletedMissionCount(1);
			playerData.clearMissionFlow();
		});

		int appliedFollower = updatedData.getFollower() - beforeFollower;
		int appliedSocial = updatedData.getSocialCredibility() - beforeSocial;

		MineFluenceDisplay.sendChat(player, exaggerated ? "Posted exaggerated." : "Posted normally.");
		MineFluenceDisplay.sendChat(player, "Follower " + signed(appliedFollower) + ", Social Credibility " + signed(appliedSocial) + ".");
		if (exaggerated && preview.lieValueIncrease() > 0) {
			MineFluenceDisplay.sendChat(player, "Your story spreads further... but something feels risky.");
		}
		MineFluenceDisplay.sendChat(player, "Mission " + updatedData.getCompletedMissionCount() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " completed.");
		MineFluenceWeaponManager.updateWeapon(player, updatedData);
		MineFluenceFanVillagers.syncFanVillagers(player);
		updateUploadedMissionBillboards(player, mission, exaggerated);
		if (exaggerated
				&& preview.lieValueIncrease() > 0
				&& updatedData.getLieValue() >= MineFluenceBalance.LIE_EXPOSURE_THRESHOLD
				&& MineFluenceEndingManager.triggerExposureCollapse(player, updatedData)) {
			return true;
		}
		startInvasionIfDue(player, updatedData);
		MineFluenceDisplay.sendStatusActionBar(player, updatedData);
		return true;
	}

	public static RewardPreview rewardPreview(MineFluenceMission mission, boolean exaggerated) {
		int followerReward = mission.baseFollowerReward();
		int socialReward = mission.baseSocialCredibilityReward();
		int lieReward = 0;

		if (exaggerated) {
			followerReward = (int) Math.round(followerReward * MineFluenceBalance.EXAGGERATED_POST_MULTIPLIER);
			socialReward = (int) Math.round(socialReward * MineFluenceBalance.EXAGGERATED_POST_MULTIPLIER);
			lieReward = MineFluenceBalance.getLieIncreaseForMission(mission.index());
		}

		return new RewardPreview(followerReward, socialReward, lieReward);
	}

	public static MineFluenceMission missionOrFallback(int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceMissionRoute resolvedRoute = route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route;
		return FarmerMissions.getMission(missionIndex, resolvedRoute)
				.orElseGet(() -> new MineFluenceMission(
						"unknown_" + missionIndex,
						missionIndex,
						resolvedRoute,
						"Unknown Mission",
						"No mission definition exists for this index.",
						"Reset or start the next valid Farmer mission.",
						1,
						0,
						0
				));
	}

	private static void startInvasionIfDue(ServerPlayerEntity player, MineFluencePlayerData data) {
		int invasionIndex = MineFluenceBalance.invasionIndexForCompletedMissionCount(data.getCompletedMissionCount());
		if (invasionIndex <= 0) {
			return;
		}

		MineFluenceInvasionManager.startInvasion(player, data, invasionIndex);
	}

	private static void updateUploadedMissionBillboards(ServerPlayerEntity player, MineFluenceMission mission, boolean exaggerated) {
		String imageId = MineFluenceBillboardImageResolver.resolveUploadedMissionImage(
				mission.index(),
				mission.route(),
				PostingStyle.fromExaggerated(exaggerated)
		);
		MineFluenceBillboards.setGroupImage(player.getServer(), MineFluenceBillboards.MAIN_GROUP, imageId);
	}

	private static String signed(int value) {
		if (value >= 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}

	public record RewardPreview(int followerReward, int socialCredibilityReward, int lieValueIncrease) {
	}
}
