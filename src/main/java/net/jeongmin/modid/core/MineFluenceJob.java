package net.jeongmin.modid.core;

import java.util.Locale;

public enum MineFluenceJob {
	NONE,
	FARMER,
	ARCHITECT,
	COOK;

	public static MineFluenceJob fromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return NONE;
		}

		try {
			return MineFluenceJob.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			return NONE;
		}
	}

	public String serializedName() {
		return name();
	}

	public String displayName() {
		return switch (this) {
			case NONE -> "None";
			case FARMER -> "Farmer";
			case ARCHITECT -> "Architect";
			case COOK -> "Cook";
		};
	}
}
