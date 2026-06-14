package net.jeongmin.modid.weapon;

import java.util.Set;

import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class MineFluenceWeaponManager {
	private static final Set<Item> FARMER_DEMO_WEAPONS = Set.of(
			Items.WOODEN_HOE,
			Items.STONE_HOE,
			Items.IRON_HOE,
			Items.GOLDEN_HOE,
			Items.DIAMOND_HOE
	);

	private MineFluenceWeaponManager() {
	}

	public static MineFluenceWeaponTier determineTier(int follower) {
		if (follower >= MineFluenceWeaponTier.DIAMOND.minimumFollowers()) {
			return MineFluenceWeaponTier.DIAMOND;
		}
		if (follower >= MineFluenceWeaponTier.GOLD.minimumFollowers()) {
			return MineFluenceWeaponTier.GOLD;
		}
		if (follower >= MineFluenceWeaponTier.IRON.minimumFollowers()) {
			return MineFluenceWeaponTier.IRON;
		}
		if (follower >= MineFluenceWeaponTier.STONE.minimumFollowers()) {
			return MineFluenceWeaponTier.STONE;
		}
		return MineFluenceWeaponTier.WOOD;
	}

	public static MineFluenceWeaponTier tierForFarmerDemoWeapon(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		for (MineFluenceWeaponTier tier : MineFluenceWeaponTier.values()) {
			if (stack.isOf(tier.farmerDemoItem())) {
				return tier;
			}
		}
		return null;
	}

	public static boolean isFarmerDemoWeapon(ItemStack stack) {
		return tierForFarmerDemoWeapon(stack) != null;
	}

	public static MineFluenceWeaponTier updateWeapon(ServerPlayerEntity player, MineFluencePlayerData data) {
		MineFluenceWeaponTier expectedTier = determineTier(data.getFollower());
		MineFluenceWeaponTier previousTier = data.getCurrentWeaponTier();

		removeFarmerDemoWeapons(player);
		data.setCurrentWeaponTier(expectedTier);

		if (data.getSelectedJob() == MineFluenceJob.FARMER) {
			player.giveItemStack(createFarmerWeapon(expectedTier));
			if (previousTier != expectedTier) {
				MineFluenceDisplay.sendChat(player, "Weapon tier upgraded: " + expectedTier.displayName() + ".");
			}
		}

		MineFluenceWorldState.get(player.getServer()).markDirty();
		player.getInventory().markDirty();
		MineFluenceHud.refresh(player, data);
		return expectedTier;
	}

	public static void giveDebugWeapon(ServerPlayerEntity player, MineFluenceWeaponTier tier) {
		removeFarmerDemoWeapons(player);
		player.giveItemStack(createFarmerWeapon(tier));
		player.getInventory().markDirty();
		MineFluenceDisplay.sendChat(player, "Debug weapon given: " + tier.displayName() + ".");
		MineFluenceHud.refresh(player, MineFluenceWorldState.get(player.getServer()).getPlayerData(player));
	}

	public static void removeFarmerDemoWeapons(ServerPlayerEntity player) {
		PlayerInventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (!stack.isEmpty() && FARMER_DEMO_WEAPONS.contains(stack.getItem())) {
				inventory.setStack(slot, ItemStack.EMPTY);
			}
		}
		inventory.markDirty();
	}

	private static ItemStack createFarmerWeapon(MineFluenceWeaponTier tier) {
		ItemStack stack = new ItemStack(tier.farmerDemoItem());
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("MineFluence Farmer Hoe - " + tier.displayName()));
		return stack;
	}
}
