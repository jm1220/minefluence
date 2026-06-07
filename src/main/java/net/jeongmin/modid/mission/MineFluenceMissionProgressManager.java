package net.jeongmin.modid.mission;

import java.util.function.Predicate;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.jeongmin.modid.area.MineFluenceArea;
import net.jeongmin.modid.area.MineFluenceAreaType;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

public final class MineFluenceMissionProgressManager {
	private MineFluenceMissionProgressManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceMissionProgressManager::tick);
		UseEntityCallback.EVENT.register(MineFluenceMissionProgressManager::onUseEntity);
	}

	public static boolean canStartMission(ServerPlayerEntity player, MineFluenceMission mission) {
		if (mission.route() == MineFluenceMissionRoute.BAD) {
			return true;
		}

		MineFluenceAreaType requiredAreaType = requiredAreaType(mission.index());
		if (requiredAreaType == null) {
			return true;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluenceArea area = state.getArea(requiredAreaType);
		if (area == null) {
			MineFluenceDisplay.sendChat(player, requiredAreaType.displayName() + " area is not configured. Use /minefluence area load_preset, /minefluence area set " + requiredAreaType.commandName() + " <radius>, or /minefluence area set_box.");
			return false;
		}
		if (findWorld(player.getServer(), area.dimensionId()) == null) {
			MineFluenceDisplay.sendChat(player, requiredAreaType.displayName() + " area is configured in an unavailable dimension: " + area.dimensionId() + ".");
			return false;
		}
		return true;
	}

	public static int baselineForMission(ServerPlayerEntity player, MineFluenceMission mission) {
		if (mission.route() == MineFluenceMissionRoute.BAD) {
			return 0;
		}

		return switch (mission.index()) {
			case 1 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.GARDEN, state -> state.isIn(BlockTags.FLOWERS));
			case 2 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.FARM, state -> state.isOf(Blocks.WHEAT));
			case 5 -> countInventoryItems(player, Items.COMPOSTER);
			case 6 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.SHARED_SPACE, state -> state.isOf(Blocks.HAY_BLOCK));
			default -> 0;
		};
	}

	public static String progressText(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (data.hasPendingMissionSelection()) {
			return "Mission " + data.getPendingMissionSelectionIndex() + " is waiting for Good/Bad selection.";
		}
		if (data.isWaitingForPostingChoice()) {
			MineFluenceMission mission = missionFor(data.getPendingPostingMissionIndex(), data.getPendingPostingMissionRoute());
			return "Mission " + mission.index() + " " + mission.route() + " is ready to post: " + mission.title() + ".";
		}
		if (!data.hasActiveMission()) {
			return "No active mission. Use /minefluence mission next.";
		}

		MineFluenceMission mission = missionFor(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (mission.route() == MineFluenceMissionRoute.BAD) {
			return "Bad mission detection is not implemented yet. Use /minefluence mission complete_debug for now.";
		}
		if (mission.index() == 7) {
			FarmPlotCounts counts = countFarmPlot(player.getServer());
			if (counts == null) {
				return mission.title() + ": Farm Build Area missing. Use /minefluence area load_preset, /minefluence area set farm_build <radius>, or /minefluence area set_box.";
			}
			return mission.title() + ": Water " + Math.min(counts.water(), MineFluenceBalance.FARMER_MISSION_7_WATER_TARGET) + "/" + MineFluenceBalance.FARMER_MISSION_7_WATER_TARGET
					+ ", Farmland " + Math.min(counts.farmland(), MineFluenceBalance.FARMER_MISSION_7_FARMLAND_TARGET) + "/" + MineFluenceBalance.FARMER_MISSION_7_FARMLAND_TARGET
					+ ", Composter " + Math.min(counts.composter(), MineFluenceBalance.FARMER_MISSION_7_COMPOSTER_TARGET) + "/" + MineFluenceBalance.FARMER_MISSION_7_COMPOSTER_TARGET;
		}

		return mission.title() + ": " + data.getActiveMissionProgress() + "/" + mission.targetProgress();
	}

	public static String publicProgressText(MineFluencePlayerData data) {
		if (data.hasPendingMissionSelection()) {
			return "Choose Good/Bad for mission " + data.getPendingMissionSelectionIndex();
		}
		if (data.isWaitingForPostingChoice()) {
			return "Ready to post mission " + data.getPendingPostingMissionIndex() + " " + data.getPendingPostingMissionRoute();
		}
		if (!data.hasActiveMission()) {
			return "No active mission";
		}

		MineFluenceMission mission = missionFor(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (mission.route() == MineFluenceMissionRoute.BAD) {
			return mission.title() + " debug-only";
		}
		return mission.title() + " " + data.getActiveMissionProgress() + "/" + mission.targetProgress();
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % MineFluenceBalance.MISSION_SCAN_TICK_INTERVAL != 0) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(server);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			MineFluencePlayerData data = state.getPlayerData(player);
			if (!data.hasActiveMission() || data.isWaitingForPostingChoice() || data.getActiveMissionRoute() != MineFluenceMissionRoute.GOOD) {
				continue;
			}

			int progress = scannedProgress(player, data);
			if (progress < 0) {
				continue;
			}

			updateProgress(player, state, data, progress);
		}
	}

	private static ActionResult onUseEntity(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, Entity entity, net.minecraft.util.hit.EntityHitResult hitResult) {
		if (world.isClient() || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayer)) {
			return ActionResult.PASS;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(serverPlayer.getServer());
		MineFluencePlayerData data = state.getPlayerData(serverPlayer);
		if (!data.hasActiveMission() || data.isWaitingForPostingChoice() || data.getActiveMissionRoute() != MineFluenceMissionRoute.GOOD || !(entity instanceof VillagerEntity villager)) {
			return ActionResult.PASS;
		}

		if (data.getActiveMissionIndex() == 3 && villager.getVillagerData().getProfession() == VillagerProfession.FARMER) {
			incrementEventProgress(serverPlayer, state, data, 1);
			return ActionResult.PASS;
		}

		if (data.getActiveMissionIndex() == 4) {
			ItemStack stack = serverPlayer.getStackInHand(hand);
			if (stack.isOf(Items.POTATO)) {
				stack.decrementUnlessCreative(1, serverPlayer);
				incrementEventProgress(serverPlayer, state, data, 1);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	private static int scannedProgress(ServerPlayerEntity player, MineFluencePlayerData data) {
		return switch (data.getActiveMissionIndex()) {
			case 1 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.GARDEN, state -> state.isIn(BlockTags.FLOWERS)) - data.getMissionBaselineValue();
			case 2 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.FARM, state -> state.isOf(Blocks.WHEAT)) - data.getMissionBaselineValue();
			case 5 -> countInventoryItems(player, Items.COMPOSTER) - data.getMissionBaselineValue();
			case 6 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.SHARED_SPACE, state -> state.isOf(Blocks.HAY_BLOCK)) - data.getMissionBaselineValue();
			case 7 -> {
				FarmPlotCounts counts = countFarmPlot(player.getServer());
				yield counts == null ? -1 : counts.progress();
			}
			default -> data.getActiveMissionProgress();
		};
	}

	private static void incrementEventProgress(ServerPlayerEntity player, MineFluenceWorldState state, MineFluencePlayerData data, int amount) {
		if (!data.hasActiveMission() || data.isWaitingForPostingChoice()) {
			return;
		}

		updateProgress(player, state, data, data.getActiveMissionProgress() + amount);
	}

	private static void updateProgress(ServerPlayerEntity player, MineFluenceWorldState state, MineFluencePlayerData data, int rawProgress) {
		MineFluenceMission mission = missionFor(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		int progress = Math.min(Math.max(0, rawProgress), mission.targetProgress());
		if (progress != data.getActiveMissionProgress()) {
			data.setActiveMissionProgress(progress);
			state.markDirty();
		}

		MineFluenceDisplay.sendActionBar(player, "[MineFluence] " + progressText(player, data));
		MineFluenceHud.refresh(player, data);
		if (progress >= mission.targetProgress()) {
			completeActiveMission(player, state, data, mission);
		}
	}

	private static void completeActiveMission(ServerPlayerEntity player, MineFluenceWorldState state, MineFluencePlayerData data, MineFluenceMission mission) {
		if (!data.hasActiveMission() || data.isWaitingForPostingChoice()) {
			return;
		}

		data.markActiveMissionReadyToPost();
		state.markDirty();
		MineFluenceDisplay.sendChat(player, "Mission objective completed!");
		MineFluenceDisplay.sendChat(player, "Choose posting style:");
		MineFluenceDisplay.sendChat(player, "/minefluence post normal");
		MineFluenceDisplay.sendChat(player, "/minefluence post exaggerate");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] " + mission.title() + " complete");
		MineFluenceHud.refresh(player, data);
	}

	private static MineFluenceAreaType requiredAreaType(int missionIndex) {
		return switch (missionIndex) {
			case 1 -> MineFluenceAreaType.GARDEN;
			case 2 -> MineFluenceAreaType.FARM;
			case 6 -> MineFluenceAreaType.SHARED_SPACE;
			case 7 -> MineFluenceAreaType.FARM_BUILD_AREA;
			default -> null;
		};
	}

	private static int countBlocksInArea(MinecraftServer server, MineFluenceAreaType type, Predicate<BlockState> matcher) {
		MineFluenceArea area = MineFluenceWorldState.get(server).getArea(type);
		if (area == null) {
			return 0;
		}

		ServerWorld world = findWorld(server, area.dimensionId());
		if (world == null) {
			return 0;
		}

		int count = 0;
		BlockPos min = area.min();
		BlockPos max = area.max();
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int y = min.getY(); y <= max.getY(); y++) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					if (matcher.test(world.getBlockState(new BlockPos(x, y, z)))) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private static FarmPlotCounts countFarmPlot(MinecraftServer server) {
		MineFluenceArea area = MineFluenceWorldState.get(server).getArea(MineFluenceAreaType.FARM_BUILD_AREA);
		if (area == null) {
			return null;
		}

		ServerWorld world = findWorld(server, area.dimensionId());
		if (world == null) {
			return null;
		}

		int water = 0;
		int farmland = 0;
		int composter = 0;
		BlockPos min = area.min();
		BlockPos max = area.max();
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int y = min.getY(); y <= max.getY(); y++) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					BlockState state = world.getBlockState(new BlockPos(x, y, z));
					if (state.isOf(Blocks.WATER)) {
						water++;
					} else if (state.isOf(Blocks.FARMLAND)) {
						farmland++;
					} else if (state.isOf(Blocks.COMPOSTER)) {
						composter++;
					}
				}
			}
		}
		return new FarmPlotCounts(water, farmland, composter);
	}

	private static int countInventoryItems(ServerPlayerEntity player, Item item) {
		PlayerInventory inventory = player.getInventory();
		int count = 0;
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (!stack.isEmpty() && stack.isOf(item)) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static ServerWorld findWorld(MinecraftServer server, String dimensionId) {
		for (ServerWorld world : server.getWorlds()) {
			if (world.getRegistryKey().getValue().toString().equals(dimensionId)) {
				return world;
			}
		}
		return null;
	}

	private static MineFluenceMission missionFor(int missionIndex, MineFluenceMissionRoute route) {
		return FarmerMissions.getMission(missionIndex, route)
				.orElseGet(() -> new MineFluenceMission(
						"unknown_" + missionIndex,
						missionIndex,
						route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route,
						"Unknown Mission",
						"No mission definition exists for this index.",
						"Reset or start the next valid Farmer mission.",
						1,
						0,
						0
				));
	}

	private record FarmPlotCounts(int water, int farmland, int composter) {
		private int progress() {
			return Math.min(water, MineFluenceBalance.FARMER_MISSION_7_WATER_TARGET)
					+ Math.min(farmland, MineFluenceBalance.FARMER_MISSION_7_FARMLAND_TARGET)
					+ Math.min(composter, MineFluenceBalance.FARMER_MISSION_7_COMPOSTER_TARGET);
		}
	}
}
