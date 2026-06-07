package net.jeongmin.modid.mission;

public record MineFluenceMission(
		String id,
		int index,
		MineFluenceMissionRoute route,
		String title,
		String description,
		String objectiveText,
		int targetProgress,
		int baseFollowerReward,
		int baseSocialCredibilityReward
) {
}
