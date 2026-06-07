package net.jeongmin.modid.weapon;

import java.util.Locale;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum MineFluenceWeaponTier {
	WOOD(MineFluenceBalance.WEAPON_TIER_WOOD_FOLLOWERS, "Wood", Items.WOODEN_HOE),
	STONE(MineFluenceBalance.WEAPON_TIER_STONE_FOLLOWERS, "Stone", Items.STONE_HOE),
	IRON(MineFluenceBalance.WEAPON_TIER_IRON_FOLLOWERS, "Iron", Items.IRON_HOE),
	GOLD(MineFluenceBalance.WEAPON_TIER_GOLD_FOLLOWERS, "Gold", Items.GOLDEN_HOE),
	DIAMOND(MineFluenceBalance.WEAPON_TIER_DIAMOND_FOLLOWERS, "Diamond", Items.DIAMOND_HOE);

	private final int minimumFollowers;
	private final String displayName;
	private final Item farmerDemoItem;

	MineFluenceWeaponTier(int minimumFollowers, String displayName, Item farmerDemoItem) {
		this.minimumFollowers = minimumFollowers;
		this.displayName = displayName;
		this.farmerDemoItem = farmerDemoItem;
	}

	public static MineFluenceWeaponTier fromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return WOOD;
		}

		try {
			return MineFluenceWeaponTier.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			return WOOD;
		}
	}

	public int minimumFollowers() {
		return minimumFollowers;
	}

	public String displayName() {
		return displayName;
	}

	public Item farmerDemoItem() {
		return farmerDemoItem;
	}

	public String serializedName() {
		return name();
	}
}
