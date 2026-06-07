package net.jeongmin.modid.ending;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MineFluenceEndingRegistry {
	private static final String MEDIA_BASE_PATH = "assets/minefluence/endings/";

	private static final Map<String, MineFluenceEnding> ENDINGS_BY_KEY = Arrays.stream(MineFluenceEndingType.values())
			.map(MineFluenceEndingRegistry::fromType)
			.collect(Collectors.toUnmodifiableMap(
					ending -> key(ending.followerTier(), ending.socialTier()),
					Function.identity()
			));

	private static final Map<String, MineFluenceEnding> ENDINGS_BY_ID = createEndingsById();

	private MineFluenceEndingRegistry() {
	}

	public static MineFluenceEnding get(MineFluenceEndingTier followerTier, MineFluenceEndingTier socialTier) {
		return ENDINGS_BY_KEY.get(key(followerTier, socialTier));
	}

	public static Optional<MineFluenceEnding> getById(String id) {
		return Optional.ofNullable(ENDINGS_BY_ID.get(id));
	}

	private static Map<String, MineFluenceEnding> createEndingsById() {
		Map<String, MineFluenceEnding> endingsById = new HashMap<>();
		for (MineFluenceEnding ending : ENDINGS_BY_KEY.values()) {
			endingsById.put(ending.id(), ending);
		}

		MineFluenceEnding famousVillain = ENDINGS_BY_KEY.get(key(MineFluenceEndingTier.HIGH, MineFluenceEndingTier.LOW));
		endingsById.put("ending_high_low", famousVillain);
		endingsById.put("villain_influencer", famousVillain);
		return Map.copyOf(endingsById);
	}

	private static MineFluenceEnding fromType(MineFluenceEndingType type) {
		String followerTier = type.followerTier().serializedName();
		String socialTier = type.socialTier().serializedName();
		return new MineFluenceEnding(
				type.id(),
				type.displayName(),
				type.followerTier(),
				type.socialTier(),
				type.description(),
				MEDIA_BASE_PATH + "follower_" + followerTier + ".png",
				MEDIA_BASE_PATH + "social_" + socialTier + ".png",
				MEDIA_BASE_PATH + "ending_" + followerTier + "_" + socialTier + ".png"
		);
	}

	private static String key(MineFluenceEndingTier followerTier, MineFluenceEndingTier socialTier) {
		return followerTier.name() + "_" + socialTier.name();
	}
}
