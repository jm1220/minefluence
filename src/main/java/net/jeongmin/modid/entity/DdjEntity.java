package net.jeongmin.modid.entity;

import net.jeongmin.modid.MineFluence;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DdjEntity extends HostileEntity implements GeoEntity {
	private static final TrackedData<Boolean> IS_ATTACKING =
			DataTracker.registerData(DdjEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> IS_ENRAGED =
			DataTracker.registerData(DdjEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> JUST_ENRAGED =
			DataTracker.registerData(DdjEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	private static final double MAX_HEALTH = 60.0D;
	private static final double NORMAL_SPEED = 0.25D;
	private static final double ENRAGED_SPEED = 0.4D;
	private static final double NORMAL_DAMAGE = 5.0D;
	private static final double ENRAGED_DAMAGE = 9.0D;
	private static final float ENRAGE_HEALTH_THRESHOLD = 0.5F;
	private static final int ANGRY_ANIMATION_DURATION = 30;
	private static final int ATTACK_ANIMATION_DURATION = 20;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private int angryAnimationTicks;
	private int attackAnimationTicks;

	public DdjEntity(EntityType<? extends DdjEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, MAX_HEALTH)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, NORMAL_SPEED)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, NORMAL_DAMAGE)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D)
				.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.5D);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(IS_ATTACKING, false);
		builder.add(IS_ENRAGED, false);
		builder.add(JUST_ENRAGED, false);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new DdjMeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(4, new LookAroundGoal(this));

		this.targetSelector.add(0, new RevengeGoal(this));
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, VillagerEntity.class, true));
	}

	public void startAttackAnimation() {
		if (this.getWorld().isClient()) {
			return;
		}
		this.attackAnimationTicks = ATTACK_ANIMATION_DURATION;
		this.getDataTracker().set(IS_ATTACKING, true);
	}

	@Override
	public boolean tryAttack(Entity target) {
		return super.tryAttack(target);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.getWorld().isClient()) {
			return;
		}

		checkEnrageTransition();
		destroyBlocksAround();
		tickAnimationFlags();
	}

	private void tickAnimationFlags() {
		if (this.angryAnimationTicks > 0) {
			this.angryAnimationTicks--;
			if (this.angryAnimationTicks == 0) {
				this.getDataTracker().set(JUST_ENRAGED, false);
			}
		}

		if (this.attackAnimationTicks > 0) {
			this.attackAnimationTicks--;
			if (this.attackAnimationTicks == 0) {
				this.getDataTracker().set(IS_ATTACKING, false);
			}
		}
	}

	private void checkEnrageTransition() {
		if (this.getDataTracker().get(IS_ENRAGED)) {
			return;
		}
		if (this.getHealth() / this.getMaxHealth() > ENRAGE_HEALTH_THRESHOLD) {
			return;
		}

		this.getDataTracker().set(IS_ENRAGED, true);
		this.getDataTracker().set(JUST_ENRAGED, true);
		this.angryAnimationTicks = ANGRY_ANIMATION_DURATION;

		EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speedAttribute != null) {
			speedAttribute.setBaseValue(ENRAGED_SPEED);
		}

		EntityAttributeInstance damageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		if (damageAttribute != null) {
			damageAttribute.setBaseValue(ENRAGED_DAMAGE);
		}

		MineFluence.LOGGER.info("[DDJ] Enraged at {}/{} health.", this.getHealth(), this.getMaxHealth());
	}

	private void destroyBlocksAround() {
		BlockPos pos = this.getBlockPos();
		BlockPos[] checkPositions = {
				pos.down(),
				pos,
				pos.north(),
				pos.south(),
				pos.east(),
				pos.west(),
				pos.north().down(),
				pos.south().down(),
				pos.east().down(),
				pos.west().down()
		};

		for (BlockPos checkPos : checkPositions) {
			BlockState state = this.getWorld().getBlockState(checkPos);

			if (state.isOf(Blocks.WHEAT)
					|| state.isOf(Blocks.CARROTS)
					|| state.isOf(Blocks.POTATOES)
					|| state.isOf(Blocks.BEETROOTS)
					|| state.isOf(Blocks.HAY_BLOCK)) {
				this.getWorld().breakBlock(checkPos, true, this, 512);
			} else if (state.getBlock() instanceof FarmlandBlock) {
				this.getWorld().setBlockState(checkPos, Blocks.DIRT.getDefaultState());
			}
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		AnimationController<DdjEntity> controller =
				new AnimationController<>(this, "controller", 5, this::predicate);

		controller.setParticleKeyframeHandler(event -> {
			if (!this.getWorld().isClient()) {
				return;
			}

			String effectName = event.getKeyframeData().getEffect();
			String locatorName = event.getKeyframeData().getLocator();
			spawnEffectParticles(effectName, locatorName);
		});

		controllers.add(controller);
	}

	private void spawnEffectParticles(String effectName, String locatorName) {
		if (effectName == null) {
			return;
		}

		double[] spawnPos = getLocatorPosition(locatorName);
		double px = spawnPos[0];
		double py = spawnPos[1];
		double pz = spawnPos[2];

		switch (effectName) {
			case "ddj_ground" -> {
				for (int i = 0; i < 12; i++) {
					this.getWorld().addParticle(
							new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
							px + (this.random.nextDouble() - 0.5D) * 0.8D,
							py,
							pz + (this.random.nextDouble() - 0.5D) * 0.8D,
							(this.random.nextDouble() - 0.5D) * 0.2D,
							0.15D,
							(this.random.nextDouble() - 0.5D) * 0.2D
					);
				}
			}
			case "ddj_flame" -> {
				for (int i = 0; i < 60; i++) {
					double angle = this.random.nextDouble() * Math.PI * 2.0D;
					double spread = this.random.nextDouble() * 0.12D;
					double velocityX = Math.cos(angle) * spread;
					double velocityZ = Math.sin(angle) * spread;
					double velocityY = 0.04D + this.random.nextDouble() * 0.06D;
					double startY = py + this.random.nextDouble() * 0.6D;

					this.getWorld().addParticle(
							ParticleTypes.SOUL_FIRE_FLAME,
							px + (this.random.nextDouble() - 0.5D) * 0.15D,
							startY,
							pz + (this.random.nextDouble() - 0.5D) * 0.15D,
							velocityX,
							velocityY,
							velocityZ
					);
				}
			}
			case "ddj_swing" -> {
				int count = 8;
				float yawRadians = (float) Math.toRadians(this.getYaw());
				double rightX = Math.cos(yawRadians);
				double rightZ = Math.sin(yawRadians);

				for (int i = 0; i < count; i++) {
					double t = (i / (double) (count - 1)) * 2.0D - 1.0D;
					double offsetX = rightX * t;
					double offsetZ = rightZ * t;
					double offsetY = t;

					this.getWorld().addParticle(
							ParticleTypes.SWEEP_ATTACK,
							px + offsetX,
							py + offsetY,
							pz + offsetZ,
							0.0D,
							0.0D,
							0.0D
					);
				}
			}
			case "ddj_anger" -> {
				for (int i = 0; i < 20; i++) {
					this.getWorld().addParticle(
							ParticleTypes.SMOKE,
							px + (this.random.nextDouble() - 0.5D) * 1.8D,
							py + this.random.nextDouble() * 1.8D,
							pz + (this.random.nextDouble() - 0.5D) * 1.8D,
							(this.random.nextDouble() - 0.5D) * 0.1D,
							0.08D,
							(this.random.nextDouble() - 0.5D) * 0.1D
					);
				}
				for (int i = 0; i < 6; i++) {
					this.getWorld().addParticle(
							ParticleTypes.LARGE_SMOKE,
							px + (this.random.nextDouble() - 0.5D) * 1.5D,
							py + this.random.nextDouble() * 1.5D,
							pz + (this.random.nextDouble() - 0.5D) * 1.5D,
							0.0D,
							0.1D,
							0.0D
					);
				}
			}
			default -> MineFluence.LOGGER.debug("[DDJ] Unknown particle keyframe effect: {}", effectName);
		}
	}

	private double[] getLocatorPosition(String locatorName) {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		double height = this.getHeight();
		float yawRadians = (float) Math.toRadians(this.getYaw());
		double forwardX = -Math.sin(yawRadians);
		double forwardZ = Math.cos(yawRadians);
		double rightX = Math.cos(yawRadians);
		double rightZ = Math.sin(yawRadians);

		return switch (locatorName == null ? "" : locatorName) {
			case "locator" -> new double[] {
					x + forwardX * 0.6D + rightX * 0.5D,
					y + height * 0.6D,
					z + forwardZ * 0.6D + rightZ * 0.5D
			};
			case "locator2" -> new double[] {
					x + forwardX * 0.6D - rightX * 0.5D,
					y + height * 0.6D,
					z + forwardZ * 0.6D - rightZ * 0.5D
			};
			case "locator_FL" -> new double[] {
					x - rightX * 0.4D,
					y + 0.1D,
					z - rightZ * 0.4D
			};
			case "locator_FR" -> new double[] {
					x + rightX * 0.4D,
					y + 0.1D,
					z + rightZ * 0.4D
			};
			default -> new double[] {x, y + height * 0.5D, z};
		};
	}

	private PlayState predicate(AnimationState<DdjEntity> state) {
		AnimationController<?> controller = state.getController();
		boolean justEnraged = this.getDataTracker().get(JUST_ENRAGED);
		boolean isAttacking = this.getDataTracker().get(IS_ATTACKING);
		boolean isEnraged = this.getDataTracker().get(IS_ENRAGED);

		if (justEnraged) {
			controller.setAnimation(RawAnimation.begin().thenPlay("angry"));
			return PlayState.CONTINUE;
		}

		if (isAttacking) {
			controller.setAnimation(RawAnimation.begin().thenPlay("attack2"));
			return PlayState.CONTINUE;
		}

		if (state.isMoving()) {
			controller.setAnimation(RawAnimation.begin().thenLoop(isEnraged ? "run" : "walk"));
		} else {
			controller.setAnimation(RawAnimation.begin().thenLoop("idle"));
		}

		return PlayState.CONTINUE;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
}
