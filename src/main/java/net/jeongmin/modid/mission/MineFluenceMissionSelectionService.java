package net.jeongmin.modid.mission;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.area.MineFluenceAreaGuideManager;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluenceMissionSelectionService {
	private MineFluenceMissionSelectionService() {
	}

	public static MineFluenceMissionBoardState boardState(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);

		if (data.isExposureTriggered() || data.isEndingTriggered()) {
			return MineFluenceMissionBoardState.messageOnly(0, "The demo has ended. Restart from the smartphone to choose new missions.");
		}
		if (data.hasActiveInvasion()) {
			return MineFluenceMissionBoardState.messageOnly(0, "Clear the active invasion before choosing another mission.");
		}
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			return MineFluenceMissionBoardState.messageOnly(0, "Start the demo and choose Farmer first.");
		}
		if (data.isWaitingForPostingChoice()) {
			MineFluenceMission mission = missionOrFallback(data.getPendingPostingMissionIndex(), data.getPendingPostingMissionRoute());
			return MineFluenceMissionBoardState.messageOnly(
					mission.index(),
					"Mission " + mission.index() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " "
							+ mission.route() + " is ready to post. Use /minefluence post normal or /minefluence post exaggerate."
			);
		}
		if (data.hasActiveMission()) {
			MineFluenceMission mission = missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute());
			return MineFluenceMissionBoardState.messageOnly(
					mission.index(),
					"Mission " + mission.index() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " "
							+ mission.route() + " is active: " + mission.title() + ". Complete it first."
			);
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return MineFluenceMissionBoardState.messageOnly(MineFluenceBalance.TOTAL_DEMO_MISSIONS, "All demo missions are complete.");
		}

		if (!data.hasPendingMissionSelection()) {
			return MineFluenceMissionBoardState.messageOnly(
					data.getCompletedMissionCount() + 1,
					"Start the next mission from the smartphone before choosing a route."
			);
		}

		int missionIndex = data.getPendingMissionSelectionIndex();
		return MineFluenceMissionBoardState.choices(missionIndex, true, "Choose a route for mission " + missionIndex + ".");
	}

	public static boolean chooseMission(ServerPlayerEntity player, MineFluenceMissionRoute route, boolean allowAutoPrepare) {
		MineFluenceMissionRoute resolvedRoute = route == MineFluenceMissionRoute.BAD ? MineFluenceMissionRoute.BAD : MineFluenceMissionRoute.GOOD;
		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		if (data.isExposureTriggered() || data.isEndingTriggered()) {
			MineFluenceDisplay.sendChat(player, "Restart the demo before choosing another mission.");
			return false;
		}
		if (data.hasActiveInvasion()) {
			MineFluenceDisplay.sendChat(player, "Clear the active invasion before choosing another mission.");
			return false;
		}
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			MineFluenceDisplay.sendChat(player, "Choose Farmer first with /minefluence choose farmer.");
			MineFluenceDisplay.sendActionBar(player, "Choose Farmer first");
			return false;
		}
		if (data.isWaitingForPostingChoice()) {
			showPendingPosting(player, data);
			return false;
		}
		if (data.hasActiveMission()) {
			MineFluenceMission activeMission = missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute());
			MineFluenceDisplay.sendChat(player, "A mission is already active: " + activeMission.route() + " - " + activeMission.title() + ".");
			MineFluenceDisplay.sendActionBar(player, "Complete the active mission first");
			return false;
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			MineFluenceDisplay.sendChat(player, "All Farmer demo missions are complete.");
			MineFluenceDisplay.sendActionBar(player, "All Farmer missions complete");
			return false;
		}
		if (!data.hasPendingMissionSelection() && !allowAutoPrepare) {
			MineFluenceDisplay.sendChat(player, "No mission is waiting for Good/Bad selection. Use /minefluence mission next first.");
			MineFluenceDisplay.sendStatusActionBar(player, data);
			return false;
		}

		int missionIndex = data.hasPendingMissionSelection()
				? data.getPendingMissionSelectionIndex()
				: data.getCompletedMissionCount() + 1;
		MineFluenceMission mission = missionOrFallback(missionIndex, resolvedRoute);
		if (!MineFluenceMissionProgressManager.canStartMission(player, mission)) {
			MineFluenceDisplay.sendChat(player, "Mission choice is still open. You can set the missing area or choose /minefluence mission choose bad.");
			MineFluenceDisplay.sendStatusActionBar(player, data);
			return false;
		}

		int baseline = MineFluenceMissionProgressManager.baselineForMission(player, mission);
		MineFluencePlayerData updatedData = state.updatePlayerData(player, playerData -> {
			playerData.startMission(mission.index(), mission.route());
			playerData.setMissionBaselineValue(baseline);
			playerData.setActiveMissionProgress(0);
		});

		MineFluenceDisplay.sendChat(player, "Mission selected: " + mission.route() + ".");
		showMission(player, mission);
		MineFluenceMissionSupplies.grantForMissionStart(player, updatedData, mission);
		MineFluenceDisplay.sendChat(player, "Progress: " + MineFluenceMissionProgressManager.progressText(player, updatedData));
		MineFluenceDisplay.sendStatusActionBar(player, updatedData);
		MineFluenceAreaGuideManager.sendMissionAreaHint(player, mission.index(), mission.route());
		return true;
	}

	public static boolean prepareNextMission(ServerPlayerEntity player) {
		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			MineFluenceDisplay.sendChat(player, "Choose Farmer first with the smartphone or /minefluence choose farmer.");
			MineFluenceDisplay.sendActionBar(player, "Choose Farmer first");
			return false;
		}
		if (data.isWaitingForPostingChoice()) {
			showPendingPosting(player, data);
			return false;
		}
		if (data.hasActiveMission()) {
			MineFluenceMission activeMission = missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute());
			MineFluenceDisplay.sendChat(player, "A mission is already active: " + activeMission.route() + " - " + activeMission.title() + ".");
			showMission(player, activeMission);
			MineFluenceDisplay.sendStatusActionBar(player, data);
			return false;
		}
		if (data.hasPendingMissionSelection()) {
			showMissionOptions(player, data.getPendingMissionSelectionIndex());
			return false;
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			MineFluenceDisplay.sendChat(player, "All Farmer demo missions are complete.");
			MineFluenceDisplay.sendActionBar(player, "All Farmer missions complete");
			return true;
		}

		int nextMissionIndex = data.getCompletedMissionCount() + 1;
		MineFluencePlayerData updatedData = state.updatePlayerData(player, playerData -> playerData.setPendingMissionSelectionIndex(nextMissionIndex));
		showMissionOptions(player, nextMissionIndex);
		MineFluenceDisplay.sendStatusActionBar(player, updatedData);
		return true;
	}

	private static void showMission(ServerPlayerEntity player, MineFluenceMission mission) {
		MineFluenceDisplay.sendChat(player, "Mission " + mission.index() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " " + mission.route() + ": " + mission.title());
		MineFluenceDisplay.sendChat(player, mission.description());
		MineFluenceDisplay.sendChat(player, "Objective: " + mission.objectiveText());
		MineFluenceDisplay.sendChat(player, "Target: " + mission.targetProgress());
		MineFluenceDisplay.sendChat(player, "Base rewards: Follower " + signed(mission.baseFollowerReward()) + ", Social Credibility " + signed(mission.baseSocialCredibilityReward()) + ".");
		if (mission.route() == MineFluenceMissionRoute.BAD && !MineFluenceMissionProgressManager.hasBadGameplayDetection(mission.index())) {
			MineFluenceDisplay.sendChat(player, "Bad mission detection is not implemented yet. Use /minefluence mission complete_debug for now.");
		}
		MineFluenceDisplay.sendActionBar(player, "Mission " + mission.index() + ": " + mission.title());
	}

	private static void showPendingPosting(ServerPlayerEntity player, MineFluencePlayerData data) {
		MineFluenceMission mission = missionOrFallback(data.getPendingPostingMissionIndex(), data.getPendingPostingMissionRoute());
		MineFluenceDisplay.sendChat(player, "Mission ready to post: " + mission.index() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " " + mission.route() + " - " + mission.title() + ".");
		MineFluenceDisplay.sendChat(player, "Choose /minefluence post normal or /minefluence post exaggerate.");
		MineFluenceDisplay.sendActionBar(player, "Choose a posting style");
	}

	private static void showMissionOptions(ServerPlayerEntity player, int missionIndex) {
		MineFluenceDisplay.sendChat(player, "Mission " + missionIndex + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " route selection:");
		showMissionOption(player, missionOrFallback(missionIndex, MineFluenceMissionRoute.GOOD));
		showMissionOption(player, missionOrFallback(missionIndex, MineFluenceMissionRoute.BAD));
		MineFluenceDisplay.sendChat(player, "Choose a route with the smartphone, mission board, or /minefluence mission choose good/bad.");
		MineFluenceDisplay.sendActionBar(player, "Choose mission " + missionIndex + " route");
	}

	private static void showMissionOption(ServerPlayerEntity player, MineFluenceMission mission) {
		MineFluenceDisplay.sendChat(player, mission.route() + ": " + mission.title()
				+ " | Objective: " + mission.objectiveText()
				+ " | Rewards: Follower " + signed(mission.baseFollowerReward())
				+ ", Social " + signed(mission.baseSocialCredibilityReward()));
	}

	private static MineFluenceMission missionOrFallback(int missionIndex, MineFluenceMissionRoute route) {
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

	private static String signed(int value) {
		if (value >= 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}
}
