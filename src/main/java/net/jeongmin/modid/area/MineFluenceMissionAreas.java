package net.jeongmin.modid.area;

import net.jeongmin.modid.mission.MineFluenceMissionRoute;

public final class MineFluenceMissionAreas {
	private MineFluenceMissionAreas() {
	}

	public static MineFluenceAreaType getAreaForMission(MineFluenceMissionRoute route, int missionIndex) {
		if (route == MineFluenceMissionRoute.GOOD) {
			return switch (missionIndex) {
				case 1 -> MineFluenceAreaType.GARDEN;
				case 2 -> MineFluenceAreaType.FARM;
				case 6 -> MineFluenceAreaType.SHARED_SPACE;
				case 7 -> MineFluenceAreaType.FARM_BUILD_AREA;
				default -> null;
			};
		}

		if (route == MineFluenceMissionRoute.BAD) {
			return switch (missionIndex) {
				case 2, 6 -> MineFluenceAreaType.FARM;
				case 4 -> MineFluenceAreaType.SHARED_SPACE;
				default -> null;
			};
		}
		return null;
	}
}
