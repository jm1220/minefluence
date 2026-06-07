package net.jeongmin.modid.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jeongmin.modid.client.MineFluenceHudState;
import net.jeongmin.modid.client.MineFluenceMissionScreen;
import net.jeongmin.modid.client.MineFluenceMissionClientState;
import net.jeongmin.modid.client.MineFluencePhoneInfoScreen;
import net.jeongmin.modid.client.MineFluenceSmartphoneHomeScreen;
import net.jeongmin.modid.client.MineFluenceTutorialScreen;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.minecraft.client.MinecraftClient;

public final class MineFluenceClientNetworking {
	private MineFluenceClientNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(MineFluenceMissionBoardResponsePayload.ID, (payload, context) ->
				MineFluenceMissionClientState.receiveBoard(payload, context.client())
		);
		ClientPlayNetworking.registerGlobalReceiver(MineFluencePhoneStateResponsePayload.ID, (payload, context) ->
				openPhoneState(payload, context.client())
		);
		ClientPlayNetworking.registerGlobalReceiver(MineFluenceHudStatePayload.ID, (payload, context) ->
				MineFluenceHudState.update(payload)
		);
		ClientPlayNetworking.registerGlobalReceiver(MineFluenceTutorialOpenPayload.ID, (payload, context) ->
				context.client().setScreen(new MineFluenceTutorialScreen())
		);
	}

	public static boolean requestMissionBoard() {
		if (!ClientPlayNetworking.canSend(MineFluenceMissionBoardRequestPayload.ID)) {
			return false;
		}

		ClientPlayNetworking.send(new MineFluenceMissionBoardRequestPayload());
		return true;
	}

	public static boolean chooseMission(MineFluenceMissionRoute route) {
		if (!ClientPlayNetworking.canSend(MineFluenceMissionChoosePayload.ID)) {
			return false;
		}

		ClientPlayNetworking.send(new MineFluenceMissionChoosePayload(route));
		return true;
	}

	public static boolean requestPhoneState() {
		if (!ClientPlayNetworking.canSend(MineFluencePhoneStateRequestPayload.ID)) {
			return false;
		}

		ClientPlayNetworking.send(new MineFluencePhoneStateRequestPayload());
		return true;
	}

	public static boolean sendPhoneAction(MineFluencePhoneAction action) {
		if (!ClientPlayNetworking.canSend(MineFluencePhoneActionPayload.ID)) {
			return false;
		}

		ClientPlayNetworking.send(new MineFluencePhoneActionPayload(action));
		return true;
	}

	public static boolean postMission(boolean exaggerated) {
		if (!ClientPlayNetworking.canSend(MineFluencePostingChoicePayload.ID)) {
			return false;
		}

		ClientPlayNetworking.send(new MineFluencePostingChoicePayload(exaggerated));
		return true;
	}

	public static boolean finishTutorial() {
		if (!ClientPlayNetworking.canSend(MineFluenceTutorialPlayPayload.ID)) {
			return false;
		}

		ClientPlayNetworking.send(new MineFluenceTutorialPlayPayload());
		return true;
	}

	private static void openPhoneState(MineFluencePhoneStateResponsePayload payload, MinecraftClient client) {
		client.setScreen(new MineFluenceSmartphoneHomeScreen(payload));
	}
}
