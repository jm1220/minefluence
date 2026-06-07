package net.jeongmin.modid.ui;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.ending.MineFluenceEndingManager;
import net.jeongmin.modid.mission.FarmerMissions;
import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.weapon.MineFluenceWeaponManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class MineFluenceDisplay {
	public static final String PREFIX = "[MineFluence] ";

	private MineFluenceDisplay() {
	}

	public static void sendChat(ServerCommandSource source, String message) {
		source.sendFeedback(() -> Text.literal(PREFIX + message), false);
	}

	public static void sendChat(ServerPlayerEntity player, String message) {
		player.sendMessage(Text.literal(PREFIX + message), false);
	}

	public static void sendActionBar(ServerPlayerEntity player, String message) {
		player.sendMessage(Text.literal(message), true);
	}

	public static void sendPublicStatus(ServerCommandSource source, MineFluencePlayerData data) {
		sendChat(source, publicStatus(data));
	}

	public static void sendStatusActionBar(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (data.hasActiveInvasion()) {
			sendActionBar(player, "[MineFluence] Invasion " + data.getActiveInvasionIndex() + ": Invaders tracked " + data.getTrackedInvasionMobCount());
			MineFluenceHud.refresh(player, data);
			return;
		}

		sendActionBar(player, "Followers: " + data.getFollower()
				+ " | Social: " + data.getSocialCredibility()
				+ " | Missions: " + data.getCompletedMissionCount() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS
				+ " | Weapon: " + MineFluenceWeaponManager.determineTier(data.getFollower())
				+ " | " + shortMissionState(data)
				+ " | " + MineFluenceMissionProgressManager.publicProgressText(data));
		MineFluenceHud.refresh(player, data);
	}

	public static String publicStatus(MineFluencePlayerData data) {
		return "Status: Follower=" + data.getFollower()
				+ ", Social Credibility=" + data.getSocialCredibility()
				+ ", Missions=" + data.getCompletedMissionCount() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS
				+ ", Job=" + data.getSelectedJob()
				+ ", Weapon Tier=" + MineFluenceWeaponManager.determineTier(data.getFollower())
				+ ", " + missionState(data)
				+ ", Progress=" + MineFluenceMissionProgressManager.publicProgressText(data)
				+ ", " + invasionState(data)
				+ endingState(data);
	}

	public static String debugStats(MineFluencePlayerData data) {
		return "Stats: Follower=" + data.getFollower()
				+ ", Social Credibility=" + data.getSocialCredibility()
				+ ", Missions=" + data.getCompletedMissionCount()
				+ ", Job=" + data.getSelectedJob()
				+ ", Last Invasion=" + data.getLastCompletedInvasionIndex()
				+ ", Pending Mission Selection=" + data.getPendingMissionSelectionIndex()
				+ ", Active Mission=" + data.getActiveMissionIndex()
				+ ", Active Mission Route=" + data.getActiveMissionRoute()
				+ ", Pending Posting Mission=" + data.getPendingPostingMissionIndex()
				+ ", Pending Posting Route=" + data.getPendingPostingMissionRoute()
				+ ", Waiting For Posting=" + data.isWaitingForPostingChoice()
				+ ", Active Mission Progress=" + data.getActiveMissionProgress()
				+ ", Mission Baseline=" + data.getMissionBaselineValue()
				+ ", Active Invasion=" + data.getActiveInvasionIndex()
				+ ", Last Completed Invasion=" + data.getLastCompletedInvasionIndex()
				+ ", Tracked Invasion Mobs=" + data.getTrackedInvasionMobCount()
				+ ", Invasion Started Tick=" + data.getInvasionStartedAtTick()
				+ ", Stored Weapon Tier=" + data.getCurrentWeaponTier()
				+ ", Current Weapon Tier=" + MineFluenceWeaponManager.determineTier(data.getFollower())
				+ ", Ending Triggered=" + data.isEndingTriggered()
				+ ", Ending Id=" + data.getEndingId()
				+ ", Calculated Follower Ending Tier=" + MineFluenceEndingManager.calculateFollowerTier(data.getFollower())
				+ ", Calculated Social Ending Tier=" + MineFluenceEndingManager.calculateSocialTier(data.getSocialCredibility())
				+ ", Lie Value(debug hidden stat)=" + data.getLieValue();
	}

	private static String shortMissionState(MineFluencePlayerData data) {
		if (data.hasPendingMissionSelection()) {
			return "Choose mission " + data.getPendingMissionSelectionIndex();
		}
		if (data.isWaitingForPostingChoice()) {
			return "Post mission " + data.getPendingPostingMissionIndex() + " " + data.getPendingPostingMissionRoute();
		}
		if (data.hasActiveMission()) {
			return "Mission " + data.getActiveMissionIndex() + " " + data.getActiveMissionRoute();
		}
		return "Job: " + data.getSelectedJob();
	}

	private static String missionState(MineFluencePlayerData data) {
		if (data.hasPendingMissionSelection()) {
			return "Mission Choice=" + data.getPendingMissionSelectionIndex() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " waiting for Good/Bad selection";
		}
		if (data.isWaitingForPostingChoice()) {
			return "Ready to Post=" + data.getPendingPostingMissionIndex() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " " + data.getPendingPostingMissionRoute() + " - " + missionTitle(data.getPendingPostingMissionIndex(), data.getPendingPostingMissionRoute());
		}
		if (data.hasActiveMission()) {
			return "Mission=" + data.getActiveMissionIndex() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " " + data.getActiveMissionRoute() + " - " + missionTitle(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		}
		return "Active Mission=None";
	}

	private static String missionTitle(int missionIndex, MineFluenceMissionRoute route) {
		return FarmerMissions.getMission(missionIndex, route)
				.map(mission -> mission.title())
				.orElse("Mission " + missionIndex);
	}

	private static String invasionState(MineFluencePlayerData data) {
		if (data.hasActiveInvasion()) {
			return "Active Invasion=" + data.getActiveInvasionIndex();
		}
		return "Active Invasion=None";
	}

	private static String endingState(MineFluencePlayerData data) {
		if (!data.isEndingTriggered()) {
			return "";
		}
		return ", Ending=" + MineFluenceEndingManager.endingDisplayName(data);
	}
}
