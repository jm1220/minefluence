package net.jeongmin.modid.network;

import java.util.Locale;

public enum MineFluencePhoneAction {
	START_DEMO,
	OPEN_TUTORIAL,
	CHOOSE_FARMER,
	START_NEXT_MISSION,
	CHOOSE_GOOD,
	CHOOSE_BAD,
	POST_NORMAL,
	POST_EXAGGERATE,
	SHOW_MISSION_AREA,
	SHOW_INVASION_STATUS,
	PLAY_ENDING_VIDEO,
	RESTART_DEMO;

	public String serializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static MineFluencePhoneAction fromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		String normalized = value.trim().toUpperCase(Locale.ROOT);
		for (MineFluencePhoneAction action : values()) {
			if (action.name().equals(normalized) || action.serializedName().equalsIgnoreCase(value)) {
				return action;
			}
		}
		return null;
	}
}
