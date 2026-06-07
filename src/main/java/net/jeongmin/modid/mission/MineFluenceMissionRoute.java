package net.jeongmin.modid.mission;

import java.util.Locale;

public enum MineFluenceMissionRoute {
	NONE,
	GOOD,
	BAD;

	public static MineFluenceMissionRoute fromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return NONE;
		}

		try {
			return MineFluenceMissionRoute.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			return NONE;
		}
	}

	public String serializedName() {
		return name();
	}
}
