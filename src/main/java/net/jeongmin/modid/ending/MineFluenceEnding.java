package net.jeongmin.modid.ending;

public record MineFluenceEnding(
		String id,
		String displayName,
		MineFluenceEndingTier followerTier,
		MineFluenceEndingTier socialTier,
		String description,
		String followerMediaPath,
		String socialMediaPath,
		String combinedMediaPath
) {
}
