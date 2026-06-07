package net.jeongmin.modid.client;

import net.jeongmin.modid.network.MineFluenceHudStatePayload;

public final class MineFluenceHudState {
	private static MineFluenceHudStatePayload snapshot;

	private MineFluenceHudState() {
	}

	public static void update(MineFluenceHudStatePayload payload) {
		snapshot = payload;
	}

	public static MineFluenceHudStatePayload snapshot() {
		return snapshot;
	}

	public static void clear() {
		snapshot = null;
	}
}
