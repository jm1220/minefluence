package net.jeongmin.modid.mixin;

import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {
	@Inject(method = "setToDirt", at = @At("HEAD"))
	private static void minefluence$countPlayerFarmlandTrample(Entity entity, BlockState state, World world, BlockPos pos, CallbackInfo info) {
		MineFluenceMissionProgressManager.onFarmlandTrampled(entity, state, world, pos);
	}
}
