package net.jeongmin.modid.mission;

import net.jeongmin.modid.config.MineFluenceBalance;

public record MineFluenceMissionBoardState(
		int missionIndex,
		boolean canChoose,
		boolean showOptions,
		String message,
		MineFluenceMissionOption goodOption,
		MineFluenceMissionOption badOption
) {
	public static MineFluenceMissionBoardState messageOnly(int missionIndex, String message) {
		return new MineFluenceMissionBoardState(
				missionIndex,
				false,
				false,
				message,
				MineFluenceMissionOption.empty(MineFluenceMissionRoute.GOOD),
				MineFluenceMissionOption.empty(MineFluenceMissionRoute.BAD)
		);
	}

	public static MineFluenceMissionBoardState choices(int missionIndex, boolean canChoose, String message) {
		return new MineFluenceMissionBoardState(
				missionIndex,
				canChoose,
				true,
				message,
				MineFluenceMissionOption.fromMission(missionOrFallback(missionIndex, MineFluenceMissionRoute.GOOD)),
				MineFluenceMissionOption.fromMission(missionOrFallback(missionIndex, MineFluenceMissionRoute.BAD))
		);
	}

	private static MineFluenceMission missionOrFallback(int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceMissionRoute resolvedRoute = route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route;
		return FarmerMissions.getMission(missionIndex, resolvedRoute)
				.orElseGet(() -> new MineFluenceMission(
						"unknown_" + missionIndex,
						missionIndex,
						resolvedRoute,
						"Unknown Mission",
						"No mission definition exists for this index.",
						"Reset or start the next valid Farmer mission.",
						1,
						0,
						0
				));
	}

	public record MineFluenceMissionOption(
			String route,
			String title,
			String objectiveText,
			int followerReward,
			int socialCredibilityReward
	) {
		public static MineFluenceMissionOption fromMission(MineFluenceMission mission) {
			return new MineFluenceMissionOption(
					mission.route().serializedName(),
					mission.title(),
					mission.objectiveText(),
					mission.baseFollowerReward(),
					mission.baseSocialCredibilityReward()
			);
		}

		public static MineFluenceMissionOption empty(MineFluenceMissionRoute route) {
			return new MineFluenceMissionOption(route.serializedName(), "", "", 0, 0);
		}

		public String rewardText() {
			return "Follower " + signed(followerReward) + ", Social " + signed(socialCredibilityReward);
		}

		private static String signed(int value) {
			if (value >= 0) {
				return "+" + value;
			}
			return Integer.toString(value);
		}
	}
}
