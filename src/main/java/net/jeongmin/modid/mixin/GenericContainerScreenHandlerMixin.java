package net.jeongmin.modid.mixin;

import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreenHandler.class)
public abstract class GenericContainerScreenHandlerMixin {
	@Shadow
	@Final
	private Inventory inventory;

	@Inject(
			method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
			at = @At("TAIL")
	)
	private void minefluence$captureContainerOpen(
			ScreenHandlerType<?> type,
			int syncId,
			PlayerInventory playerInventory,
			Inventory inventory,
			int rows,
			CallbackInfo info
	) {
		if (playerInventory.player instanceof ServerPlayerEntity serverPlayer) {
			MineFluenceMissionProgressManager.onGenericContainerOpened(serverPlayer, inventory);
		}
	}

	@Inject(method = "onClosed", at = @At("HEAD"))
	private void minefluence$countContainerItemsRemoved(PlayerEntity player, CallbackInfo info) {
		MineFluenceMissionProgressManager.onGenericContainerClosed(player, inventory);
	}
}
