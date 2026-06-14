package net.jeongmin.modid.area;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class MineFluenceAreaGuideManager {
	private static final int GUIDE_TICK_INTERVAL = 10;
	private static final int GUIDE_DURATION_TICKS = 100;
	private static final int BOUNDARY_STEP_BLOCKS = 2;
	private static final int CENTER_MARKER_HEIGHT = 4;

	private static final Map<UUID, ActiveGuide> ACTIVE_GUIDES = new HashMap<>();

	private MineFluenceAreaGuideManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceAreaGuideManager::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE_GUIDES.clear());
	}

	public static MineFluenceAreaType requiredAreaForMission(int missionIndex, MineFluenceMissionRoute route) {
		return MineFluenceMissionAreas.getAreaForMission(route, missionIndex);
	}

	public static void sendMissionAreaHint(ServerPlayerEntity player, int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceAreaType areaType = requiredAreaForMission(missionIndex, route);
		if (areaType == null) {
			return;
		}
		if (!startGuide(player, areaType, route)) {
			return;
		}

		MineFluenceDisplay.sendChat(player, "Follow the particles to the mission area.");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Mission area: " + areaType.commandName());
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
		if (!startGuide(player, type, MineFluenceMissionRoute.NONE)) {
			return false;
		}

		MineFluenceArea area = MineFluenceWorldState.get(player.getServer()).getArea(type);
		MineFluenceDisplay.sendChat(player, "Showing area " + type.commandName() + ": " + area.describe() + ".");
		return true;
	}

	public static boolean showMissionArea(
			ServerPlayerEntity player,
			MineFluenceAreaType type,
			MineFluenceMissionRoute route
	) {
		if (!startGuide(player, type, route)) {
			return false;
		}

		MineFluenceDisplay.sendChat(player, "Showing mission area: " + type.commandName() + ".");
		return true;
	}

	private static boolean startGuide(
			ServerPlayerEntity player,
			MineFluenceAreaType type,
			MineFluenceMissionRoute route
	) {
		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluenceArea area = state.getArea(type);
		if (area == null) {
			int loaded = MineFluenceDemoMapPreset.loadInto(state, false);
			area = state.getArea(type);
			if (loaded > 0) {
				MineFluenceDisplay.sendChat(player, "Area preset was missing and has been restored.");
			}
		}
		if (area == null) {
			MineFluenceDisplay.sendChat(player, "Area preset is missing. Run /minefluence area load_preset.");
			return false;
		}

		ServerWorld world = findWorld(player.getServer(), area.dimensionId());
		if (world == null) {
			MineFluenceDisplay.sendChat(player, type.displayName() + " area is configured in an unavailable dimension: " + area.dimensionId() + ".");
			return false;
		}

		MineFluenceMissionRoute safeRoute = route == null ? MineFluenceMissionRoute.NONE : route;
		ACTIVE_GUIDES.put(
				player.getUuid(),
				new ActiveGuide(type, safeRoute, player.getServer().getTicks() + GUIDE_DURATION_TICKS)
		);
		renderArea(world, area, safeRoute);
		return true;
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % GUIDE_TICK_INTERVAL != 0) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(server);
		Iterator<Map.Entry<UUID, ActiveGuide>> iterator = ACTIVE_GUIDES.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, ActiveGuide> entry = iterator.next();
			ActiveGuide guide = entry.getValue();
			if (guide.expiresAtTick() <= server.getTicks()) {
				iterator.remove();
				continue;
			}

			MineFluenceArea area = state.getArea(guide.type());
			if (area == null) {
				iterator.remove();
				continue;
			}

			ServerWorld world = findWorld(server, area.dimensionId());
			if (world == null) {
				iterator.remove();
				continue;
			}
			renderArea(world, area, guide.route());
		}
	}

	private static void renderArea(ServerWorld world, MineFluenceArea area, MineFluenceMissionRoute route) {
		int minX = area.min().getX();
		int maxX = area.max().getX();
		int minZ = area.min().getZ();
		int maxZ = area.max().getZ();
		double y = findDisplayY(world, area);
		ParticleEffect boundaryParticle = boundaryParticle(route);

		for (int x = minX; x <= maxX; x += BOUNDARY_STEP_BLOCKS) {
			spawnParticle(world, boundaryParticle, x + 0.5D, y, minZ + 0.5D);
			spawnParticle(world, boundaryParticle, x + 0.5D, y, maxZ + 0.5D);
		}
		spawnParticle(world, boundaryParticle, maxX + 0.5D, y, minZ + 0.5D);
		spawnParticle(world, boundaryParticle, maxX + 0.5D, y, maxZ + 0.5D);

		for (int z = minZ; z <= maxZ; z += BOUNDARY_STEP_BLOCKS) {
			spawnParticle(world, boundaryParticle, minX + 0.5D, y, z + 0.5D);
			spawnParticle(world, boundaryParticle, maxX + 0.5D, y, z + 0.5D);
		}
		spawnParticle(world, boundaryParticle, minX + 0.5D, y, maxZ + 0.5D);
		spawnParticle(world, boundaryParticle, maxX + 0.5D, y, maxZ + 0.5D);

		BlockPos center = area.center();
		for (int offsetY = 0; offsetY <= CENTER_MARKER_HEIGHT; offsetY++) {
			spawnParticle(
					world,
					ParticleTypes.END_ROD,
					center.getX() + 0.5D,
					y + offsetY,
					center.getZ() + 0.5D
			);
		}
	}

	private static double findDisplayY(ServerWorld world, MineFluenceArea area) {
		BlockPos center = area.center();
		int preferredY = Math.max(area.min().getY() + 1, Math.min(center.getY(), area.max().getY()));
		int maxDistance = Math.max(1, area.max().getY() - area.min().getY());
		for (int distance = 0; distance <= maxDistance; distance++) {
			int aboveY = preferredY + distance;
			if (aboveY <= area.max().getY() && isOpenAboveGround(world, center.getX(), aboveY, center.getZ())) {
				return aboveY + 0.15D;
			}
			if (distance == 0) {
				continue;
			}

			int belowY = preferredY - distance;
			if (belowY >= area.min().getY() + 1 && isOpenAboveGround(world, center.getX(), belowY, center.getZ())) {
				return belowY + 0.15D;
			}
		}
		return area.min().getY() + 1.15D;
	}

	private static boolean isOpenAboveGround(ServerWorld world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		BlockPos floorPos = pos.down();
		BlockState floor = world.getBlockState(floorPos);
		BlockState body = world.getBlockState(pos);
		return floor.isSideSolidFullSquare(world, floorPos, Direction.UP)
				&& body.getCollisionShape(world, pos).isEmpty()
				&& body.getFluidState().isEmpty();
	}

	private static ParticleEffect boundaryParticle(MineFluenceMissionRoute route) {
		return switch (route) {
			case GOOD -> ParticleTypes.HAPPY_VILLAGER;
			case BAD -> ParticleTypes.ANGRY_VILLAGER;
			case NONE -> ParticleTypes.END_ROD;
		};
	}

	private static void spawnParticle(ServerWorld world, ParticleEffect particle, double x, double y, double z) {
		world.spawnParticles(particle, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	private static ServerWorld findWorld(MinecraftServer server, String dimensionId) {
		for (ServerWorld world : server.getWorlds()) {
			if (world.getRegistryKey().getValue().toString().equals(dimensionId)) {
				return world;
			}
		}
		return null;
	}

	private record ActiveGuide(
			MineFluenceAreaType type,
			MineFluenceMissionRoute route,
			int expiresAtTick
	) {
	}
}
