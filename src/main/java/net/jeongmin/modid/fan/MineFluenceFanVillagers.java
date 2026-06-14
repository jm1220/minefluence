package net.jeongmin.modid.fan;

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
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public final class MineFluenceFanVillagers {
	public static final String FAN_TAG = "minefluence_fan";

	private static final String FAN_SLOT_TAG_PREFIX = "minefluence_fan_slot_";
	private static final String FAN_NAME = "MineFluence Fan";
	private static final int SEARCH_RADIUS = 80;
	private static final int SPAWN_VERTICAL_SEARCH = 8;
	private static final int POSITION_TARGET_RANGE = 8;
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

	private MineFluenceFanVillagers() {
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				syncFanVillagers(handler.player)
		);
	}

	public static SyncResult syncFanVillagers(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).getPlayerData(player);
		int targetCount = MineFluenceBalance.getTargetFanCount(data.getFollower());
		ServerWorld world = player.getServer().getOverworld();
		List<VillagerEntity> fans = findFans(world);
		int removed = removeExtraFans(fans, targetCount);

		fans = findFans(world);
		int spawned = spawnMissingFans(world, fans, targetCount);
		int currentCount = findFans(world).size();
		if (spawned > 0) {
			MineFluenceDisplay.sendChat(player, "New fans have arrived in the village.");
		}
		return new SyncResult(currentCount, targetCount, spawned, removed);
	}

	public static int clearFanVillagers(ServerPlayerEntity player) {
		return clearFanVillagers(player.getServer().getOverworld());
	}

	public static int countFanVillagers(ServerPlayerEntity player) {
		return findFans(player.getServer().getOverworld()).size();
	}

	public static boolean isFan(Entity entity) {
		return entity != null && entity.getCommandTags().contains(FAN_TAG);
	}

	private static int clearFanVillagers(ServerWorld world) {
		List<VillagerEntity> fans = findTaggedFans(world);
		fans.forEach(Entity::discard);
		return fans.size();
	}

	private static List<VillagerEntity> findFans(ServerWorld world) {
		List<VillagerEntity> fans = findTaggedFans(world);
		fans.removeIf(villager -> !villager.isAlive());
		return fans;
	}

	private static List<VillagerEntity> findTaggedFans(ServerWorld world) {
		BlockPos center = MineFluenceDemoMapPreset.fanVillageCenter();
		Box searchBox = new Box(center).expand(SEARCH_RADIUS);
		return new ArrayList<>(world.getEntitiesByClass(
				VillagerEntity.class,
				searchBox,
				villager -> isFan(villager) && !villager.isRemoved()
		));
	}

	private static int removeExtraFans(List<VillagerEntity> fans, int targetCount) {
		if (fans.size() <= targetCount) {
			return 0;
		}

		fans.sort(Comparator.comparingInt(MineFluenceFanVillagers::slotIndex));
		int removed = 0;
		for (int index = fans.size() - 1; index >= targetCount; index--) {
			fans.get(index).discard();
			removed++;
		}
		return removed;
	}

	private static int spawnMissingFans(ServerWorld world, List<VillagerEntity> existingFans, int targetCount) {
		int missing = targetCount - existingFans.size();
		if (missing <= 0) {
			return 0;
		}

		Set<Integer> occupiedSlots = new HashSet<>();
		for (VillagerEntity fan : existingFans) {
			int slot = slotIndex(fan);
			if (slot >= 0) {
				occupiedSlots.add(slot);
			}
		}

		int spawned = 0;
		List<BlockPos> spawnPoints = MineFluenceDemoMapPreset.fanSpawnPoints();
		for (int slot = 0; slot < spawnPoints.size() && spawned < missing; slot++) {
			if (occupiedSlots.contains(slot)) {
				continue;
			}
			if (spawnFan(world, spawnPoints.get(slot), slot)) {
				spawned++;
				occupiedSlots.add(slot);
			}
		}
		return spawned;
	}

	private static boolean spawnFan(ServerWorld world, BlockPos configuredPos, int slot) {
		VillagerEntity fan = EntityType.VILLAGER.create(world);
		if (fan == null) {
			return false;
		}

		BlockPos spawnPos = findSafeSpawnPos(world, fan, configuredPos);
		if (spawnPos == null) {
			return false;
		}

		fan.refreshPositionAndAngles(
				spawnPos.getX() + 0.5D,
				spawnPos.getY(),
				spawnPos.getZ() + 0.5D,
				world.getRandom().nextFloat() * 360.0F,
				0.0F
		);
		fan.setCustomName(Text.literal(FAN_NAME));
		fan.setCustomNameVisible(true);
		fan.addCommandTag(FAN_TAG);
		fan.addCommandTag(slotTag(slot));
		fan.setPersistent();
		fan.setPositionTarget(spawnPos, POSITION_TARGET_RANGE);

		if (!world.isSpaceEmpty(fan) || !world.spawnEntity(fan)) {
			return false;
		}

		world.spawnParticles(
				ParticleTypes.HAPPY_VILLAGER,
				fan.getX(),
				fan.getBodyY(0.5D),
				fan.getZ(),
				6,
				0.4D,
				0.5D,
				0.4D,
				0.0D
		);
		return true;
	}

	private static BlockPos findSafeSpawnPos(ServerWorld world, VillagerEntity fan, BlockPos configuredPos) {
		for (int[] offset : HORIZONTAL_OFFSETS) {
			int x = configuredPos.getX() + offset[0];
			int z = configuredPos.getZ() + offset[1];
			for (int distance = 0; distance <= SPAWN_VERTICAL_SEARCH; distance++) {
				BlockPos above = new BlockPos(x, configuredPos.getY() + distance, z);
				if (isSafeSpawnPos(world, fan, above)) {
					return above;
				}
				if (distance == 0) {
					continue;
				}

				BlockPos below = new BlockPos(x, configuredPos.getY() - distance, z);
				if (isSafeSpawnPos(world, fan, below)) {
					return below;
				}
			}
		}
		return null;
	}

	private static boolean isSafeSpawnPos(ServerWorld world, VillagerEntity fan, BlockPos pos) {
		BlockPos floorPos = pos.down();
		BlockState floorState = world.getBlockState(floorPos);
		BlockState feetState = world.getBlockState(pos);
		BlockState headState = world.getBlockState(pos.up());
		if (!floorState.isSideSolidFullSquare(world, floorPos, Direction.UP)
				|| !feetState.getCollisionShape(world, pos).isEmpty()
				|| !headState.getCollisionShape(world, pos.up()).isEmpty()
				|| !feetState.getFluidState().isEmpty()
				|| !headState.getFluidState().isEmpty()) {
			return false;
		}

		fan.refreshPositionAndAngles(
				pos.getX() + 0.5D,
				pos.getY(),
				pos.getZ() + 0.5D,
				0.0F,
				0.0F
		);
		return world.isSpaceEmpty(fan)
				&& world.getOtherEntities(fan, fan.getBoundingBox().expand(0.2D), Entity::isAlive).isEmpty();
	}

	private static int slotIndex(VillagerEntity villager) {
		for (String tag : villager.getCommandTags()) {
			if (!tag.startsWith(FAN_SLOT_TAG_PREFIX)) {
				continue;
			}
			try {
				return Integer.parseInt(tag.substring(FAN_SLOT_TAG_PREFIX.length()));
			} catch (NumberFormatException ignored) {
				return Integer.MAX_VALUE;
			}
		}
		return Integer.MAX_VALUE;
	}

	private static String slotTag(int slot) {
		return FAN_SLOT_TAG_PREFIX + slot;
	}

	public record SyncResult(int currentCount, int targetCount, int spawnedCount, int removedCount) {
	}
}
