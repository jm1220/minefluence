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
			MineFluenceMissionSelectionService.chooseMission(context.player(), route, true);
			sendMissionBoard(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(MineFluencePhoneStateRequestPayload.ID, (payload, context) -> sendPhoneState(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(MineFluencePhoneActionPayload.ID, (payload, context) -> {
			handlePhoneAction(context.player(), MineFluencePhoneAction.fromSerializedName(payload.action()));
			sendPhoneState(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(MineFluencePostingChoicePayload.ID, (payload, context) ->
				MineFluencePostingService.postMission(context.player(), payload.exaggerated())
		);
		ServerPlayNetworking.registerGlobalReceiver(MineFluenceTutorialPlayPayload.ID, (payload, context) ->
				MineFluenceDemoFlow.chooseFarmer(context.player())
		);
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
					MineFluencePhoneStateResponsePayload.STATE_POSTING,
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
					"Mission complete. Choose an upload style."
			);
		}

		if (data.hasPendingMissionSelection()) {
			return phoneStatePayload(
					player,
					data,
					MineFluencePhoneStateResponsePayload.STATE_MISSION_BOARD,
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
					MineFluencePhoneStateResponsePayload.STATE_STATUS,
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

		if (data.hasActiveInvasion()) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_GUIDANCE, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Defend the village from invaders.");
		}
		if (data.isEndingTriggered()) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_GUIDANCE, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Ending reached: " + MineFluenceEndingManager.endingDisplayName(data) + ".");
		}
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_GUIDANCE, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Press Start Demo, then choose Farmer.");
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_GUIDANCE, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "All Farmer demo missions are complete.");
		}
		return phoneStatePayload(player, data, MineFluencePhoneStateResponsePayload.STATE_GUIDANCE, 0, "", "", "", 0, 0, 0, 0, 0, 0, 0, 0, "Start the next mission.");
	}

	private static void handlePhoneAction(ServerPlayerEntity player, MineFluencePhoneAction action) {
		if (action == null) {
			MineFluenceDisplay.sendChat(player, "Unknown smartphone action.");
			return;
		}

		switch (action) {
			case START_DEMO -> MineFluenceDemoFlow.startDemo(player, false);
			case CHOOSE_FARMER -> MineFluenceDemoFlow.chooseFarmer(player);
			case START_NEXT_MISSION -> MineFluenceMissionSelectionService.prepareNextMission(player);
			case CHOOSE_GOOD -> MineFluenceMissionSelectionService.chooseMission(player, MineFluenceMissionRoute.GOOD, false);
			case CHOOSE_BAD -> MineFluenceMissionSelectionService.chooseMission(player, MineFluenceMissionRoute.BAD, false);
			case POST_NORMAL -> MineFluencePostingService.postMission(player, false);
			case POST_EXAGGERATE -> MineFluencePostingService.postMission(player, true);
			case SHOW_MISSION_AREA -> showActiveMissionArea(player);
			case PLAY_ENDING_VIDEO -> playEndingVideo(player);
		}
	}

	private static void showActiveMissionArea(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		if (!data.hasActiveMission()) {
			MineFluenceDisplay.sendChat(player, "No active mission area to show.");
			return;
		}

		MineFluenceAreaType areaType = MineFluenceAreaGuideManager.requiredAreaForMission(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (areaType == null) {
			MineFluenceDisplay.sendChat(player, "This mission does not use a fixed map area.");
			return;
		}
		MineFluenceAreaGuideManager.showArea(player, areaType);
	}

	private static void playEndingVideo(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		if (!data.isEndingTriggered()) {
			MineFluenceDisplay.sendChat(player, "No ending video is available yet.");
			return;
		}

		MineFluenceEnding ending = MineFluenceEndingRegistry.getById(data.getEndingId()).orElseGet(() -> MineFluenceEndingManager.getEnding(data));
		if (!MineFluenceEndingManager.isTheFamousVillainEnding(ending)) {
			MineFluenceDisplay.sendChat(player, "No external ending video is configured for " + ending.displayName() + ".");
			return;
		}
		MineFluenceEndingVideoLauncher.launchTheFamousVillain(player);
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
				data.isEndingTriggered() ? MineFluenceEndingManager.endingDisplayName(data) : "",
				MineFluenceWeaponManager.determineTier(data.getFollower()).displayName()
		);
	}
}
