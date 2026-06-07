package net.jeongmin.modid.client;

import net.jeongmin.modid.network.MineFluenceMissionBoardResponsePayload;
import net.minecraft.client.MinecraftClient;

public final class MineFluenceMissionClientState {
	private static MineFluenceMissionBoardResponsePayload latestBoard;

	private MineFluenceMissionClientState() {
	}

	public static MineFluenceMissionBoardResponsePayload latestBoard() {
		return latestBoard;
	}

	public static void clearBoard() {
		latestBoard = null;
	}

	public static void receiveBoard(MineFluenceMissionBoardResponsePayload payload, MinecraftClient client) {
		latestBoard = payload;
		if (client.currentScreen instanceof MineFluenceMissionScreen screen) {
			screen.updateBoard(payload);
		}
	}
}
