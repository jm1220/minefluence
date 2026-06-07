package net.jeongmin.modid.billboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.minecraft.util.Identifier;

public final class MineFluenceBillboardImageResolver {
	public static final String DEFAULT_IMAGE_ID = "default";
	public static final int FIRST_MISSION_IMAGE_INDEX = 1;
	public static final int LAST_MISSION_IMAGE_INDEX = 7;

	private static final List<String> AVAILABLE_IMAGE_IDS = createAvailableImageIds();

	private MineFluenceBillboardImageResolver() {
	}

	public static String resolveUploadedMissionImage(int missionIndex, MineFluenceMissionRoute route, PostingStyle style) {
		MineFluenceMissionRoute resolvedRoute = route == null || route == MineFluenceMissionRoute.NONE
				? MineFluenceMissionRoute.GOOD
				: route;
		PostingStyle resolvedStyle = style == null ? PostingStyle.NORMAL : style;
		String imageId = "mission_" + missionIndex + "_" + routeAssetSuffix(resolvedRoute) + "_" + resolvedStyle.assetSuffix();
		return isKnownImageId(imageId) ? imageId : DEFAULT_IMAGE_ID;
	}

	public static String resolveUploadedMissionImage(int missionIndex, PostingStyle style) {
		return resolveUploadedMissionImage(missionIndex, MineFluenceMissionRoute.GOOD, style);
	}

	public static Identifier textureForImageId(String imageId) {
		return Identifier.of(MineFluence.MOD_ID, "textures/billboard/" + normalizeImageId(imageId) + ".png");
	}

	public static List<String> availableImageIds() {
		return AVAILABLE_IMAGE_IDS;
	}

	public static boolean isKnownImageId(String imageId) {
		return AVAILABLE_IMAGE_IDS.contains(normalizeImageId(imageId));
	}

	public static String normalizeImageId(String imageId) {
		if (imageId == null) {
			return DEFAULT_IMAGE_ID;
		}

		String normalized = imageId.trim().toLowerCase(Locale.ROOT);
		if (normalized.startsWith("textures/billboard/")) {
			normalized = normalized.substring("textures/billboard/".length());
		}
		if (normalized.endsWith(".png")) {
			normalized = normalized.substring(0, normalized.length() - ".png".length());
		}
		if (normalized.isEmpty() || !Identifier.isPathValid("textures/billboard/" + normalized + ".png")) {
			return DEFAULT_IMAGE_ID;
		}
		return normalized;
	}

	private static List<String> createAvailableImageIds() {
		List<String> imageIds = new ArrayList<>();
		imageIds.add(DEFAULT_IMAGE_ID);
		for (int missionIndex = FIRST_MISSION_IMAGE_INDEX; missionIndex <= LAST_MISSION_IMAGE_INDEX; missionIndex++) {
			for (MineFluenceMissionRoute route : List.of(MineFluenceMissionRoute.GOOD, MineFluenceMissionRoute.BAD)) {
				for (PostingStyle style : PostingStyle.values()) {
					imageIds.add("mission_" + missionIndex + "_" + routeAssetSuffix(route) + "_" + style.assetSuffix());
				}
			}
		}
		return List.copyOf(imageIds);
	}

	private static String routeAssetSuffix(MineFluenceMissionRoute route) {
		return route == MineFluenceMissionRoute.BAD ? "bad" : "good";
	}
}
