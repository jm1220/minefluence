package net.jeongmin.modid.config;

public final class MineFluenceBalance {
	public static final int FOLLOWER_MIN = 0;
	public static final int FOLLOWER_MAX = 100;
	public static final int FOLLOWER_DEFAULT = 0;

	public static final int SOCIAL_CREDIBILITY_MIN = -500;
	public static final int SOCIAL_CREDIBILITY_MAX = 500;
	public static final int SOCIAL_CREDIBILITY_DEFAULT = 50;

	public static final int LIE_VALUE_MIN = 0;
	public static final int LIE_VALUE_MAX = 100;
	public static final int LIE_VALUE_DEFAULT = 0;

	public static final int TOTAL_DEMO_MISSIONS = 7;
	public static final int COMPLETED_MISSION_MIN = 0;
	public static final int COMPLETED_MISSION_MAX = TOTAL_DEMO_MISSIONS;
	public static final int COMPLETED_MISSION_DEFAULT = 0;

	public static final int LAST_COMPLETED_INVASION_MIN = 0;
	public static final int LAST_COMPLETED_INVASION_MAX = 3;
	public static final int LAST_COMPLETED_INVASION_DEFAULT = 0;

	public static final int DEMO_START_FOLLOWER = 0;
	public static final int DEMO_START_SOCIAL_CREDIBILITY = 0;
	public static final int DEMO_START_LIE_VALUE = 0;
	public static final int DEMO_START_COMPLETED_MISSIONS = 0;
	public static final int DEMO_START_LAST_COMPLETED_INVASION = LAST_COMPLETED_INVASION_DEFAULT;

	public static final double EXAGGERATED_POSTING_MULTIPLIER = 1.5D;
	public static final double EXAGGERATED_POST_MULTIPLIER = EXAGGERATED_POSTING_MULTIPLIER;
	public static final int LIE_VALUE_INCREASE_PER_EXAGGERATED_POST = 10;
	public static final int LIE_VALUE_PER_EXAGGERATED_POST = LIE_VALUE_INCREASE_PER_EXAGGERATED_POST;

	public static final int FARMER_MISSION_1_FOLLOWER_REWARD = 1;
	public static final int FARMER_MISSION_1_SOCIAL_REWARD = 10;
	public static final int FARMER_MISSION_2_FOLLOWER_REWARD = 3;
	public static final int FARMER_MISSION_2_SOCIAL_REWARD = 30;
	public static final int FARMER_MISSION_3_FOLLOWER_REWARD = 5;
	public static final int FARMER_MISSION_3_SOCIAL_REWARD = 50;
	public static final int FARMER_MISSION_4_FOLLOWER_REWARD = 7;
	public static final int FARMER_MISSION_4_SOCIAL_REWARD = 70;
	public static final int FARMER_MISSION_5_FOLLOWER_REWARD = 9;
	public static final int FARMER_MISSION_5_SOCIAL_REWARD = 90;
	public static final int FARMER_MISSION_6_FOLLOWER_REWARD = 11;
	public static final int FARMER_MISSION_6_SOCIAL_REWARD = 110;
	public static final int FARMER_MISSION_7_FOLLOWER_REWARD = 14;
	public static final int FARMER_MISSION_7_SOCIAL_REWARD = 140;

	public static final int FARMER_BAD_MISSION_1_FOLLOWER_REWARD = 2;
	public static final int FARMER_BAD_MISSION_1_SOCIAL_REWARD = -10;
	public static final int FARMER_BAD_MISSION_2_FOLLOWER_REWARD = 5;
	public static final int FARMER_BAD_MISSION_2_SOCIAL_REWARD = -30;
	public static final int FARMER_BAD_MISSION_3_FOLLOWER_REWARD = 10;
	public static final int FARMER_BAD_MISSION_3_SOCIAL_REWARD = -50;
	public static final int FARMER_BAD_MISSION_4_FOLLOWER_REWARD = 14;
	public static final int FARMER_BAD_MISSION_4_SOCIAL_REWARD = -70;
	public static final int FARMER_BAD_MISSION_5_FOLLOWER_REWARD = 18;
	public static final int FARMER_BAD_MISSION_5_SOCIAL_REWARD = -90;
	public static final int FARMER_BAD_MISSION_6_FOLLOWER_REWARD = 23;
	public static final int FARMER_BAD_MISSION_6_SOCIAL_REWARD = -115;
	public static final int FARMER_BAD_MISSION_7_FOLLOWER_REWARD = 29;
	public static final int FARMER_BAD_MISSION_7_SOCIAL_REWARD = -145;

	public static final int FARMER_MISSION_1_TARGET = 3;
	public static final int FARMER_MISSION_2_TARGET = 5;
	public static final int FARMER_MISSION_3_TARGET = 2;
	public static final int FARMER_MISSION_4_TARGET = 10;
	public static final int FARMER_MISSION_5_TARGET = 2;
	public static final int FARMER_MISSION_6_TARGET = 1;
	public static final int FARMER_MISSION_7_WATER_TARGET = 1;
	public static final int FARMER_MISSION_7_FARMLAND_TARGET = 8;
	public static final int FARMER_MISSION_7_COMPOSTER_TARGET = 1;
	public static final int FARMER_MISSION_7_TARGET = FARMER_MISSION_7_WATER_TARGET + FARMER_MISSION_7_FARMLAND_TARGET + FARMER_MISSION_7_COMPOSTER_TARGET;

	public static final int FARMER_BAD_MISSION_1_TARGET = 3;
	public static final int FARMER_BAD_MISSION_2_TARGET = 5;
	public static final int FARMER_BAD_MISSION_3_TARGET = 2;
	public static final int FARMER_BAD_MISSION_4_TARGET = 10;
	public static final int FARMER_BAD_MISSION_5_TARGET = 2;
	public static final int FARMER_BAD_MISSION_6_TARGET = 1;
	public static final int FARMER_BAD_MISSION_7_TARGET = 1;

	public static final int MISSION_SCAN_TICK_INTERVAL = 20;

	public static final int INVASION_1_TRIGGER_MISSION_COUNT = 2;
	public static final int INVASION_2_TRIGGER_MISSION_COUNT = 5;
	public static final int INVASION_3_TRIGGER_MISSION_COUNT = 7;

	public static final int INVASION_WEAK_ZOMBIE_COUNT = 1;
	public static final int INVASION_MEDIUM_ZOMBIE_COUNT = 3;
	public static final int INVASION_STRONG_ZOMBIE_COUNT = 5;

	public static final int INVASION_1_WEAK_SOCIAL_THRESHOLD = 30;
	public static final int INVASION_1_STRONG_SOCIAL_THRESHOLD = -21;
	public static final int INVASION_2_WEAK_SOCIAL_THRESHOLD = 150;
	public static final int INVASION_2_STRONG_SOCIAL_THRESHOLD = -150;
	public static final int INVASION_3_WEAK_SOCIAL_THRESHOLD = 200;
	public static final int INVASION_3_STRONG_SOCIAL_THRESHOLD = -200;

	public static final int INVASION_SPAWN_RADIUS_BLOCKS = 10;
	public static final int INVASION_MAX_SPAWN_ATTEMPTS = 16;
	public static final int INVASION_TICK_FEEDBACK_INTERVAL = 20;

	public static final int LIE_PENALTY_MINOR_THRESHOLD = 30;
	public static final int LIE_PENALTY_MAJOR_THRESHOLD = 70;
	public static final int LIE_PENALTY_MINOR_SOCIAL_CREDIBILITY = -10;
	public static final int LIE_PENALTY_MAJOR_SOCIAL_CREDIBILITY = -30;

	public static final int WEAPON_TIER_WOOD_FOLLOWERS = 0;
	public static final int WEAPON_TIER_STONE_FOLLOWERS = 5;
	public static final int WEAPON_TIER_IRON_FOLLOWERS = 30;
	public static final int WEAPON_TIER_GOLD_FOLLOWERS = 60;
	public static final int WEAPON_TIER_DIAMOND_FOLLOWERS = 90;

	public static final int ENDING_FOLLOWER_MID_THRESHOLD = 30;
	public static final int ENDING_FOLLOWER_HIGH_THRESHOLD = 70;
	public static final int ENDING_SOCIAL_CREDIBILITY_MID_THRESHOLD = 0;
	public static final int ENDING_SOCIAL_CREDIBILITY_HIGH_THRESHOLD = 200;

	private MineFluenceBalance() {
	}

	public static int clampFollower(int value) {
		return clamp(value, FOLLOWER_MIN, FOLLOWER_MAX);
	}

	public static int clampSocialCredibility(int value) {
		return clamp(value, SOCIAL_CREDIBILITY_MIN, SOCIAL_CREDIBILITY_MAX);
	}

	public static int clampLieValue(int value) {
		return clamp(value, LIE_VALUE_MIN, LIE_VALUE_MAX);
	}

	public static int clampCompletedMissionCount(int value) {
		return clamp(value, COMPLETED_MISSION_MIN, COMPLETED_MISSION_MAX);
	}

	public static int clampMissionIndex(int value) {
		return clamp(value, 0, TOTAL_DEMO_MISSIONS);
	}

	public static int clampMissionProgress(int value) {
		return Math.max(0, value);
	}

	public static int clampLastCompletedInvasionIndex(int value) {
		return clamp(value, LAST_COMPLETED_INVASION_MIN, LAST_COMPLETED_INVASION_MAX);
	}

	public static int clampActiveInvasionIndex(int value) {
		return clamp(value, 0, LAST_COMPLETED_INVASION_MAX);
	}

	public static int invasionIndexForCompletedMissionCount(int completedMissionCount) {
		return switch (completedMissionCount) {
			case INVASION_1_TRIGGER_MISSION_COUNT -> 1;
			case INVASION_2_TRIGGER_MISSION_COUNT -> 2;
			case INVASION_3_TRIGGER_MISSION_COUNT -> 3;
			default -> 0;
		};
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
