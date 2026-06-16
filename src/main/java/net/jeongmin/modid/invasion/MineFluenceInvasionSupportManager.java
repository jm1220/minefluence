package net.jeongmin.modid.invasion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.area.MineFluenceDemoMapPreset;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public final class MineFluenceInvasionSupportManager {
	public static final String SUPPORT_TAG = "minefluence_invasion_support";

	private static final String SUPPORT_SLOT_TAG_PREFIX = "minefluence_invasion_support_slot_";
	private static final String SUPPORT_NAME = "MineFluence Defender";
	private static final int SEARCH_RADIUS = 96;
	private static final int SPAWN_VERTICAL_SEARCH = 8;
	private static final int SPAWN_HORIZONTAL_SEARCH = 5;
	private static final int POSITION_TARGET_RANGE = 24;
	private static final int FRIENDLY_TARGET_CHECK_INTERVAL = 10;

	private MineFluenceInvasionSupportManager() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof IronGolemEntity ally
					&& isSupportAlly(ally)
					&& source.getAttacker() instanceof PlayerEntity) {
				clearAggro(ally);
				return false;
			}
			return true;
		});
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceInvasionSupportManager::tickFriendlyTargetSafety);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			MineFluencePlayerData data = MineFluenceWorldState.get(server).getPlayerData(player);
			if (data.hasActiveInvasion()) {
				syncSupportAllies(player, data);
			} else {
				clearSupportAllies(player);
			}
		});
	}

	public static SpawnResult spawnForInvasion(ServerPlayerEntity player, MineFluencePlayerData data) {
		int targetCount = MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility());
		MineFluence.LOGGER.info(
				"[InvasionSupport] Spawn requested player={} invasion={} social={} expected={}",
				player.getName().getString(),
				data.getActiveInvasionIndex(),
				data.getSocialCredibility(),
				targetCount
		);
		SyncResult syncResult = reconcileTargetCount(player, targetCount, "invasion_start");
		SpawnResult result = new SpawnResult(targetCount, syncResult.spawnedCount());
		if (result.targetCount() <= 0) {
			MineFluenceDisplay.sendChat(player, "The village does not trust you enough to help.");
		} else if (result.spawnedCount() > 0) {
			MineFluenceDisplay.sendChat(player, "Villagers trust you. Defenders have joined the fight.");
		} else {
			MineFluenceDisplay.sendChat(player, "Village defenders could not find a safe place to join the fight.");
		}
		return result;
	}

	public static SpawnResult spawnForTesting(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		clearSupportAllies(player);
		int targetCount = MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility());
		SyncResult result = reconcileTargetCount(player, targetCount, "debug_spawn");
		return new SpawnResult(targetCount, result.spawnedCount());
	}

	public static SyncResult syncSupportAllies(ServerPlayerEntity player, MineFluencePlayerData data) {
		int targetCount = MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility());
		return reconcileTargetCount(player, targetCount, "sync");
	}

	public static int clearSupportAllies(ServerPlayerEntity player) {
		List<IronGolemEntity> allies = findTaggedSupportAllies(player.getServer().getOverworld());
		allies.forEach(Entity::discard);
		if (!allies.isEmpty()) {
			MineFluence.LOGGER.info("[InvasionSupport] Removed {} tagged support ally/allies.", allies.size());
		}
		return allies.size();
	}

	public static int countSupportAllies(ServerPlayerEntity player) {
		return findLivingSupportAllies(player.getServer().getOverworld()).size();
	}

	public static boolean isSupportAlly(Entity entity) {
		return entity != null && entity.getCommandTags().contains(SUPPORT_TAG);
	}

	public static void updateCombatTargets(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (!data.hasActiveInvasion()) {
			return;
		}

		ServerWorld world = player.getServer().getOverworld();
		List<IronGolemEntity> allies = findLivingSupportAllies(world);
		if (allies.isEmpty()) {
			return;
		}

		List<LivingEntity> invaders = findActiveInvaders(world, data);
		for (IronGolemEntity ally : allies) {
			clearFriendlyTarget(player.getServer(), ally);
			if (invaders.isEmpty()) {
				continue;
			}

			LivingEntity currentTarget = ally.getTarget();
			if (currentTarget != null && MineFluenceInvasionManager.isActiveTrackedInvader(data, currentTarget)) {
				continue;
			}

			LivingEntity nearest = invaders.stream()
					.min(Comparator.comparingDouble(ally::squaredDistanceTo))
					.orElse(null);
			if (nearest != null) {
				ally.setTarget(nearest);
			}
		}
	}

	private static void tickFriendlyTargetSafety(MinecraftServer server) {
		if (server.getTicks() % FRIENDLY_TARGET_CHECK_INTERVAL != 0) {
			return;
		}

		for (IronGolemEntity ally : findLivingSupportAllies(server.getOverworld())) {
			clearFriendlyTarget(server, ally);
		}
	}

	private static void clearFriendlyTarget(MinecraftServer server, IronGolemEntity ally) {
		LivingEntity target = ally.getTarget();
		boolean hasFriendlyTarget = target instanceof PlayerEntity || target instanceof VillagerEntity;
		boolean angryAtPlayer = ally.getAngryAt() != null
				&& server.getPlayerManager().getPlayer(ally.getAngryAt()) != null;
		if (hasFriendlyTarget || angryAtPlayer) {
			clearAggro(ally);
		}
	}

	private static void clearAggro(IronGolemEntity ally) {
		ally.setTarget(null);
		ally.setAttacker(null);
		ally.stopAnger();
		ally.getNavigation().stop();
	}

	private static SyncResult reconcileTargetCount(ServerPlayerEntity player, int targetCount, String reason) {
		ServerWorld world = player.getServer().getOverworld();
		List<IronGolemEntity> allies = findLivingSupportAllies(world);
		int actualCount = allies.size();
		MineFluence.LOGGER.info(
				"[InvasionSupport] Reconcile reason={} player={} expected={} actual={} center={}",
				reason,
				player.getName().getString(),
				targetCount,
				actualCount,
				MineFluenceDemoMapPreset.villageCenter().toShortString()
		);
		if (targetCount <= 0) {
			int removed = removeExtraAllies(allies, 0);
			MineFluence.LOGGER.info(
					"[InvasionSupport] Spawn skipped reason={} because Social Credibility tier expects no allies; removed={}.",
					reason,
					removed
			);
			return new SyncResult(0, targetCount, 0, removed);
		}

		int removed = removeExtraAllies(allies, targetCount);
		allies = findLivingSupportAllies(world);
		int spawned = spawnMissingAllies(world, allies, targetCount);
		int currentCount = findLivingSupportAllies(world).size();
		if (spawned == 0 && currentCount >= targetCount) {
			MineFluence.LOGGER.info(
					"[InvasionSupport] Spawn skipped reason={} because {} tagged ally/allies already satisfy target {}.",
					reason,
					currentCount,
					targetCount
			);
		}
		MineFluence.LOGGER.info(
				"[InvasionSupport] Reconcile complete reason={} expected={} current={} spawned={} removed={}",
				reason,
				targetCount,
				currentCount,
				spawned,
				removed
		);
		return new SyncResult(currentCount, targetCount, spawned, removed);
	}

	private static List<LivingEntity> findActiveInvaders(
			ServerWorld supportWorld,
			MineFluencePlayerData data
	) {
		List<LivingEntity> invaders = new ArrayList<>();
		for (java.util.UUID uuid : data.getActiveInvasionMobUuids()) {
			Entity entity = supportWorld.getEntity(uuid);
			if (entity instanceof LivingEntity living
					&& MineFluenceInvasionManager.isActiveTrackedInvader(data, living)) {
				invaders.add(living);
			}
		}
		return invaders;
	}

	private static List<IronGolemEntity> findLivingSupportAllies(ServerWorld world) {
		List<IronGolemEntity> allies = findTaggedSupportAllies(world);
		allies.removeIf(ally -> !ally.isAlive());
		return allies;
	}

	private static List<IronGolemEntity> findTaggedSupportAllies(ServerWorld world) {
		Box searchBox = new Box(MineFluenceDemoMapPreset.villageCenter()).expand(SEARCH_RADIUS);
		return new ArrayList<>(world.getEntitiesByClass(
				IronGolemEntity.class,
				searchBox,
				ally -> isSupportAlly(ally) && !ally.isRemoved()
		));
	}

	private static int removeExtraAllies(List<IronGolemEntity> allies, int targetCount) {
		if (allies.size() <= targetCount) {
			return 0;
		}

		allies.sort(Comparator.comparingInt(MineFluenceInvasionSupportManager::slotIndex));
		int removed = 0;
		for (int index = allies.size() - 1; index >= targetCount; index--) {
			allies.get(index).discard();
			removed++;
		}
		return removed;
	}

	private static int spawnMissingAllies(ServerWorld world, List<IronGolemEntity> existingAllies, int targetCount) {
		int missing = targetCount - existingAllies.size();
		if (missing <= 0) {
			return 0;
		}

		Set<Integer> occupiedSlots = new HashSet<>();
		for (IronGolemEntity ally : existingAllies) {
			int slot = slotIndex(ally);
			if (slot >= 0) {
				occupiedSlots.add(slot);
			}
		}

		int spawned = 0;
		List<BlockPos> spawnPoints = MineFluenceDemoMapPreset.invasionSupportSpawnPoints();
		for (int slot = 0; slot < spawnPoints.size() && spawned < missing; slot++) {
			if (occupiedSlots.contains(slot)) {
				continue;
			}
			if (spawnAlly(world, spawnPoints.get(slot), slot)) {
				spawned++;
				occupiedSlots.add(slot);
			}
		}
		return spawned;
	}

	private static boolean spawnAlly(ServerWorld world, BlockPos configuredPos, int slot) {
		IronGolemEntity ally = EntityType.IRON_GOLEM.create(world);
		if (ally == null) {
			MineFluence.LOGGER.warn("[InvasionSupport] Slot {} skipped: iron golem entity creation returned null.", slot);
			return false;
		}

		BlockPos spawnPos = findSafeSpawnPos(world, ally, configuredPos);
		if (spawnPos == null) {
			MineFluence.LOGGER.warn(
					"[InvasionSupport] Slot {} skipped: no safe position near configured anchor {}.",
					slot,
					configuredPos.toShortString()
			);
			return false;
		}

		ally.refreshPositionAndAngles(
				spawnPos.getX() + 0.5D,
				spawnPos.getY(),
				spawnPos.getZ() + 0.5D,
				world.getRandom().nextFloat() * 360.0F,
				0.0F
		);
		ally.setCustomName(Text.literal(SUPPORT_NAME));
		ally.setCustomNameVisible(true);
		ally.addCommandTag(SUPPORT_TAG);
		ally.addCommandTag(slotTag(slot));
		ally.setPlayerCreated(true);
		ally.setPersistent();
		ally.setPositionTarget(spawnPos, POSITION_TARGET_RANGE);

		if (!world.isSpaceEmpty(ally) || !world.spawnEntity(ally)) {
			MineFluence.LOGGER.warn(
					"[InvasionSupport] Slot {} skipped: entity spawn was rejected at {}.",
					slot,
					spawnPos.toShortString()
			);
			return false;
		}
		MineFluence.LOGGER.info(
				"[InvasionSupport] Spawned slot {} at {} with tag {}.",
				slot,
				spawnPos.toShortString(),
				SUPPORT_TAG
		);

		world.spawnParticles(
				ParticleTypes.HAPPY_VILLAGER,
				ally.getX(),
				ally.getBodyY(0.5D),
				ally.getZ(),
				8,
				0.7D,
				0.8D,
				0.7D,
				0.0D
		);
		return true;
	}

	private static BlockPos findSafeSpawnPos(ServerWorld world, IronGolemEntity ally, BlockPos configuredPos) {
		for (int radius = 0; radius <= SPAWN_HORIZONTAL_SEARCH; radius++) {
			for (int offsetX = -radius; offsetX <= radius; offsetX++) {
				for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
					if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) {
						continue;
					}
					int x = configuredPos.getX() + offsetX;
					int z = configuredPos.getZ() + offsetZ;
					for (int distance = 0; distance <= SPAWN_VERTICAL_SEARCH; distance++) {
						BlockPos above = new BlockPos(x, configuredPos.getY() + distance, z);
						if (isSafeSpawnPos(world, ally, above)) {
							return above;
						}
						if (distance == 0) {
							continue;
						}

						BlockPos below = new BlockPos(x, configuredPos.getY() - distance, z);
						if (isSafeSpawnPos(world, ally, below)) {
							return below;
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isSafeSpawnPos(ServerWorld world, IronGolemEntity ally, BlockPos pos) {
		BlockPos floorPos = pos.down();
		BlockState floorState = world.getBlockState(floorPos);
		if (!floorState.isSideSolidFullSquare(world, floorPos, Direction.UP)) {
			return false;
		}

		for (int height = 0; height <= 2; height++) {
			BlockPos bodyPos = pos.up(height);
			BlockState bodyState = world.getBlockState(bodyPos);
			if (!bodyState.getCollisionShape(world, bodyPos).isEmpty()
					|| !bodyState.getFluidState().isEmpty()) {
				return false;
			}
		}

		ally.refreshPositionAndAngles(
				pos.getX() + 0.5D,
				pos.getY(),
				pos.getZ() + 0.5D,
				0.0F,
				0.0F
		);
		return world.isSpaceEmpty(ally)
				&& world.getOtherEntities(ally, ally.getBoundingBox().expand(0.2D), Entity::isAlive).isEmpty();
	}

	private static int slotIndex(IronGolemEntity ally) {
		for (String tag : ally.getCommandTags()) {
			if (!tag.startsWith(SUPPORT_SLOT_TAG_PREFIX)) {
				continue;
			}
			try {
				return Integer.parseInt(tag.substring(SUPPORT_SLOT_TAG_PREFIX.length()));
			} catch (NumberFormatException ignored) {
				return Integer.MAX_VALUE;
			}
		}
		return Integer.MAX_VALUE;
	}

	private static String slotTag(int slot) {
		return SUPPORT_SLOT_TAG_PREFIX + slot;
	}

	public record SpawnResult(int targetCount, int spawnedCount) {
	}

	public record SyncResult(int currentCount, int targetCount, int spawnedCount, int removedCount) {
	}
}
