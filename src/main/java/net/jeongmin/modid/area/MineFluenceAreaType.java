package net.jeongmin.modid.area;

import java.util.Locale;

public enum MineFluenceAreaType {
	GARDEN("garden", "Garden"),
	FARM("farm", "Farm"),
	SHARED_SPACE("shared", "Shared Space"),
	FARM_BUILD_AREA("farm_build", "Farm Build Area");

	private final String commandName;
	private final String displayName;

	MineFluenceAreaType(String commandName, String displayName) {
		this.commandName = commandName;
		this.displayName = displayName;
	}

	public static MineFluenceAreaType fromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return MineFluenceAreaType.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			for (MineFluenceAreaType type : values()) {
				if (type.commandName.equalsIgnoreCase(value)) {
					return type;
				}
			}
			return null;
		}
	}

	public String commandName() {
		return commandName;
	}

	public String displayName() {
		return displayName;
	}

	public String serializedName() {
		return name();
	}
}
