package net.jeongmin.modid.mixin;

import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackCraftMixin {
	@Inject(method = "onCraftByPlayer", at = @At("HEAD"))
	private void minefluence$countMissionComposterCraft(World world, PlayerEntity player, int amount, CallbackInfo info) {
		if (!world.isClient()) {
			MineFluenceMissionProgressManager.onItemCrafted(
					player,
					(ItemStack) (Object) this,
					amount
			);
		}
	}
}
