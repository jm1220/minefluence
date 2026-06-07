package net.jeongmin.modid.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.util.Hand;

public class DdjMeleeAttackGoal extends MeleeAttackGoal {
	private static final int DAMAGE_TICK = 10;
	private static final int ANIMATION_LENGTH = 24;

	private final DdjEntity ddj;
	private int attackTickCounter;

	public DdjMeleeAttackGoal(DdjEntity ddj, double speed, boolean pauseWhenMobIdle) {
		super(ddj, speed, pauseWhenMobIdle);
		this.ddj = ddj;
	}

	@Override
	protected void attack(LivingEntity target) {
		if (this.attackTickCounter == 0) {
			if (this.canAttack(target)) {
				this.resetCooldown();
				this.ddj.startAttackAnimation();
				this.attackTickCounter = 1;
			}
			return;
		}

		this.attackTickCounter++;
		if (this.attackTickCounter == DAMAGE_TICK
				&& this.mob.isInAttackRange(target)
				&& this.mob.getVisibilityCache().canSee(target)) {
			this.mob.swingHand(Hand.MAIN_HAND);
			this.mob.tryAttack(target);
		}

		if (this.attackTickCounter >= ANIMATION_LENGTH) {
			this.attackTickCounter = 0;
		}
	}

	@Override
	public void stop() {
		super.stop();
		this.attackTickCounter = 0;
	}
}
