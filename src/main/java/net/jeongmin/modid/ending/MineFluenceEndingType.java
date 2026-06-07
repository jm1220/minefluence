package net.jeongmin.modid.ending;

public enum MineFluenceEndingType {
	LOW_LOW(
			"ending_low_low",
			"Nobody",
			MineFluenceEndingTier.LOW,
			MineFluenceEndingTier.LOW,
			"Almost no one watched, and the village does not trust you."
	),
	LOW_MID(
			"ending_low_mid",
			"Quiet Neighbor",
			MineFluenceEndingTier.LOW,
			MineFluenceEndingTier.MID,
			"You stayed ordinary, but your choices did not destroy the community."
	),
	LOW_HIGH(
			"ending_low_high",
			"Law-abiding Citizen",
			MineFluenceEndingTier.LOW,
			MineFluenceEndingTier.HIGH,
			"You were not famous, but the village remembers you as trustworthy."
	),
	MID_LOW(
			"ending_mid_low",
			"Troublemaker",
			MineFluenceEndingTier.MID,
			MineFluenceEndingTier.LOW,
			"You gained some attention, but damaged the trust around you."
	),
	MID_MID(
			"ending_mid_mid",
			"Local Creator",
			MineFluenceEndingTier.MID,
			MineFluenceEndingTier.MID,
			"You balanced influence and responsibility, at least for now."
	),
	MID_HIGH(
			"ending_mid_high",
			"Honored Citizen",
			MineFluenceEndingTier.MID,
			MineFluenceEndingTier.HIGH,
			"You used your influence carefully and earned the village's respect."
	),
	HIGH_LOW(
			"the_famous_villain",
			"The Famous Villain",
			MineFluenceEndingTier.HIGH,
			MineFluenceEndingTier.LOW,
			"Everyone knows your name, but the village paid the price."
	),
	HIGH_MID(
			"ending_high_mid",
			"Mega Influencer",
			MineFluenceEndingTier.HIGH,
			MineFluenceEndingTier.MID,
			"You became famous, but your legacy remains complicated."
	),
	HIGH_HIGH(
			"ending_high_high",
			"Everyone's Role Model",
			MineFluenceEndingTier.HIGH,
			MineFluenceEndingTier.HIGH,
			"You became influential without losing the village's trust."
	);

	private final String id;
	private final String displayName;
	private final MineFluenceEndingTier followerTier;
	private final MineFluenceEndingTier socialTier;
	private final String description;

	MineFluenceEndingType(
			String id,
			String displayName,
			MineFluenceEndingTier followerTier,
			MineFluenceEndingTier socialTier,
			String description
	) {
		this.id = id;
		this.displayName = displayName;
		this.followerTier = followerTier;
		this.socialTier = socialTier;
		this.description = description;
	}

	public String id() {
		return id;
	}

	public String displayName() {
		return displayName;
	}

	public MineFluenceEndingTier followerTier() {
		return followerTier;
	}

	public MineFluenceEndingTier socialTier() {
		return socialTier;
	}

	public String description() {
		return description;
	}
}
