package net.jeongmin.modid.billboard;

public enum PostingStyle {
	NORMAL("normally"),
	EXAGGERATED("exaggerated");

	private final String assetSuffix;

	PostingStyle(String assetSuffix) {
		this.assetSuffix = assetSuffix;
	}

	public String assetSuffix() {
		return assetSuffix;
	}

	public static PostingStyle fromExaggerated(boolean exaggerated) {
		return exaggerated ? EXAGGERATED : NORMAL;
	}
}
