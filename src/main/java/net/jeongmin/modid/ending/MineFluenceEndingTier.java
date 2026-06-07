package net.jeongmin.modid.ending;

public enum MineFluenceEndingTier {
	LOW,
	MID,
	HIGH;

	public String serializedName() {
		return name().toLowerCase();
	}
}
