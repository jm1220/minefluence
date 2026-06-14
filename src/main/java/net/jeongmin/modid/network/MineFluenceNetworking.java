package net.jeongmin.modid.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeongmin.modid.area.MineFluenceAreaGuideManager;
import net.jeongmin.modid.area.MineFluenceAreaType;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.core.MineFluenceDemoFlow;
import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ending.MineFluenceEnding;
import net.jeongmin.modid.ending.MineFluenceEndingManager;
import net.jeongmin.modid.ending.MineFluenceEndingRegistry;
import net.jeongmin.modid.ending.MineFluenceEndingVideoLauncher;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.jeongmin.modid.invasion.MineFluenceInvasionSupportManager;
import net.jeongmin.modid.mission.MineFluenceMission;
import net.jeongmin.modid.mission.MineFluenceMissionBoardState;
import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.mission.MineFluenceMissionSelectionService;
import net.jeongmin.modid.mission.MineFluencePostingService;
import net.jeongmin.modid.mission.MineFluencePostingService.RewardPreview;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.weapon.MineFluenceWeaponManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluenceNetworking {
	private MineFluenceNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(MineFluenceMissionBoardRequestPayload.ID, MineFluenceMissionBoardRequestPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MineFluenceMissionChoosePayload.ID, MineFluenceMissionChoosePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MineFluencePhoneActionPayload.ID, MineFluencePhoneActionPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MineFluencePhoneStateRequestPayload.ID, MineFluencePhoneStateRequestPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MineFluencePostingChoicePayload.ID, MineFluencePostingChoicePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MineFluenceTutorialPlayPayload.ID, MineFluenceTutorialPlayPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MineFluenceHudStatePayload.ID, MineFluenceHudStatePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MineFluenceMissionBoardResponsePayload.ID, MineFluenceMissionBoardResponsePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MineFluencePhoneStateResponsePayload.ID, MineFluencePhoneStateResponsePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MineFluenceTutorialOpenPayload.ID, MineFluenceTutorialOpenPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(MineFluenceMissionBoardRequestPayload.ID, (payload, context) -> sendMissionBoard(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(MineFluenceMissionChoosePayload.ID, (payload, context) -> {
			MineFluenceMissionRoute route = MineFluenceMissionRoute.fromSerializedName(payload.route());
			MineFluenceMissionSelectionService.chooseMission(context.player(), route, false);
			sendMissionBoard(context.player());
			sendPhoneState(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(MineFluencePhoneStateRequestPayload.ID, (payload, context) -> sendPhoneState(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(MineFluencePhoneActionPayload.ID, (payload, context) -> {
			if (handlePhoneAction(context.player(), MineFluencePhoneAction.fromSerializedName(payload.action()))) {
				sendPhoneState(context.player());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(MineFluencePostingChoicePayload.ID, (payload, context) -> {
			MineFluencePostingService.postMission(context.player(), payload.exaggerated());
			sendPhoneState(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(MineFluenceTutorialPlayPayload.ID, (payload, context) -> {
			finishTutorial(context.player());
			sendPhoneState(context.player());
		});
	}

	public static void sendMissionBoard(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, MineFluenceMissionBoardResponsePayload.ID)) {
			return;
		}

		MineFluenceMissionBoardState state = MineFluenceMissionSelectionService.boardState(player);
		ServerPlayNetworking.send(player, new MineFluenceMissionBoardResponsePayload(state));
	}

	public static void sendPhoneState(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, MineFluencePhoneStateResponsePayload.ID)) {
			return;
		}

		ServerPlayNetworking.send(player, phoneState(player));
	}

	public static boolean openTutorial(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, MineFluenceTutorialOpenPayload.ID)) {
			return false;
		}

		ServerPlayNetworking.send(player, new MineFluenceTutorialOpenPayload());
		return true;
	}

	private static MineFluencePhoneStateResponsePayload phoneState(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		if (data.isExposureTriggered()) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_EXPOSED, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Your lies were exposed.");
		}
		if (data.isEndingTriggered()) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_ENDING, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Ending reached: " + MineFluenceEndingManager.endingDisplayName(data) + ".");
		}
		if (data.hasActiveInvasion()) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_INVASION, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Defend the village from invaders.");
		}
		if (data.isWaitingForPostingChoice()) {
			MineFluenceMission mission = MineFluencePostingService.missionOrFallback(
					data.getPendingPostingMissionIndex(),
					data.getPendingPostingMissionRoute()
			);
			RewardPreview normal = MineFluencePostingService.rewardPreview(mission, false);
			RewardPreview exaggerated = MineFluencePostingService.rewardPreview(mission, true);
			return phoneStatePayload(
					player,
					data,
					MineFluencePhoneStateResponsePayload.STATE_READY_TO_UPLOAD,
					mission.index(),
					mission.route().serializedName(),
					mission.title(),
					mission.objectiveText(),
					mission.targetProgress(),
					mission.targetProgress(),
					normal.followerReward(),
					normal.socialCredibilityReward(),
					normal.lieValueIncrease(),
					exaggerated.followerReward(),
					exaggerated.socialCredibilityReward(),
					exaggerated.lieValueIncrease(),
					"Mission complete. Open Upload Screen to choose a posting type."
			);
		}

		if (data.hasPendingMissionSelection()) {
			return phoneStatePayload(
					player,
					data,
					MineFluencePhoneStateResponsePayload.STATE_MISSION_CHOICE,
					data.getPendingMissionSelectionIndex(),
					"",
					"",
					"",
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					"Choose a route for mission " + data.getPendingMissionSelectionIndex() + "."
			);
		}

		if (data.hasActiveMission()) {
			MineFluenceMission mission = MineFluencePostingService.missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute());
			return phoneStatePayload(
					player,
					data,
					MineFluencePhoneStateResponsePayload.STATE_MISSION_ACTIVE,
					mission.index(),
					mission.route().serializedName(),
					mission.title(),
					mission.objectiveText(),
					data.getActiveMissionProgress(),
					mission.targetProgress(),
					0,
					0,
					0,
					0,
					0,
					0,
					MineFluenceMissionProgressManager.progressText(player, data)
			);
		}

		if (!data.isDemoStarted()) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_NOT_STARTED, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Play the tutorial to begin MineFluence.");
		}
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_CHOOSE_JOB, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Choose Farmer to continue.");
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_READY, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "All Farmer demo missions are complete.");
		}
		return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_READY, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Start the next mission.");
	}

	private static boolean handlePhoneAction(ServerPlayerEntity player, MineFluencePhoneAction action) {
		if (action == null) {
			MineFluenceDisplay.sendChat(player, "Unknown smartphone action.");
			return true;
		}

		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		switch (action) {
			case START_DEMO -> MineFluenceDemoFlow.startDemo(player, false);
			case OPEN_TUTORIAL -> {
				if (openTutorial(player)) {
					return false;
				}
				MineFluenceDisplay.sendChat(player, "Tutorial screen channel is not available.");
			}
			case CHOOSE_FARMER -> {
				if (!data.isDemoStarted()) {
					MineFluenceDisplay.sendChat(player, "Start the demo before choosing Farmer.");
				} else if (data.getSelectedJob() == MineFluenceJob.FARMER) {
					MineFluenceDisplay.sendChat(player, "Farmer is already selected.");
				} else {
					MineFluenceDemoFlow.chooseFarmer(player);
				}
			}
			case START_NEXT_MISSION -> {
				if (data.hasActiveInvasion()) {
					MineFluenceDisplay.sendChat(player, "Clear the active invasion before starting another mission.");
				} else if (data.isEndingTriggered()) {
					MineFluenceDisplay.sendChat(player, "Restart the demo to begin new missions.");
				} else {
					MineFluenceMissionSelectionService.prepareNextMission(player);
				}
			}
			case CHOOSE_GOOD -> chooseMissionFromPhone(player, data, MineFluenceMissionRoute.GOOD);
			case CHOOSE_BAD -> chooseMissionFromPhone(player, data, MineFluenceMissionRoute.BAD);
			case POST_NORMAL -> postMissionFromPhone(player, data, false);
			case POST_EXAGGERATE -> postMissionFromPhone(player, data, true);
			case SHOW_MISSION_AREA -> showActiveMissionArea(player);
			case SHOW_INVASION_STATUS -> showInvasionStatus(player, data);
			case PLAY_ENDING_VIDEO -> playEndingVideo(player);
			case RESTART_DEMO -> {
				if (!data.isEndingTriggered()) {
					MineFluenceDisplay.sendChat(player, "Restart Demo is available after an ending.");
				} else {
					MineFluenceDemoFlow.startDemo(player, false);
				}
			}
		}
		return true;
	}

	private static void chooseMissionFromPhone(ServerPlayerEntity player, MineFluencePlayerData data, MineFluenceMissionRoute route) {
		if (!data.hasPendingMissionSelection()) {
			MineFluenceDisplay.sendChat(player, "No mission is waiting for a Good/Bad choice.");
			return;
		}
		MineFluenceMissionSelectionService.chooseMission(player, route, false);
	}

	private static void postMissionFromPhone(ServerPlayerEntity player, MineFluencePlayerData data, boolean exaggerated) {
		if (!data.isWaitingForPostingChoice()) {
			MineFluenceDisplay.sendChat(player, "No completed mission is waiting to be uploaded.");
			return;
		}
		MineFluencePostingService.postMission(player, exaggerated);
	}

	private static void finishTutorial(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		if (!data.isDemoStarted()) {
			MineFluenceDemoFlow.startDemo(player, false);
			MineFluenceDemoFlow.chooseFarmer(player);
			MineFluenceDisplay.sendChat(player, "Tutorial complete. Farmer is ready.");
			return;
		}
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			MineFluenceDemoFlow.chooseFarmer(player);
			return;
		}
		MineFluenceDisplay.sendChat(player, "Tutorial complete.");
	}

	private static void showActiveMissionArea(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		if (data.hasPendingMissionSelection()) {
			MineFluenceDisplay.sendChat(player, "Choose a mission first.");
			return;
		}
		if (!data.hasActiveMission()) {
			MineFluenceDisplay.sendChat(player, "No active mission area to show.");
			return;
		}

		MineFluenceAreaType areaType = MineFluenceAreaGuideManager.requiredAreaForMission(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (areaType == null) {
			MineFluenceDisplay.sendChat(player, "This mission does not require a specific area.");
			return;
		}
		MineFluenceAreaGuideManager.showMissionArea(player, areaType, data.getActiveMissionRoute());
	}

	private static void showInvasionStatus(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (!data.hasActiveInvasion()) {
			MineFluenceDisplay.sendChat(player, "No invasion is active.");
			return;
		}

		int remaining = MineFluenceInvasionManager.countRemainingTrackedMobs(player.getServer(), data);
		int supportAllies = MineFluenceInvasionSupportManager.countSupportAllies(player);
		MineFluenceDisplay.sendChat(player, "Invasion " + data.getActiveInvasionIndex()
				+ ": " + remaining + " enemies remaining, " + supportAllies + " support allies.");
	}

	private static void playEndingVideo(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		if (!data.isEndingTriggered()) {
			MineFluenceDisplay.sendChat(player, "No ending video is available yet.");
			return;
		}

		MineFluenceEnding ending = MineFluenceEndingRegistry.getById(data.getEndingId()).orElseGet(() -> MineFluenceEndingManager.getEnding(data));
		if (!MineFluenceEndingManager.isTheFamousVillainEnding(ending)) {
			MineFluenceDisplay.sendChat(player, "Ending video could not be opened.");
			return;
		}
		if (!MineFluenceEndingVideoLauncher.launchTheFamousVillain(player)) {
			MineFluenceDisplay.sendChat(player, "Ending video could not be opened.");
		}
	}

	private static MineFluencePhoneStateResponsePayload phoneStatePayload(
			ServerPlayerEntity player,
			MineFluencePlayerData data,
			String state,
			int missionIndex,
			String route,
			String title,
			String objectiveText,
			int currentProgress,
			int targetProgress,
			int normalFollowerReward,
			int normalSocialCredibilityReward,
			int normalLieValueIncrease,
			int exaggeratedFollowerReward,
			int exaggeratedSocialCredibilityReward,
			int exaggeratedLieValueIncrease,
			String message
	) {
		int invasionRemaining = 0;
		int invasionTotal = 0;
		if (data.hasActiveInvasion()) {
			invasionRemaining = MineFluenceInvasionManager.countRemainingTrackedMobs(player.getServer(), data);
			invasionTotal = Math.max(data.getActiveInvasionTotal(), invasionRemaining);
		}

		int supportAllyCount = data.hasActiveInvasion()
				? MineFluenceInvasionSupportManager.countSupportAllies(player)
				: 0;
		MineFluenceAreaType requiredArea = data.hasActiveMission()
				? MineFluenceAreaGuideManager.requiredAreaForMission(data.getActiveMissionIndex(), data.getActiveMissionRoute())
				: null;
		MineFluenceEnding ending = data.isEndingTriggered()
				? MineFluenceEndingRegistry.getById(data.getEndingId()).orElseGet(() -> MineFluenceEndingManager.getEnding(data))
				: null;

		return new MineFluencePhoneStateResponsePayload(
				state,
				missionIndex,
				route,
				title,
				objectiveText,
				currentProgress,
				targetProgress,
				normalFollowerReward,
				normalSocialCredibilityReward,
				normalLieValueIncrease,
				exaggeratedFollowerReward,
				exaggeratedSocialCredibilityReward,
				exaggeratedLieValueIncrease,
				message,
				data.getFollower(),
				data.getSocialCredibility(),
				data.getLieValue(),
				data.getCompletedMissionCount(),
				data.getSelectedJob().serializedName(),
				data.getPendingMissionSelectionIndex(),
				data.getActiveInvasionIndex(),
				invasionRemaining,
				invasionTotal,
				data.isEndingTriggered(),
				data.isExposureTriggered(),
				data.isEndingTriggered() ? MineFluenceEndingManager.endingDisplayName(data) : "",
				ending != null && MineFluenceEndingManager.isTheFamousVillainEnding(ending),
				MineFluenceWeaponManager.determineTier(data.getFollower()).displayName(),
				supportAllyCount,
				requiredArea == null ? "" : requiredArea.displayName()
		);
	}
}
