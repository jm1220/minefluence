package net.jeongmin.modid.billboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.minecraft.util.Identifier;

public final class MineFluenceBillboardImageResolver {
	public static final String DEFAULT_IMAGE_ID = "default";
	public static final int FIRST_MISSION_IMAGE_INDEX = 1;
	public static final int LAST_MISSION_IMAGE_INDEX = 7;

	private static final List<String> AVAILABLE_IMAGE_IDS = createAvailableImageIds();
	private static final Set<String> MISSING_UPLOAD_ASSET_WARNINGS = new HashSet<>();

	private MineFluenceBillboardImageResolver() {
	}

	public static String resolveUploadedMissionImage(int missionIndex, MineFluenceMissionRoute route, PostingStyle style) {
		MineFluenceMissionRoute resolvedRoute = route == null || route == MineFluenceMissionRoute.NONE
				? MineFluenceMissionRoute.GOOD
				: route;
		PostingStyle resolvedStyle = style == null ? PostingStyle.NORMAL : style;
		String requestedImageId = missionImageId(missionIndex, resolvedRoute, resolvedStyle);

		if (missionIndex < FIRST_MISSION_IMAGE_INDEX || missionIndex > LAST_MISSION_IMAGE_INDEX) {
			MineFluence.LOGGER.warn(
					"[MineFluence] Billboard upload image request out of range: mission={} route={} post={}; using {}.",
					missionIndex,
					resolvedRoute,
					resolvedStyle,
					DEFAULT_IMAGE_ID
			);
			return DEFAULT_IMAGE_ID;
		}

		if (!isKnownImageId(requestedImageId) || !assetExists(requestedImageId)) {
			warnMissingUploadAsset(missionIndex, resolvedRoute, resolvedStyle, requestedImageId);
			return DEFAULT_IMAGE_ID;
		}

		MineFluence.LOGGER.info(
				"[MineFluence] Billboard upload image resolved: mission={} route={} post={} path={}",
				missionIndex,
				resolvedRoute,
				resolvedStyle,
				texturePath(requestedImageId)
		);
		return requestedImageId;
	}

	public static String resolveUploadedMissionImage(int missionIndex, PostingStyle style) {
		return resolveUploadedMissionImage(missionIndex, MineFluenceMissionRoute.GOOD, style);
	}

	public static Identifier textureForImageId(String imageId) {
		String normalizedImageId = normalizeImageId(imageId);
		String resolvedImageId = isKnownImageId(normalizedImageId) ? normalizedImageId : DEFAULT_IMAGE_ID;
		return Identifier.of(MineFluence.MOD_ID, "textures/billboard/" + resolvedImageId + ".png");
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
			imageIds.add("mission_" + missionIndex);
			imageIds.add(missionImageId(missionIndex, MineFluenceMissionRoute.GOOD, PostingStyle.NORMAL));
			imageIds.add(missionImageId(missionIndex, MineFluenceMissionRoute.GOOD, PostingStyle.EXAGGERATED));
			imageIds.add(missionImageId(missionIndex, MineFluenceMissionRoute.BAD, PostingStyle.NORMAL));
			imageIds.add(missionImageId(missionIndex, MineFluenceMissionRoute.BAD, PostingStyle.EXAGGERATED));
		}
		return List.copyOf(imageIds);
	}

	private static String routeAssetSuffix(MineFluenceMissionRoute route) {
		return route == MineFluenceMissionRoute.BAD ? "bad" : "good";
	}

	private static String missionImageId(int missionIndex, MineFluenceMissionRoute route, PostingStyle style) {
		return "mission_" + missionIndex + "_" + routeAssetSuffix(route) + "_" + style.assetSuffix();
	}

	private static boolean assetExists(String imageId) {
		return MineFluenceBillboardImageResolver.class.getClassLoader().getResource(resourcePath(imageId)) != null;
	}

	private static void warnMissingUploadAsset(
			int missionIndex,
			MineFluenceMissionRoute route,
			PostingStyle style,
			String imageId
	) {
		String key = missionIndex + ":" + route + ":" + style + ":" + imageId;
		if (!MISSING_UPLOAD_ASSET_WARNINGS.add(key)) {
			return;
		}

		MineFluence.LOGGER.warn(
				"[MineFluence] Billboard upload image missing: mission={} route={} post={} expectedPath={}; using {}.",
				missionIndex,
				route,
				style,
				texturePath(imageId),
				texturePath(DEFAULT_IMAGE_ID)
		);
	}

	private static String texturePath(String imageId) {
		return "textures/billboard/" + imageId + ".png";
	}

	private static String resourcePath(String imageId) {
		return "assets/" + MineFluence.MOD_ID + "/" + texturePath(imageId);
	}
}
