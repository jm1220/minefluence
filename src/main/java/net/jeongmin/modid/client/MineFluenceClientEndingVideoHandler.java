package net.jeongmin.modid.client;

import net.minecraft.client.MinecraftClient;

public final class MineFluenceClientEndingVideoHandler {
	private MineFluenceClientEndingVideoHandler() {
	}

	public static void playTheFamousVillain(MinecraftClient client) {
		if (client == null) {
			return;
		}
		client.setScreen(EndingVideoScreen.theFamousVillain());
	}
}
