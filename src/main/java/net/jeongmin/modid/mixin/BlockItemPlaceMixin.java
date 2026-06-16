package net.jeongmin.modid.mixin;

import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemPlaceMixin {
	@Inject(method = "place", at = @At("RETURN"))
	private void minefluence$countMissionComposterPlacement(
			ItemPlacementContext context,
			CallbackInfoReturnable<ActionResult> info
	) {
		if (info.getReturnValue().isAccepted() && !context.getWorld().isClient()) {
			MineFluenceMissionProgressManager.onBlockPlaced(
					context.getPlayer(),
					context.getWorld().getBlockState(context.getBlockPos())
			);
		}
	}
}
