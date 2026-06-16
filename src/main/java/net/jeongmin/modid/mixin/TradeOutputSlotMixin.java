package net.jeongmin.modid.mixin;

import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.village.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TradeOutputSlot.class)
public abstract class TradeOutputSlotMixin {
	@Shadow
	@Final
	private Merchant merchant;

	@Inject(
			method = "onTakeItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/village/Merchant;trade(Lnet/minecraft/village/TradeOffer;)V",
					shift = At.Shift.AFTER
			)
	)
	private void minefluence$countCompletedVillagerTrade(PlayerEntity player, ItemStack stack, CallbackInfo info) {
		if (merchant instanceof Entity merchantEntity) {
			MineFluenceMissionProgressManager.onVillagerTradeCompleted(player, merchantEntity);
		}
	}
}
