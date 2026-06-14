package net.jeongmin.modid.mixin;

import net.jeongmin.modid.weapon.MineFluenceFarmerHoeDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttackMixin {
	@Redirect(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
					ordinal = 0
			)
	)
	private boolean minefluence$damageWithFarmerHoeBonus(Entity target, DamageSource source, float amount) {
		PlayerEntity attacker = (PlayerEntity) (Object) this;
		return target.damage(source, MineFluenceFarmerHoeDamage.adjustedAttackDamage(attacker, target, source, amount));
	}
}
