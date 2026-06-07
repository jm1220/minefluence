package net.jeongmin.modid.area;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class MineFluenceAreaGuideManager {
	private static final int GUIDE_TICK_INTERVAL = 20;
	private static final int DEBUG_GUIDE_DURATION_TICKS = 100;
	private static final int BOUNDARY_STEP_BLOCKS = 1;

	private static final Map<UUID, DebugGuide> DEBUG_GUIDES = new HashMap<>();

	private MineFluenceAreaGuideManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceAreaGuideManager::tick);
	}

	public static MineFluenceAreaType requiredAreaForMission(int missionIndex, MineFluenceMissionRoute route) {
		if (route != MineFluenceMissionRoute.GOOD) {
			return null;
		}

		return switch (missionIndex) {
			case 1 -> MineFluenceAreaType.GARDEN;
			case 2 -> MineFluenceAreaType.FARM;
			case 6 -> MineFluenceAreaType.SHARED_SPACE;
			case 7 -> MineFluenceAreaType.FARM_BUILD_AREA;
			default -> null;
		};
	}

	public static void sendMissionAreaHint(ServerPlayerEntity player, int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceAreaType areaType = requiredAreaForMission(missionIndex, route);
		if (areaType == null) {
			return;
		}

		MineFluenceDisplay.sendChat(player, "Go to the " + areaType.displayName() + " area.");
		MineFluenceDisplay.sendChat(player, "The mission area is highlighted with particles.");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Go to the " + areaType.displayName() + " area");
		renderAreaOnce(player, areaType);
	}

	public static String requiredAreaProgressLine(ServerPlayerEntity player, int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceAreaType areaType = requiredAreaForMission(missionIndex, route);
		if (areaType == null) {
			return "";
		}

		MineFluenceArea area = MineFluenceWorldState.get(player.getServer()).getArea(areaType);
		if (area == null) {
			return "Required area: " + areaType.displayName() + " (missing).";
		}
		return "Required area: " + areaType.displayName() + " " + area.describe() + ".";
	}

	public static boolean showArea(ServerPlayerEntity player, MineFluenceAreaType type) {
		MineFluenceArea area = MineFluenceWorldState.get(player.getServer()).getArea(type);
		if (area == null) {
			MineFluenceDisplay.sendChat(player, type.displayName() + " area is not configured.");
			return false;
		}

		ServerWorld world = findWorld(player.getServer(), area.dimensionId());
		if (world == null) {
			MineFluenceDisplay.sendChat(player, type.displayName() + " area is configured in an unavailable dimension: " + area.dimensionId() + ".");
			return false;
		}

		DEBUG_GUIDES.put(player.getUuid(), new DebugGuide(type, player.getServer().getTicks() + DEBUG_GUIDE_DURATION_TICKS));
		renderArea(world, area);
		MineFluenceDisplay.sendChat(player, "Showing " + type.displayName() + " area guide: " + area.describe() + ".");
		return true;
	}

	private static void renderAreaOnce(ServerPlayerEntity player, MineFluenceAreaType type) {
		MineFluenceArea area = MineFluenceWorldState.get(player.getServer()).getArea(type);
		if (area == null) {
			return;
		}

		ServerWorld world = findWorld(player.getServer(), area.dimensionId());
		if (world != null) {
			renderArea(world, area);
		}
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % GUIDE_TICK_INTERVAL != 0) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(server);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			renderActiveMissionGuide(server, state, player);
			renderDebugGuide(server, state, player);
		}
		cleanupExpiredDebugGuides(server.getTicks());
	}

	private static void renderActiveMissionGuide(MinecraftServer server, MineFluenceWorldState state, ServerPlayerEntity player) {
		MineFluencePlayerData data = state.getPlayerData(player);
		if (!data.hasActiveMission() || data.isWaitingForPostingChoice()) {
			return;
		}

		MineFluenceAreaType areaType = requiredAreaForMission(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (areaType == null) {
			return;
		}

		MineFluenceArea area = state.getArea(areaType);
		if (area == null) {
			return;
		}

		ServerWorld world = findWorld(server, area.dimensionId());
		if (world != null) {
			renderArea(world, area);
		}
	}

	private static void renderDebugGuide(MinecraftServer server, MineFluenceWorldState state, ServerPlayerEntity player) {
		DebugGuide debugGuide = DEBUG_GUIDES.get(player.getUuid());
		if (debugGuide == null || debugGuide.expiresAtTick() < server.getTicks()) {
			return;
		}

		MineFluenceArea area = state.getArea(debugGuide.type());
		if (area == null) {
			return;
		}

		ServerWorld world = findWorld(server, area.dimensionId());
		if (world != null) {
			renderArea(world, area);
		}
	}

	private static void cleanupExpiredDebugGuides(int currentTick) {
		Iterator<Map.Entry<UUID, DebugGuide>> iterator = DEBUG_GUIDES.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue().expiresAtTick() < currentTick) {
				iterator.remove();
			}
		}
	}

	private static void renderArea(ServerWorld world, MineFluenceArea area) {
		int minX = area.min().getX();
		int maxX = area.max().getX();
		int minZ = area.min().getZ();
		int maxZ = area.max().getZ();
		double y = area.center().getY() + 0.15D;

		for (int x = minX; x <= maxX; x += BOUNDARY_STEP_BLOCKS) {
			spawnGuideParticle(world, x + 0.5D, y, minZ + 0.5D);
			spawnGuideParticle(world, x + 0.5D, y, maxZ + 0.5D);
		}
		for (int z = minZ; z <= maxZ; z += BOUNDARY_STEP_BLOCKS) {
			spawnGuideParticle(world, minX + 0.5D, y, z + 0.5D);
			spawnGuideParticle(world, maxX + 0.5D, y, z + 0.5D);
		}

		BlockPos center = area.center();
		for (int offsetY = 0; offsetY <= 4; offsetY++) {
			world.spawnParticles(
					ParticleTypes.END_ROD,
					center.getX() + 0.5D,
					y + offsetY,
					center.getZ() + 0.5D,
					1,
					0.0D,
					0.0D,
					0.0D,
					0.0D
			);
		}
	}

	private static void spawnGuideParticle(ServerWorld world, double x, double y, double z) {
		world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	private static ServerWorld findWorld(MinecraftServer server, String dimensionId) {
		for (ServerWorld world : server.getWorlds()) {
			if (world.getRegistryKey().getValue().toString().equals(dimensionId)) {
				return world;
			}
		}
		return null;
	}

	private record DebugGuide(MineFluenceAreaType type, int expiresAtTick) {
	}
}
