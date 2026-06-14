package net.jeongmin.modid.invasion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import net.minecraft.particle.ParticleTypes;
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
	private static final int POSITION_TARGET_RANGE = 24;
	private static final int[][] HORIZONTAL_OFFSETS = {
			{0, 0},
			{1, 0},
			{-1, 0},
			{0, 1},
			{0, -1},
			{1, 1},
			{1, -1},
			{-1, 1},
			{-1, -1},
			{2, 0},
			{-2, 0},
			{0, 2},
			{0, -2}
	};

	private MineFluenceInvasionSupportManager() {
	}

	public static void register() {
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
		SpawnResult result = spawnTargetCount(player, MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility()));
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
		return spawnTargetCount(player, MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility()));
	}

	public static SyncResult syncSupportAllies(ServerPlayerEntity player, MineFluencePlayerData data) {
		int targetCount = MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility());
		ServerWorld world = player.getServer().getOverworld();
		List<IronGolemEntity> allies = findLivingSupportAllies(world);
		int removed = removeExtraAllies(allies, targetCount);

		allies = findLivingSupportAllies(world);
		int spawned = spawnMissingAllies(world, allies, targetCount);
		return new SyncResult(findLivingSupportAllies(world).size(), targetCount, spawned, removed);
	}

	public static int clearSupportAllies(ServerPlayerEntity player) {
		List<IronGolemEntity> allies = findTaggedSupportAllies(player.getServer().getOverworld());
		allies.forEach(Entity::discard);
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
		if (invaders.isEmpty()) {
			return;
		}

		for (IronGolemEntity ally : allies) {
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

	private static SpawnResult spawnTargetCount(ServerPlayerEntity player, int targetCount) {
		ServerWorld world = player.getServer().getOverworld();
		int spawned = spawnMissingAllies(world, List.of(), targetCount);
		return new SpawnResult(targetCount, spawned);
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
			return false;
		}

		BlockPos spawnPos = findSafeSpawnPos(world, ally, configuredPos);
		if (spawnPos == null) {
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
			return false;
		}

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
		for (int[] offset : HORIZONTAL_OFFSETS) {
			int x = configuredPos.getX() + offset[0];
			int z = configuredPos.getZ() + offset[1];
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
