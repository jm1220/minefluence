package net.jeongmin.modid.core;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.jeongmin.modid.item.MineFluenceItems;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluencePlayerDeathManager {
	private MineFluencePlayerDeathManager() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayerEntity player) {
				MineFluenceDemoFlow.resetDemoProgress(player);
			}
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (alive) {
				return;
			}

			MineFluenceDemoFlow.resetDemoProgress(newPlayer);
			MineFluenceItems.ensureSingleSmartphone(newPlayer);
			MineFluenceDisplay.sendChat(newPlayer, "The demo was reset. Use your smartphone to replay the Tutorial.");
		});
	}
}
