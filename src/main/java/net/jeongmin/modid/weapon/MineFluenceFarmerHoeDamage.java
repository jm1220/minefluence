package net.jeongmin.modid.weapon;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluenceFarmerHoeDamage {
	private MineFluenceFarmerHoeDamage() {
	}

	public static float adjustedAttackDamage(PlayerEntity attacker, Entity target, DamageSource source, float amount) {
		if (!(attacker instanceof ServerPlayerEntity serverPlayer) || serverPlayer.getWorld().isClient()) {
			return amount;
		}
		if (!source.isOf(DamageTypes.PLAYER_ATTACK) || source.getAttacker() != serverPlayer || source.getSource() != serverPlayer) {
			return amount;
		}

		MineFluencePlayerData data = MineFluenceWorldState.get(serverPlayer.getServer()).getPlayerData(serverPlayer);
		if (data.getSelectedJob() != MineFluenceJob.FARMER || !MineFluenceInvasionManager.isActiveTrackedInvader(data, target)) {
			return amount;
		}

		MineFluenceWeaponTier tier = MineFluenceWeaponManager.tierForFarmerDemoWeapon(serverPlayer.getWeaponStack());
		if (tier == null) {
			return amount;
		}

		return amount + (float) bonusDamage(tier);
	}

	public static double bonusDamage(MineFluenceWeaponTier tier) {
		return switch (tier) {
			case WOOD -> MineFluenceBalance.FARMER_HOE_WOOD_INVASION_BONUS_DAMAGE;
			case STONE -> MineFluenceBalance.FARMER_HOE_STONE_INVASION_BONUS_DAMAGE;
			case IRON -> MineFluenceBalance.FARMER_HOE_IRON_INVASION_BONUS_DAMAGE;
			case GOLD -> MineFluenceBalance.FARMER_HOE_GOLD_INVASION_BONUS_DAMAGE;
			case DIAMOND -> MineFluenceBalance.FARMER_HOE_DIAMOND_INVASION_BONUS_DAMAGE;
		};
	}
}
