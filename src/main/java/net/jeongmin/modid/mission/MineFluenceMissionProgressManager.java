package net.jeongmin.modid.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.jeongmin.modid.area.MineFluenceArea;
import net.jeongmin.modid.area.MineFluenceAreaType;
import net.jeongmin.modid.area.MineFluenceDemoMapPreset;
import net.jeongmin.modid.area.MineFluenceMissionAreas;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.fan.MineFluenceFanVillagers;
import net.jeongmin.modid.mixin.DoubleInventoryAccessor;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
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
	private static final Map<UUID, ContainerSnapshot> OPEN_CONTAINER_SNAPSHOTS = new HashMap<>();

	private MineFluenceMissionProgressManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceMissionProgressManager::tick);
		UseEntityCallback.EVENT.register(MineFluenceMissionProgressManager::onUseEntity);
		UseBlockCallback.EVENT.register(MineFluenceMissionProgressManager::onUseBlock);
		AttackEntityCallback.EVENT.register(MineFluenceMissionProgressManager::onAttackEntity);
		PlayerBlockBreakEvents.AFTER.register(MineFluenceMissionProgressManager::onBlockBreak);
		ServerLivingEntityEvents.AFTER_DEATH.register(MineFluenceMissionProgressManager::onLivingDeath);
	}

	public static boolean hasBadGameplayDetection(int missionIndex) {
		return switch (missionIndex) {
			case 1, 2, 3, 4, 5, 6, 7 -> true;
			default -> false;
		};
	}

	public static void clearPlayerTransientState(ServerPlayerEntity player) {
		if (player != null) {
			OPEN_CONTAINER_SNAPSHOTS.remove(player.getUuid());
		}
	}

	public static void onFarmlandTrampled(Entity entity, BlockState originalState, World world, BlockPos pos) {
		if (world == null
				|| world.isClient()
				|| !(world instanceof ServerWorld serverWorld)
				|| !(entity instanceof ServerPlayerEntity serverPlayer)
				|| serverPlayer.isSpectator()) {
			return;
		}
		if (originalState == null || !originalState.isOf(Blocks.FARMLAND) || !world.getBlockState(pos).isOf(Blocks.FARMLAND)) {
			return;
		}
		if (!isInsideMissionArea(serverPlayer, serverWorld, pos, MineFluenceMissionRoute.BAD, 2)) {
			return;
		}

		incrementBadMissionProgress(serverPlayer, 2, 1);
	}

	public static void onGenericContainerOpened(ServerPlayerEntity player, Inventory inventory) {
		if (player == null || player.getServer() == null || player.isSpectator() || inventory == null) {
			return;
		}
		if (!canProgressActiveBadMission(player, 4) || !isVillagerContainer(player, inventory)) {
			OPEN_CONTAINER_SNAPSHOTS.remove(player.getUuid());
			return;
		}

		OPEN_CONTAINER_SNAPSHOTS.put(player.getUuid(), new ContainerSnapshot(inventory, countItems(inventory)));
	}

	public static void onGenericContainerClosed(PlayerEntity player, Inventory inventory) {
		if (!(player instanceof ServerPlayerEntity serverPlayer) || inventory == null) {
			return;
		}

		ContainerSnapshot snapshot = OPEN_CONTAINER_SNAPSHOTS.remove(serverPlayer.getUuid());
		if (snapshot == null || snapshot.inventory() != inventory || !canProgressActiveBadMission(serverPlayer, 4)) {
			return;
		}

		int removedItems = snapshot.itemCount() - countItems(inventory);
		if (removedItems > 0) {
			incrementBadMissionProgress(serverPlayer, 4, removedItems);
		}
	}

	public static boolean canStartMission(ServerPlayerEntity player, MineFluenceMission mission) {
		MineFluenceAreaType requiredAreaType = MineFluenceMissionAreas.getAreaForMission(mission.route(), mission.index());
		if (requiredAreaType == null) {
			return true;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		if (state.getArea(requiredAreaType) == null && MineFluenceDemoMapPreset.loadInto(state, false) > 0) {
			MineFluenceDisplay.sendChat(player, "Area preset was missing and has been restored.");
		}
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
			if (hasBadGameplayDetection(mission.index())) {
				return mission.title() + ": " + data.getActiveMissionProgress() + "/" + mission.targetProgress();
			}
			return mission.title() + ": gameplay detection is planned for Stage 4B. Use /minefluence mission complete_debug for now.";
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
		if (mission.index() == 5) {
			return mission.title() + ": Crafted "
					+ Math.min(data.getMission5CraftedComposters(), mission.targetProgress()) + "/" + mission.targetProgress()
					+ ", Placed " + Math.min(data.getMission5PlacedComposters(), mission.targetProgress()) + "/" + mission.targetProgress();
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
			if (hasBadGameplayDetection(mission.index())) {
				return mission.title() + " " + data.getActiveMissionProgress() + "/" + mission.targetProgress();
			}
			return mission.title() + " debug-only";
		}
		if (mission.index() == 5) {
			return mission.title() + " Crafted "
					+ Math.min(data.getMission5CraftedComposters(), mission.targetProgress()) + "/" + mission.targetProgress()
					+ ", Placed " + Math.min(data.getMission5PlacedComposters(), mission.targetProgress()) + "/" + mission.targetProgress();
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
		if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer) || serverPlayer.isSpectator()) {
			return ActionResult.PASS;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(serverPlayer.getServer());
		MineFluencePlayerData data = state.getPlayerData(serverPlayer);
		if (!data.hasActiveMission()
				|| data.isWaitingForPostingChoice()
				|| data.getActiveMissionRoute() != MineFluenceMissionRoute.GOOD
				|| !isMissionVillager(entity)) {
			return ActionResult.PASS;
		}

		if (canProgressActiveGoodMission(data, 4)) {
			ItemStack stack = serverPlayer.getStackInHand(hand);
			if (stack.isOf(Items.POTATO)) {
				stack.decrementUnlessCreative(1, serverPlayer);
				incrementEventProgress(serverPlayer, state, data, 1);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	public static void onVillagerTradeCompleted(PlayerEntity player, Entity merchantEntity) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)
				|| serverPlayer.isSpectator()
				|| player.getWorld().isClient()
				|| !isMissionVillager(merchantEntity)) {
			return;
		}

		VillagerEntity villager = (VillagerEntity) merchantEntity;
		if (!isMissionFarmerVillager(villager)) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(serverPlayer.getServer());
		MineFluencePlayerData data = state.getPlayerData(serverPlayer);
		if (!canProgressActiveGoodMission(data, 3)) {
			return;
		}

		incrementEventProgress(serverPlayer, state, data, 1);
	}

	private static ActionResult onUseBlock(
			net.minecraft.entity.player.PlayerEntity player,
			World world,
			Hand hand,
			net.minecraft.util.hit.BlockHitResult hitResult
	) {
		if (world.isClient() || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayer) || serverPlayer.isSpectator()) {
			return ActionResult.PASS;
		}

		if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.BELL)) {
			incrementBadMissionProgress(serverPlayer, 1, 1);
		}
		return ActionResult.PASS;
	}

	private static ActionResult onAttackEntity(
			net.minecraft.entity.player.PlayerEntity player,
			World world,
			Hand hand,
			Entity entity,
			net.minecraft.util.hit.EntityHitResult hitResult
	) {
		if (world.isClient() || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayer) || serverPlayer.isSpectator()) {
			return ActionResult.PASS;
		}

		if (isMissionVillager(entity)) {
			incrementBadMissionProgress(serverPlayer, 3, 1);
		}
		return ActionResult.PASS;
	}

	private static void onBlockBreak(
			World world,
			net.minecraft.entity.player.PlayerEntity player,
			BlockPos pos,
			BlockState state,
			net.minecraft.block.entity.BlockEntity blockEntity
	) {
		if (world.isClient() || !(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer) || serverPlayer.isSpectator()) {
			return;
		}

		if (state == null) {
			return;
		}

		if (state.isOf(Blocks.FARMLAND)
				&& isInsideMissionArea(serverPlayer, serverWorld, pos, MineFluenceMissionRoute.BAD, 2)) {
			incrementBadMissionProgress(serverPlayer, 2, 1);
		}
		if (state.isOf(Blocks.COMPOSTER)) {
			incrementBadMissionProgress(serverPlayer, 5, 1);
		}
		if (isFarmPlotDestructionBlock(state)
				&& isInsideMissionArea(serverPlayer, serverWorld, pos, MineFluenceMissionRoute.BAD, 6)) {
			incrementBadMissionProgress(serverPlayer, 6, 1);
		}
	}

	private static void onLivingDeath(LivingEntity entity, DamageSource damageSource) {
		if (entity.getWorld().isClient()
				|| !isMissionVillager(entity)) {
			return;
		}

		Entity attacker = damageSource.getAttacker();
		if (attacker instanceof ServerPlayerEntity serverPlayer && !serverPlayer.isSpectator()) {
			incrementBadMissionProgress(serverPlayer, 7, 1);
		}
	}

	public static void onItemCrafted(PlayerEntity player, ItemStack stack, int amount) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)
				|| stack == null
				|| !stack.isOf(Items.COMPOSTER)
				|| amount <= 0) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(serverPlayer.getServer());
		MineFluencePlayerData data = state.getPlayerData(serverPlayer);
		if (!canProgressGoodMission5(data)) {
			return;
		}

		data.setMission5CraftedComposters(data.getMission5CraftedComposters() + amount);
		updateGoodMission5Progress(serverPlayer, state, data);
	}

	public static void onBlockPlaced(PlayerEntity player, BlockState placedState) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)
				|| placedState == null
				|| !placedState.isOf(Blocks.COMPOSTER)) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(serverPlayer.getServer());
		MineFluencePlayerData data = state.getPlayerData(serverPlayer);
		if (!canProgressGoodMission5(data)) {
			return;
		}

		data.setMission5PlacedComposters(data.getMission5PlacedComposters() + 1);
		updateGoodMission5Progress(serverPlayer, state, data);
	}

	private static int scannedProgress(ServerPlayerEntity player, MineFluencePlayerData data) {
		return switch (data.getActiveMissionIndex()) {
			case 1 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.GARDEN, state -> state.isIn(BlockTags.FLOWERS)) - data.getMissionBaselineValue();
			case 2 -> countBlocksInArea(player.getServer(), MineFluenceAreaType.FARM, state -> state.isOf(Blocks.WHEAT)) - data.getMissionBaselineValue();
			case 5 -> -1;
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

	public static boolean incrementBadMissionProgress(ServerPlayerEntity player, int missionIndex, int amount) {
		if (amount <= 0 || player.getServer() == null) {
			return false;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);
		MineFluenceMission mission = missionFor(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (!canProgressActiveBadMission(data, mission, missionIndex)) {
			return false;
		}

		incrementEventProgress(player, state, data, amount);
		return true;
	}

	private static boolean canProgressActiveBadMission(ServerPlayerEntity player, int missionIndex) {
		if (player == null || player.getServer() == null) {
			return false;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);
		MineFluenceMission mission = missionFor(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		return canProgressActiveBadMission(data, mission, missionIndex);
	}

	private static boolean canProgressActiveBadMission(MineFluencePlayerData data, MineFluenceMission mission, int missionIndex) {
		return data.getSelectedJob() == MineFluenceJob.FARMER
				&& !data.isEndingTriggered()
				&& data.hasActiveMission()
				&& !data.isWaitingForPostingChoice()
				&& data.getActiveMissionRoute() == MineFluenceMissionRoute.BAD
				&& data.getActiveMissionIndex() == missionIndex
				&& mission.route() == MineFluenceMissionRoute.BAD
				&& mission.index() == missionIndex
				&& hasBadGameplayDetection(missionIndex)
				&& mission.targetProgress() > 0
				&& data.getActiveMissionProgress() < mission.targetProgress();
	}

	private static boolean canProgressGoodMission5(MineFluencePlayerData data) {
		return data.getSelectedJob() == MineFluenceJob.FARMER
				&& !data.isEndingTriggered()
				&& data.hasActiveMission()
				&& !data.isWaitingForPostingChoice()
				&& data.getActiveMissionRoute() == MineFluenceMissionRoute.GOOD
				&& data.getActiveMissionIndex() == 5;
	}

	private static boolean canProgressActiveGoodMission(MineFluencePlayerData data, int missionIndex) {
		return data.getSelectedJob() == MineFluenceJob.FARMER
				&& !data.isEndingTriggered()
				&& data.hasActiveMission()
				&& !data.isWaitingForPostingChoice()
				&& data.getActiveMissionRoute() == MineFluenceMissionRoute.GOOD
				&& data.getActiveMissionIndex() == missionIndex
				&& data.getActiveMissionProgress() < missionFor(missionIndex, MineFluenceMissionRoute.GOOD).targetProgress();
	}

	private static void updateGoodMission5Progress(
			ServerPlayerEntity player,
			MineFluenceWorldState state,
			MineFluencePlayerData data
	) {
		MineFluenceMission mission = missionFor(5, MineFluenceMissionRoute.GOOD);
		int target = mission.targetProgress();
		int visibleProgress = Math.min(data.getMission5PlacedComposters(), target);
		if (visibleProgress != data.getActiveMissionProgress()) {
			data.setActiveMissionProgress(visibleProgress);
		}
		state.markDirty();
		MineFluenceHud.refresh(player, data);

		if (data.getMission5CraftedComposters() >= target
				&& data.getMission5PlacedComposters() >= target) {
			completeActiveMission(player, state, data, mission);
		}
	}

	private static void updateProgress(ServerPlayerEntity player, MineFluenceWorldState state, MineFluencePlayerData data, int rawProgress) {
		MineFluenceMission mission = missionFor(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		int progress = Math.min(Math.max(0, rawProgress), mission.targetProgress());
		if (progress != data.getActiveMissionProgress()) {
			data.setActiveMissionProgress(progress);
			state.markDirty();
		}

		MineFluenceHud.refresh(player, data);
		if (progress >= mission.targetProgress()) {
			completeActiveMission(player, state, data, mission);
		}
	}

	private static void completeActiveMission(ServerPlayerEntity player, MineFluenceWorldState state, MineFluencePlayerData data, MineFluenceMission mission) {
		if (!MineFluenceMissionCompletionService.complete(player, state, data, mission)) {
			return;
		}

		MineFluenceDisplay.sendChat(player, "Mission objective completed!");
		MineFluenceDisplay.sendChat(player, "Choose posting style:");
		MineFluenceDisplay.sendChat(player, "/minefluence post normal");
		MineFluenceDisplay.sendChat(player, "/minefluence post exaggerate");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] " + mission.title() + " complete");
		MineFluenceHud.refresh(player, data);
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

	private static boolean isFarmPlotDestructionBlock(BlockState state) {
		return state.isOf(Blocks.FARMLAND)
				|| state.isOf(Blocks.WHEAT)
				|| state.isOf(Blocks.CARROTS)
				|| state.isOf(Blocks.POTATOES)
				|| state.isOf(Blocks.BEETROOTS);
	}

	private static boolean isMissionVillager(Entity entity) {
		return entity instanceof VillagerEntity;
	}

	private static boolean isMissionFarmerVillager(VillagerEntity villager) {
		return villager.getVillagerData().getProfession() == VillagerProfession.FARMER
				|| MineFluenceFanVillagers.isFan(villager);
	}

	private static boolean isInsideMissionArea(
			ServerPlayerEntity player,
			ServerWorld world,
			BlockPos pos,
			MineFluenceMissionRoute route,
			int missionIndex
	) {
		MineFluenceAreaType type = MineFluenceMissionAreas.getAreaForMission(route, missionIndex);
		if (type == null) {
			return true;
		}
		MineFluenceArea area = MineFluenceWorldState.get(player.getServer()).getArea(type);
		return area != null && area.contains(world, pos);
	}

	private static boolean isVillagerContainer(ServerPlayerEntity player, Inventory inventory) {
		List<BlockEntity> containers = new ArrayList<>();
		collectVillagerContainerBlocks(inventory, containers);
		if (containers.isEmpty()) {
			return false;
		}

		MineFluenceAreaType type = MineFluenceMissionAreas.getAreaForMission(MineFluenceMissionRoute.BAD, 4);
		MineFluenceArea area = MineFluenceWorldState.get(player.getServer()).getArea(type);
		if (area == null) {
			return false;
		}

		for (BlockEntity container : containers) {
			ServerWorld containerWorld = container.getWorld() instanceof ServerWorld serverWorld
					? serverWorld
					: player.getServerWorld();
			if (area.contains(containerWorld, container.getPos())) {
				return true;
			}
		}
		return false;
	}

	private static void collectVillagerContainerBlocks(Inventory inventory, List<BlockEntity> containers) {
		if (inventory instanceof ChestBlockEntity chest) {
			containers.add(chest);
			return;
		}
		if (inventory instanceof BarrelBlockEntity barrel) {
			containers.add(barrel);
			return;
		}
		if (inventory instanceof DoubleInventory doubleInventory) {
			DoubleInventoryAccessor accessor = (DoubleInventoryAccessor) doubleInventory;
			collectVillagerContainerBlocks(accessor.minefluence$getFirst(), containers);
			collectVillagerContainerBlocks(accessor.minefluence$getSecond(), containers);
		}
	}

	private static int countItems(Inventory inventory) {
		int count = 0;
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (!stack.isEmpty()) {
				count += stack.getCount();
			}
		}
		return count;
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

	private record ContainerSnapshot(Inventory inventory, int itemCount) {
	}
}
