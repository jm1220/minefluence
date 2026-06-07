package net.jeongmin.modid.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ending.MineFluenceEndingManager;
import net.jeongmin.modid.entity.DdjEntity;
import net.jeongmin.modid.entity.MineFluenceEntities;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public final class MineFluenceInvasionManager {
	public enum InvasionStrength {
		WEAK,
		MEDIUM,
		STRONG
	}

	private static final String INVADER_TAG = "minefluence_invasion";
	private static final String INVADER_NAME = "MineFluence Invader";

	private MineFluenceInvasionManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceInvasionManager::tick);
	}

	public static boolean startInvasion(ServerPlayerEntity player, MineFluencePlayerData data, int invasionIndex) {
		if (data.hasActiveInvasion()) {
			MineFluenceDisplay.sendChat(player, "Invasion " + data.getActiveInvasionIndex() + " is already active. Cannot start another invasion.");
			return false;
		}
		if (invasionIndex < 1 || invasionIndex > 3) {
			MineFluenceDisplay.sendChat(player, "Invalid invasion index: " + invasionIndex + ".");
			return false;
		}

		InvasionStrength strength = determineStrength(invasionIndex, data.getSocialCredibility());
		int mobCount = getMobCountForStrength(strength);
		List<UUID> spawnedMobUuids = spawnFarmerInvasionMobs(player, mobCount);
		if (spawnedMobUuids.isEmpty()) {
			MineFluenceDisplay.sendChat(player, "Invasion " + invasionIndex + " could not start because no DDJ moles could be spawned.");
			return false;
		}

		data.startInvasion(invasionIndex, spawnedMobUuids, player.getServer().getTicks());
		MineFluenceWorldState.get(player.getServer()).markDirty();
		MineFluenceDisplay.sendChat(player, "Invasion " + invasionIndex + " started! Strength: " + strength + ". DDJ moles: " + spawnedMobUuids.size() + ".");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Invasion " + invasionIndex + ": Invaders remaining " + spawnedMobUuids.size());
		MineFluenceHud.refresh(player, data);
		return true;
	}

	public static InvasionStrength determineStrength(int invasionIndex, int socialCredibility) {
		return switch (invasionIndex) {
			case 1 -> determineStrength(socialCredibility, MineFluenceBalance.INVASION_1_WEAK_SOCIAL_THRESHOLD, MineFluenceBalance.INVASION_1_STRONG_SOCIAL_THRESHOLD);
			case 2 -> determineStrength(socialCredibility, MineFluenceBalance.INVASION_2_WEAK_SOCIAL_THRESHOLD, MineFluenceBalance.INVASION_2_STRONG_SOCIAL_THRESHOLD);
			case 3 -> determineStrength(socialCredibility, MineFluenceBalance.INVASION_3_WEAK_SOCIAL_THRESHOLD, MineFluenceBalance.INVASION_3_STRONG_SOCIAL_THRESHOLD);
			default -> InvasionStrength.MEDIUM;
		};
	}

	public static int getMobCountForStrength(InvasionStrength strength) {
		return switch (strength) {
			case WEAK -> MineFluenceBalance.INVASION_WEAK_ZOMBIE_COUNT;
			case MEDIUM -> MineFluenceBalance.INVASION_MEDIUM_ZOMBIE_COUNT;
			case STRONG -> MineFluenceBalance.INVASION_STRONG_ZOMBIE_COUNT;
		};
	}

	public static int countRemainingTrackedMobs(MinecraftServer server, MineFluencePlayerData data) {
		int remaining = 0;
		for (UUID uuid : data.getActiveInvasionMobUuids()) {
			Entity entity = findEntity(server, uuid);
			if (isRemainingInvader(entity)) {
				remaining++;
			}
		}
		return remaining;
	}

	public static void stopInvasionDebug(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (!data.hasActiveInvasion()) {
			MineFluenceDisplay.sendChat(player, "No active invasion to stop.");
			return;
		}

		removeTrackedMobs(player.getServer(), data);
		data.clearInvasionState();
		MineFluenceWorldState.get(player.getServer()).markDirty();
		MineFluenceDisplay.sendChat(player, "Active invasion stopped and tracked mobs were removed.");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Invasion stopped");
		MineFluenceHud.refresh(player, data);
	}

	public static void clearInvasionForReset(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (data.hasActiveInvasion() || data.getTrackedInvasionMobCount() > 0) {
			removeTrackedMobs(player.getServer(), data);
		}
		data.clearInvasionState();
	}

	private static InvasionStrength determineStrength(int socialCredibility, int weakThreshold, int strongThreshold) {
		if (socialCredibility >= weakThreshold) {
			return InvasionStrength.WEAK;
		}
		if (socialCredibility <= strongThreshold) {
			return InvasionStrength.STRONG;
		}
		return InvasionStrength.MEDIUM;
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % MineFluenceBalance.INVASION_TICK_FEEDBACK_INTERVAL != 0) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(server);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			MineFluencePlayerData data = state.getPlayerData(player);
			if (!data.hasActiveInvasion()) {
				continue;
			}

			if (player.isDead() || !player.isAlive()) {
				failInvasion(player, data);
				continue;
			}

			List<UUID> remainingMobUuids = getRemainingMobUuids(server, data);
			if (remainingMobUuids.size() != data.getTrackedInvasionMobCount()) {
				data.setActiveInvasionMobUuids(remainingMobUuids);
				state.markDirty();
			}

			if (remainingMobUuids.isEmpty()) {
				completeInvasion(player, data);
				continue;
			}

			MineFluenceDisplay.sendActionBar(player, "[MineFluence] Invasion " + data.getActiveInvasionIndex() + ": Invaders remaining " + remainingMobUuids.size());
			MineFluenceHud.refresh(player, data);
		}
	}

	private static List<UUID> spawnFarmerInvasionMobs(ServerPlayerEntity player, int mobCount) {
		List<UUID> spawnedMobUuids = new ArrayList<>();
		ServerWorld world = player.getServerWorld();
		Random random = player.getRandom();

		for (int mobIndex = 0; mobIndex < mobCount; mobIndex++) {
			DdjEntity ddj = null;
			for (int attempt = 0; attempt < MineFluenceBalance.INVASION_MAX_SPAWN_ATTEMPTS && ddj == null; attempt++) {
				int offsetX = randomOffset(random);
				int offsetZ = randomOffset(random);
				if (offsetX == 0 && offsetZ == 0) {
					offsetX = MineFluenceBalance.INVASION_SPAWN_RADIUS_BLOCKS;
				}

				int x = player.getBlockPos().getX() + offsetX;
				int z = player.getBlockPos().getZ() + offsetZ;
				int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
				ddj = spawnDdjAt(world, player, x + 0.5D, y, z + 0.5D, random.nextFloat() * 360.0F);
			}

			if (ddj == null) {
				ddj = spawnDdjAt(world, player, player.getX() + mobIndex + 2.0D, player.getY(), player.getZ() + 2.0D, random.nextFloat() * 360.0F);
			}

			if (ddj != null) {
				spawnedMobUuids.add(ddj.getUuid());
			}
		}

		return spawnedMobUuids;
	}

	private static DdjEntity spawnDdjAt(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, float yaw) {
		DdjEntity ddj = MineFluenceEntities.DDJ.create(world);
		if (ddj == null) {
			return null;
		}

		ddj.refreshPositionAndAngles(x, y, z, yaw, 0.0F);
		ddj.setCustomName(Text.literal(INVADER_NAME));
		ddj.setCustomNameVisible(true);
		ddj.addCommandTag(INVADER_TAG);
		ddj.setPersistent();
		ddj.setTarget(player);

		if (!world.spawnEntity(ddj)) {
			return null;
		}
		return ddj;
	}

	private static int randomOffset(Random random) {
		int radius = MineFluenceBalance.INVASION_SPAWN_RADIUS_BLOCKS;
		return random.nextBetween(-radius, radius);
	}

	private static List<UUID> getRemainingMobUuids(MinecraftServer server, MineFluencePlayerData data) {
		List<UUID> remainingMobUuids = new ArrayList<>();
		for (UUID uuid : data.getActiveInvasionMobUuids()) {
			Entity entity = findEntity(server, uuid);
			if (isRemainingInvader(entity)) {
				remainingMobUuids.add(uuid);
			}
		}
		return remainingMobUuids;
	}

	private static boolean isRemainingInvader(Entity entity) {
		return entity != null && !entity.isRemoved() && entity.isAlive();
	}

	private static void completeInvasion(ServerPlayerEntity player, MineFluencePlayerData data) {
		int invasionIndex = data.getActiveInvasionIndex();
		data.setLastCompletedInvasionIndex(invasionIndex);
		data.clearInvasionState();
		MineFluenceWorldState.get(player.getServer()).markDirty();

		MineFluenceDisplay.sendChat(player, "Invasion " + invasionIndex + " cleared!");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Invasion " + invasionIndex + " cleared");
		MineFluenceHud.refresh(player, data);
		if (invasionIndex == 3) {
			MineFluenceEndingManager.triggerEndingIfReady(player, data);
		}
	}

	private static void failInvasion(ServerPlayerEntity player, MineFluencePlayerData data) {
		removeTrackedMobs(player.getServer(), data);
		data.clearInvasionState();
		MineFluenceWorldState.get(player.getServer()).markDirty();
		MineFluenceDisplay.sendChat(player, "Invasion failed. The village could not be defended.");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Invasion failed");
		MineFluenceHud.refresh(player, data);
	}

	private static void removeTrackedMobs(MinecraftServer server, MineFluencePlayerData data) {
		for (UUID uuid : data.getActiveInvasionMobUuids()) {
			Entity entity = findEntity(server, uuid);
			if (entity != null && !entity.isRemoved()) {
				entity.discard();
			}
		}
	}

	private static Entity findEntity(MinecraftServer server, UUID uuid) {
		for (ServerWorld world : server.getWorlds()) {
			Entity entity = world.getEntity(uuid);
			if (entity != null) {
				return entity;
			}
		}
		return null;
	}
}
