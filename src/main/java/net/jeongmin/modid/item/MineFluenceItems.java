package net.jeongmin.modid.item;

import net.jeongmin.modid.MineFluence;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class MineFluenceItems {
	public static final Item SMARTPHONE = Registry.register(
			Registries.ITEM,
			Identifier.of(MineFluence.MOD_ID, "smartphone"),
			new MineFluenceSmartphoneItem(new Item.Settings().maxCount(1))
	);

	private MineFluenceItems() {
	}

	public static void register() {
		MineFluence.LOGGER.info("Registered MineFluence items.");
	}

	public static void ensureSingleSmartphone(ServerPlayerEntity player) {
		removeSmartphones(player);
		player.giveItemStack(new ItemStack(SMARTPHONE));
		player.getInventory().markDirty();
	}

	public static int removeSmartphones(ServerPlayerEntity player) {
		PlayerInventory inventory = player.getInventory();
		int removed = 0;
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (!stack.isEmpty() && stack.isOf(SMARTPHONE)) {
				removed += stack.getCount();
				inventory.setStack(slot, ItemStack.EMPTY);
			}
		}
		inventory.markDirty();
		return removed;
	}
}
