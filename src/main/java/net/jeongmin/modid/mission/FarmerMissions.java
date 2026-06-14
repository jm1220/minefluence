package net.jeongmin.modid.mission;

import java.util.List;
import java.util.Optional;

import net.jeongmin.modid.config.MineFluenceBalance;

public final class FarmerMissions {
	private static final List<MineFluenceMission> GOOD_MISSIONS = List.of(
			new MineFluenceMission(
					"farmer_good_01_flowers",
					1,
					MineFluenceMissionRoute.GOOD,
					"Garden Starter",
					"Help the village look alive before you post your first farming update.",
					"Plant 3 flowers in the configured garden area.",
					MineFluenceBalance.FARMER_MISSION_1_TARGET,
					MineFluenceBalance.FARMER_MISSION_1_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_1_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_good_02_wheat",
					2,
					MineFluenceMissionRoute.GOOD,
					"Wheat for the Village",
					"Show that your influence can produce something useful.",
					"Plant 5 wheat seeds in the configured farm area.",
					MineFluenceBalance.FARMER_MISSION_2_TARGET,
					MineFluenceBalance.FARMER_MISSION_2_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_2_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_good_03_trade",
					3,
					MineFluenceMissionRoute.GOOD,
					"Local Farmer Collab",
					"Build credibility by working directly with a farmer villager.",
					"Interact with a farmer villager twice.",
					MineFluenceBalance.FARMER_MISSION_3_TARGET,
					MineFluenceBalance.FARMER_MISSION_3_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_3_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_good_04_potatoes",
					4,
					MineFluenceMissionRoute.GOOD,
					"Potato Giveaway",
					"Turn your audience into visible support for villagers.",
					"Right-click villagers with potatoes 10 times.",
					MineFluenceBalance.FARMER_MISSION_4_TARGET,
					MineFluenceBalance.FARMER_MISSION_4_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_4_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_good_05_job_blocks",
					5,
					MineFluenceMissionRoute.GOOD,
					"Workstation Boost",
					"Create more capacity for village farming work.",
					"Obtain 2 composters after starting this mission.",
					MineFluenceBalance.FARMER_MISSION_5_TARGET,
					MineFluenceBalance.FARMER_MISSION_5_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_5_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_good_06_hay_bale",
					6,
					MineFluenceMissionRoute.GOOD,
					"Shared Supplies",
					"Make a public contribution the whole village can see.",
					"Place 1 hay bale block in the configured shared space.",
					MineFluenceBalance.FARMER_MISSION_6_TARGET,
					MineFluenceBalance.FARMER_MISSION_6_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_6_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_good_07_farm_plot",
					7,
					MineFluenceMissionRoute.GOOD,
					"Community Farm Plot",
					"Finish the demo route by building lasting village infrastructure.",
					"Build a farm plot in the configured farm build area: 1 water, 8 farmland, and 1 composter.",
					MineFluenceBalance.FARMER_MISSION_7_TARGET,
					MineFluenceBalance.FARMER_MISSION_7_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_MISSION_7_SOCIAL_REWARD
			)
	);

	private static final List<MineFluenceMission> BAD_MISSIONS = List.of(
			new MineFluenceMission(
					"farmer_bad_01_false_alarm",
					1,
					MineFluenceMissionRoute.BAD,
					"False Alarm",
					"Create a dramatic scare for attention.",
					"Ring the village bell repeatedly as a false alarm.",
					MineFluenceBalance.FARMER_BAD_MISSION_1_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_1_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_1_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_bad_02_trample_farm",
					2,
					MineFluenceMissionRoute.BAD,
					"Trample the Farm",
					"Turn village damage into provocative content.",
					"Trample or destroy 5 farmland blocks in the configured farm area.",
					MineFluenceBalance.FARMER_BAD_MISSION_2_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_2_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_2_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_bad_03_hit_villagers",
					3,
					MineFluenceMissionRoute.BAD,
					"Hit the Villagers",
					"Escalate conflict with villagers for views.",
					"Hit villagers twice.",
					MineFluenceBalance.FARMER_BAD_MISSION_3_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_3_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_3_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_bad_04_steal",
					4,
					MineFluenceMissionRoute.BAD,
					"Steal from Villagers",
					"Take supplies and turn the theft into a viral post.",
					"Steal 10 items from a chest in the configured shared area.",
					MineFluenceBalance.FARMER_BAD_MISSION_4_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_4_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_4_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_bad_05_break_job_blocks",
					5,
					MineFluenceMissionRoute.BAD,
					"Break Farmer Job Blocks",
					"Undercut the village's farming work for attention.",
					"Break 2 composters.",
					MineFluenceBalance.FARMER_BAD_MISSION_5_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_5_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_5_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_bad_06_destroy_plot",
					6,
					MineFluenceMissionRoute.BAD,
					"Destroy a Farm Plot",
					"Ruin a villager farm plot and post the spectacle.",
					"Destroy one farm plot block in the configured farm area.",
					MineFluenceBalance.FARMER_BAD_MISSION_6_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_6_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_6_SOCIAL_REWARD
			),
			new MineFluenceMission(
					"farmer_bad_07_kill_villagers",
					7,
					MineFluenceMissionRoute.BAD,
					"Kill Villagers",
					"Take the destructive route to its extreme.",
					"Kill villagers.",
					MineFluenceBalance.FARMER_BAD_MISSION_7_TARGET,
					MineFluenceBalance.FARMER_BAD_MISSION_7_FOLLOWER_REWARD,
					MineFluenceBalance.FARMER_BAD_MISSION_7_SOCIAL_REWARD
			)
	);

	private FarmerMissions() {
	}

	public static List<MineFluenceMission> all() {
		return GOOD_MISSIONS;
	}

	public static List<MineFluenceMission> allGood() {
		return GOOD_MISSIONS;
	}

	public static List<MineFluenceMission> allBad() {
		return BAD_MISSIONS;
	}

	public static Optional<MineFluenceMission> getByIndex(int index) {
		return getGoodMission(index);
	}

	public static Optional<MineFluenceMission> getGoodMission(int index) {
		return GOOD_MISSIONS.stream()
				.filter(mission -> mission.index() == index)
				.findFirst();
	}

	public static Optional<MineFluenceMission> getBadMission(int index) {
		return BAD_MISSIONS.stream()
				.filter(mission -> mission.index() == index)
				.findFirst();
	}

	public static Optional<MineFluenceMission> getMission(int index, MineFluenceMissionRoute route) {
		return switch (route) {
			case BAD -> getBadMission(index);
			case GOOD, NONE -> getGoodMission(index);
		};
	}
}
