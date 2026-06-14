package net.jeongmin.modid.mission;

import java.util.List;

import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluenceMissionSupplies {
	private MineFluenceMissionSupplies() {
	}

	public static void grantForMissionStart(ServerPlayerEntity player, MineFluencePlayerData data, MineFluenceMission mission) {
		if (!data.hasActiveMission()
				|| data.getActiveMissionIndex() != mission.index()
				|| data.getActiveMissionRoute() != mission.route()
				|| data.hasMissionSuppliesGranted(mission.index(), mission.route())) {
			return;
		}

		List<ItemStack> supplies = suppliesFor(mission);
		data.markMissionSuppliesGranted(mission.index(), mission.route());
		MineFluenceWorldState.get(player.getServer()).markDirty();
		if (supplies.isEmpty()) {
			return;
		}

		for (ItemStack supply : supplies) {
			giveOrDrop(player, supply);
		}
		player.getInventory().markDirty();
		MineFluenceDisplay.sendChat(player, "Mission supplies added to your inventory.");
	}

	private static List<ItemStack> suppliesFor(MineFluenceMission mission) {
		if (mission.route() == MineFluenceMissionRoute.BAD) {
			// Current Bad Farmer missions are debug-only/destructive and do not have clean item prerequisites.
			return List.of();
		}

		return switch (mission.index()) {
			case 1 -> List.of(new ItemStack(Items.POPPY, 3)); // Poppy is the chosen simple flower item.
			case 2 -> List.of(new ItemStack(Items.WHEAT_SEEDS, 5));
			case 4 -> List.of(new ItemStack(Items.POTATO, 10));
			// Mission 5 requires obtaining composters after start, so direct supplies would undermine it.
			case 6 -> List.of(new ItemStack(Items.HAY_BLOCK, 1));
			// The basic hoe is skipped because vanilla hoes are managed by the Farmer weapon upgrade system.
			case 7 -> List.of(
					new ItemStack(Items.WATER_BUCKET, 1),
					new ItemStack(Items.COMPOSTER, 1),
					new ItemStack(Items.WHEAT_SEEDS, 8)
			);
			default -> List.of();
		};
	}

	private static void giveOrDrop(ServerPlayerEntity player, ItemStack supply) {
		ItemStack remaining = supply.copy();
		player.getInventory().insertStack(remaining);
		if (!remaining.isEmpty()) {
			player.dropItem(remaining, false, true);
		}
	}
}
