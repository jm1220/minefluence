package net.jeongmin.modid.command;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jeongmin.modid.area.MineFluenceArea;
import net.jeongmin.modid.area.MineFluenceAreaGuideManager;
import net.jeongmin.modid.area.MineFluenceAreaType;
import net.jeongmin.modid.area.MineFluenceDemoMapPreset;
import net.jeongmin.modid.billboard.MineFluenceBillboardBlockEntity;
import net.jeongmin.modid.billboard.MineFluenceBillboardImageResolver;
import net.jeongmin.modid.billboard.MineFluenceBillboards;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.core.MineFluenceDemoFlow;
import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ending.MineFluenceEnding;
import net.jeongmin.modid.ending.MineFluenceEndingManager;
import net.jeongmin.modid.ending.MineFluenceEndingTier;
import net.jeongmin.modid.ending.MineFluenceEndingVideoLauncher;
import net.jeongmin.modid.fan.MineFluenceFanVillagers;
import net.jeongmin.modid.fan.MineFluenceFanVillagers.SyncResult;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.jeongmin.modid.invasion.MineFluenceInvasionSupportManager;
import net.jeongmin.modid.invasion.MineFluenceInvasionSupportManager.SpawnResult;
import net.jeongmin.modid.item.MineFluenceItems;
import net.jeongmin.modid.mission.FarmerMissions;
import net.jeongmin.modid.mission.MineFluenceMission;
import net.jeongmin.modid.mission.MineFluenceMissionCompletionService;
import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.mission.MineFluenceMissionSelectionService;
import net.jeongmin.modid.mission.MineFluenceMissionSupplies;
import net.jeongmin.modid.mission.MineFluencePostingService;
import net.jeongmin.modid.network.MineFluenceNetworking;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.jeongmin.modid.weapon.MineFluenceWeaponManager;
import net.jeongmin.modid.weapon.MineFluenceWeaponTier;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class MineFluenceCommands {
	private static final MineFluenceAreaType[] REQUIRED_DEMO_AREAS = {
			MineFluenceAreaType.GARDEN,
			MineFluenceAreaType.FARM,
			MineFluenceAreaType.SHARED_SPACE,
			MineFluenceAreaType.FARM_BUILD_AREA
	};

	private MineFluenceCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				literal("minefluence")
						.then(literal("start")
								.executes(MineFluenceCommands::startDemo))
						.then(literal("status")
								.executes(MineFluenceCommands::showStatus))
						.then(literal("stats")
								.executes(MineFluenceCommands::showStats))
						.then(literal("phone")
								.then(literal("give")
										.executes(MineFluenceCommands::givePhone))
								.then(literal("remove")
										.executes(MineFluenceCommands::removePhone)))
						.then(literal("tutorial")
								.executes(MineFluenceCommands::openTutorial)
								.then(literal("open")
										.executes(MineFluenceCommands::openTutorial)))
						.then(literal("hud")
								.then(literal("refresh")
										.executes(MineFluenceCommands::refreshHud)))
						.then(literal("fans")
								.then(literal("sync")
										.executes(MineFluenceCommands::syncFans))
								.then(literal("clear")
										.executes(MineFluenceCommands::clearFans))
								.then(literal("count")
										.executes(MineFluenceCommands::countFans)))
						.then(literal("billboard")
								.then(literal("give")
										.executes(MineFluenceCommands::giveBillboard))
								.then(literal("set")
										.then(argument("image_id", word())
												.executes(context -> setLookedAtBillboardImage(context, getString(context, "image_id")))))
								.then(literal("list")
										.executes(MineFluenceCommands::listBillboardImages))
								.then(literal("info")
										.executes(MineFluenceCommands::showLookedAtBillboardInfo))
								.then(literal("size")
										.then(argument("width", integer(
												MineFluenceBillboardBlockEntity.MIN_WIDTH_BLOCKS,
												MineFluenceBillboardBlockEntity.MAX_WIDTH_BLOCKS
										))
												.then(argument("height", integer(
														MineFluenceBillboardBlockEntity.MIN_HEIGHT_BLOCKS,
														MineFluenceBillboardBlockEntity.MAX_HEIGHT_BLOCKS
												))
														.executes(context -> setLookedAtBillboardSize(
																context,
																getInteger(context, "width"),
																getInteger(context, "height")
														)))))
								.then(literal("preset")
										.then(literal("small")
												.executes(context -> setLookedAtBillboardSize(context, 4, 3)))
										.then(literal("medium")
												.executes(context -> setLookedAtBillboardSize(context, 8, 4)))
										.then(literal("large")
												.executes(context -> setLookedAtBillboardSize(context, 12, 6)))
										.then(literal("huge")
												.executes(context -> setLookedAtBillboardSize(context, 16, 9))))
								.then(literal("group")
										.then(argument("group_name", word())
												.executes(context -> setLookedAtBillboardGroup(context, getString(context, "group_name")))))
								.then(literal("group_set")
										.then(argument("group_name", word())
												.then(argument("image_id", word())
														.executes(context -> setBillboardGroupImage(
																context,
																getString(context, "group_name"),
																getString(context, "image_id")
														))))))
						.then(literal("reset")
								.executes(MineFluenceCommands::resetStats))
						.then(literal("demo")
								.then(literal("setup")
										.executes(MineFluenceCommands::demoSetup))
								.then(literal("skip_mission")
										.executes(MineFluenceCommands::demoSkipMission))
								.then(literal("quick_normal")
										.executes(context -> demoQuickPost(context, MineFluenceMissionRoute.GOOD, false)))
								.then(literal("quick_exaggerate")
										.executes(context -> demoQuickPost(context, MineFluenceMissionRoute.GOOD, true)))
								.then(literal("quick_bad_normal")
										.executes(context -> demoQuickPost(context, MineFluenceMissionRoute.BAD, false)))
								.then(literal("quick_bad_exaggerate")
										.executes(context -> demoQuickPost(context, MineFluenceMissionRoute.BAD, true)))
								.then(literal("check")
										.executes(MineFluenceCommands::demoCheck)))
						.then(literal("choose")
								.then(literal("farmer")
										.executes(MineFluenceCommands::chooseFarmer))
								.then(literal("architect")
										.executes(context -> lockedJob(context, MineFluenceJob.ARCHITECT)))
								.then(literal("cook")
										.executes(context -> lockedJob(context, MineFluenceJob.COOK))))
						.then(literal("mission")
								.then(literal("next")
										.executes(MineFluenceCommands::startNextMission))
								.then(literal("preview")
										.executes(MineFluenceCommands::previewMissionOptions))
								.then(literal("choose")
										.then(literal("good")
												.executes(context -> chooseMission(context, MineFluenceMissionRoute.GOOD)))
										.then(literal("bad")
												.executes(context -> chooseMission(context, MineFluenceMissionRoute.BAD))))
								.then(literal("current")
										.executes(MineFluenceCommands::showCurrentMission))
								.then(literal("progress")
										.executes(MineFluenceCommands::showMissionProgress))
								.then(literal("complete_debug")
										.executes(MineFluenceCommands::completeActiveMissionDebug)))
						.then(literal("area")
								.then(literal("set")
										.then(literal("garden")
												.then(argument("radius", integer(1, 64))
														.executes(context -> setArea(context, MineFluenceAreaType.GARDEN, getInteger(context, "radius")))))
										.then(literal("farm")
												.then(argument("radius", integer(1, 64))
														.executes(context -> setArea(context, MineFluenceAreaType.FARM, getInteger(context, "radius")))))
										.then(literal("shared")
												.then(argument("radius", integer(1, 64))
														.executes(context -> setArea(context, MineFluenceAreaType.SHARED_SPACE, getInteger(context, "radius")))))
										.then(literal("farm_build")
												.then(argument("radius", integer(1, 64))
														.executes(context -> setArea(context, MineFluenceAreaType.FARM_BUILD_AREA, getInteger(context, "radius"))))))
								.then(literal("set_box")
										.then(argument("type", word())
												.then(argument("x1", integer())
														.then(argument("y1", integer())
																.then(argument("z1", integer())
																		.then(argument("x2", integer())
																				.then(argument("y2", integer())
																						.then(argument("z2", integer())
																								.executes(context -> setAreaBox(
																										context,
																										getString(context, "type"),
																										getInteger(context, "x1"),
																										getInteger(context, "y1"),
																										getInteger(context, "z1"),
																										getInteger(context, "x2"),
																										getInteger(context, "y2"),
																										getInteger(context, "z2")
																								))))))))))
								.then(literal("load_preset")
										.executes(MineFluenceCommands::loadAreaPreset))
								.then(literal("list")
										.executes(MineFluenceCommands::listAreas))
								.then(literal("info")
										.then(literal("garden")
												.executes(context -> showAreaInfo(context, MineFluenceAreaType.GARDEN)))
										.then(literal("farm")
												.executes(context -> showAreaInfo(context, MineFluenceAreaType.FARM)))
										.then(literal("shared")
												.executes(context -> showAreaInfo(context, MineFluenceAreaType.SHARED_SPACE)))
										.then(literal("farm_build")
												.executes(context -> showAreaInfo(context, MineFluenceAreaType.FARM_BUILD_AREA))))
								.then(literal("show")
										.then(literal("garden")
												.executes(context -> showAreaGuide(context, MineFluenceAreaType.GARDEN)))
										.then(literal("farm")
												.executes(context -> showAreaGuide(context, MineFluenceAreaType.FARM)))
										.then(literal("shared")
												.executes(context -> showAreaGuide(context, MineFluenceAreaType.SHARED_SPACE)))
										.then(literal("farm_build")
												.executes(context -> showAreaGuide(context, MineFluenceAreaType.FARM_BUILD_AREA))))
								.then(literal("clear")
										.then(literal("garden")
												.executes(context -> clearArea(context, MineFluenceAreaType.GARDEN)))
										.then(literal("farm")
												.executes(context -> clearArea(context, MineFluenceAreaType.FARM)))
										.then(literal("shared")
												.executes(context -> clearArea(context, MineFluenceAreaType.SHARED_SPACE)))
										.then(literal("farm_build")
												.executes(context -> clearArea(context, MineFluenceAreaType.FARM_BUILD_AREA)))))
						.then(literal("post")
								.then(literal("normal")
										.executes(context -> postMission(context, false)))
								.then(literal("exaggerate")
										.executes(context -> postMission(context, true))))
						.then(literal("invasion")
								.then(literal("status")
										.executes(MineFluenceCommands::showInvasionStatus))
								.then(literal("start")
										.then(argument("index", integer(1, 3))
												.executes(context -> startInvasionDebug(context, getInteger(context, "index")))))
								.then(literal("stop_debug")
										.executes(MineFluenceCommands::stopInvasionDebug)))
						.then(literal("support")
								.then(literal("count")
										.executes(MineFluenceCommands::countSupportAllies))
								.then(literal("spawn")
										.executes(MineFluenceCommands::spawnSupportAllies))
								.then(literal("clear")
										.executes(MineFluenceCommands::clearSupportAllies)))
						.then(literal("weapon")
								.then(literal("status")
										.executes(MineFluenceCommands::showWeaponStatus))
								.then(literal("update")
										.executes(MineFluenceCommands::updateWeaponDebug))
								.then(literal("give")
										.then(literal("wood")
												.executes(context -> giveWeaponDebug(context, MineFluenceWeaponTier.WOOD)))
										.then(literal("stone")
												.executes(context -> giveWeaponDebug(context, MineFluenceWeaponTier.STONE)))
										.then(literal("iron")
												.executes(context -> giveWeaponDebug(context, MineFluenceWeaponTier.IRON)))
										.then(literal("gold")
												.executes(context -> giveWeaponDebug(context, MineFluenceWeaponTier.GOLD)))
										.then(literal("diamond")
												.executes(context -> giveWeaponDebug(context, MineFluenceWeaponTier.DIAMOND)))))
						.then(literal("ending")
								.then(literal("preview")
										.executes(MineFluenceCommands::previewEnding))
								.then(literal("video_test")
										.executes(MineFluenceCommands::testEndingVideo)
										.then(literal("the_famous_villain")
												.executes(MineFluenceCommands::testEndingVideo)))
								.then(literal("trigger_debug")
										.executes(MineFluenceCommands::triggerEndingDebug))
								.then(literal("reset_debug")
										.executes(MineFluenceCommands::resetEndingDebug))
								.then(literal("set_test")
										.then(argument("followerTier", word())
												.then(argument("socialTier", word())
														.executes(context -> setEndingTest(
																context,
																getString(context, "followerTier"),
																getString(context, "socialTier")
														))))))
						.then(literal("set")
								.then(literal("follower")
										.then(argument("value", integer())
												.executes(context -> setFollower(context, getInteger(context, "value")))))
								.then(literal("social")
										.then(argument("value", integer())
												.executes(context -> setSocialCredibility(context, getInteger(context, "value")))))
								.then(literal("lie")
										.then(argument("value", integer())
												.executes(context -> setLieValue(context, getInteger(context, "value")))))
								.then(literal("missions")
										.then(argument("value", integer())
												.executes(context -> setCompletedMissionCount(context, getInteger(context, "value"))))))
						.then(literal("add")
								.then(literal("follower")
										.then(argument("delta", integer())
												.executes(context -> addFollower(context, getInteger(context, "delta")))))
								.then(literal("social")
										.then(argument("delta", integer())
												.executes(context -> addSocialCredibility(context, getInteger(context, "delta")))))
								.then(literal("lie")
										.then(argument("delta", integer())
												.executes(context -> addLieValue(context, getInteger(context, "delta")))))
								.then(literal("missions")
										.then(argument("delta", integer())
												.executes(context -> addCompletedMissionCount(context, getInteger(context, "delta"))))))
						.then(literal("job")
								.then(literal("farmer")
										.executes(context -> setJob(context, MineFluenceJob.FARMER)))
								.then(literal("none")
										.executes(context -> setJob(context, MineFluenceJob.NONE))))
		));
	}

	private static int startDemo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		MineFluenceDemoFlow.startDemo(context.getSource().getPlayerOrThrow(), true);
		return 1;
	}

	private static int givePhone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceItems.ensureSingleSmartphone(player);
		MineFluenceDisplay.sendChat(source, "MineFluence Smartphone ready. Right-click it or press M to open the mission board.");
		return 1;
	}

	private static int removePhone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		int removed = MineFluenceItems.removeSmartphones(player);
		MineFluenceDisplay.sendChat(source, "Removed " + removed + " MineFluence Smartphone item(s).");
		return 1;
	}

	private static int refreshHud(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceHud.refresh(player, data);
		MineFluenceDisplay.sendChat(source, "HUD refreshed.");
		return 1;
	}

	private static int syncFans(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		SyncResult result = MineFluenceFanVillagers.syncFanVillagers(source.getPlayerOrThrow());
		MineFluenceDisplay.sendChat(source, "Fans synced: current=" + result.currentCount()
				+ ", target=" + result.targetCount()
				+ ", spawned=" + result.spawnedCount()
				+ ", removed=" + result.removedCount() + ".");
		return 1;
	}

	private static int clearFans(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		int removed = MineFluenceFanVillagers.clearFanVillagers(source.getPlayerOrThrow());
		MineFluenceDisplay.sendChat(source, "Cleared " + removed + " MineFluence fan villager(s).");
		return 1;
	}

	private static int countFans(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		int current = MineFluenceFanVillagers.countFanVillagers(player);
		int target = MineFluenceBalance.getTargetFanCount(data.getFollower());
		MineFluenceDisplay.sendChat(source, "Fan villagers: current=" + current + ", target=" + target + ", followers=" + data.getFollower() + ".");
		return 1;
	}

	private static int giveBillboard(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		player.giveItemStack(new ItemStack(MineFluenceBillboards.BILLBOARD_ANCHOR_ITEM));
		MineFluenceDisplay.sendChat(source, "Billboard anchor item given. Place it in the village and use /minefluence billboard group main.");
		return 1;
	}

	private static int setLookedAtBillboardImage(CommandContext<ServerCommandSource> context, String imageId) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceBillboardBlockEntity billboard = MineFluenceBillboards.findLookedAtBillboard(player).orElse(null);
		if (billboard == null) {
			MineFluenceDisplay.sendChat(source, "Look at a billboard anchor within 16 blocks first.");
			return 0;
		}

		String normalizedImageId = MineFluenceBillboardImageResolver.normalizeImageId(imageId);
		billboard.setImageId(normalizedImageId);
		MineFluenceDisplay.sendChat(source, "Billboard image set to " + normalizedImageId + ".");
		if (!MineFluenceBillboardImageResolver.isKnownImageId(normalizedImageId)) {
			MineFluenceDisplay.sendChat(source, "That image is not in the current MVP list; the renderer falls back to default.png if the texture is missing.");
		}
		return 1;
	}

	private static int listBillboardImages(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		MineFluenceDisplay.sendChat(source, "Billboard images: " + String.join(", ", MineFluenceBillboardImageResolver.availableImageIds()));
		return MineFluenceBillboardImageResolver.availableImageIds().size();
	}

	private static int showLookedAtBillboardInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceBillboardBlockEntity billboard = MineFluenceBillboards.findLookedAtBillboard(player).orElse(null);
		if (billboard == null) {
			MineFluenceDisplay.sendChat(source, "Look at a billboard anchor within 16 blocks first.");
			return 0;
		}

		MineFluenceDisplay.sendChat(source, "Billboard info: imageId=" + billboard.getImageId()
				+ ", group=" + billboard.getGroup()
				+ ", autoMode=" + billboard.isAutoMode()
				+ ", width=" + billboard.getWidthBlocks()
				+ ", height=" + billboard.getHeightBlocks() + ".");
		return 1;
	}

	private static int setLookedAtBillboardSize(CommandContext<ServerCommandSource> context, int width, int height) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceBillboardBlockEntity billboard = MineFluenceBillboards.findLookedAtBillboard(player).orElse(null);
		if (billboard == null) {
			MineFluenceDisplay.sendChat(source, "Look at a billboard anchor within 16 blocks first.");
			return 0;
		}

		billboard.setSizeBlocks(width, height);
		MineFluenceDisplay.sendChat(source, "Billboard size set to " + billboard.getWidthBlocks() + "x" + billboard.getHeightBlocks() + ".");
		return 1;
	}

	private static int setLookedAtBillboardGroup(CommandContext<ServerCommandSource> context, String group) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceBillboardBlockEntity billboard = MineFluenceBillboards.findLookedAtBillboard(player).orElse(null);
		if (billboard == null) {
			MineFluenceDisplay.sendChat(source, "Look at a billboard anchor within 16 blocks first.");
			return 0;
		}

		String normalizedGroup = MineFluenceBillboards.normalizeGroup(group);
		billboard.setGroup(normalizedGroup);
		MineFluenceDisplay.sendChat(source, "Billboard group set to " + normalizedGroup + ".");
		return 1;
	}

	private static int setBillboardGroupImage(CommandContext<ServerCommandSource> context, String group, String imageId) {
		ServerCommandSource source = context.getSource();
		String normalizedGroup = MineFluenceBillboards.normalizeGroup(group);
		String normalizedImageId = MineFluenceBillboardImageResolver.normalizeImageId(imageId);
		int updated = MineFluenceBillboards.setGroupImage(source.getServer(), normalizedGroup, normalizedImageId);
		MineFluenceDisplay.sendChat(source, "Updated " + updated + " loaded billboard(s) in group " + normalizedGroup + " to " + normalizedImageId + ".");
		if (updated == 0) {
			MineFluenceDisplay.sendChat(source, "Only loaded billboards near online players are scanned in this MVP.");
		}
		return updated;
	}

	private static int openTutorial(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		if (MineFluenceNetworking.openTutorial(player)) {
			MineFluenceDisplay.sendChat(source, "Opening tutorial.");
			return 1;
		}

		MineFluenceDisplay.sendChat(source, "Tutorial screen channel is not available on this client.");
		return 0;
	}

	private static int showStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceDisplay.sendPublicStatus(source, data);
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int chooseFarmer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		MineFluenceDemoFlow.chooseFarmer(context.getSource().getPlayerOrThrow());
		return 1;
	}

	private static int lockedJob(CommandContext<ServerCommandSource> context, MineFluenceJob job) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceDisplay.sendChat(source, job.displayName() + " is locked in this demo.");
		MineFluenceDisplay.sendActionBar(player, job.displayName() + " is locked in this demo");
		return 1;
	}

	private static int demoSetup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluenceDemoFlow.resetStartedDemoProgress(player);
		MineFluencePlayerData data = state.updatePlayerData(player, playerData -> {
			playerData.setSelectedJob(MineFluenceJob.FARMER);
		});
		MineFluenceWeaponManager.updateWeapon(player, data);
		MineFluenceItems.ensureSingleSmartphone(player);
		loadMissingDemoMapPresetAreas(source, state);
		MineFluenceFanVillagers.syncFanVillagers(player);

		MineFluenceDisplay.sendChat(source, "Demo setup complete. Clean state started with Farmer selected.");
		MineFluenceDisplay.sendChat(source, "Use /minefluence mission next to choose a route, or /minefluence demo quick_normal to advance one Good mission. The smartphone and M key open the mission board.");
		sendMissingAreaInstructions(source, state);
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int demoSkipMission(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		if (data.isWaitingForPostingChoice()) {
			showPendingPosting(source, player, data);
			MineFluenceDisplay.sendStatusActionBar(player, data);
			return 1;
		}
		if (!data.hasActiveMission()) {
			data = startNextMissionForDemo(source, player, state, data, MineFluenceMissionRoute.GOOD);
		}
		if (!data.hasActiveMission()) {
			MineFluenceDisplay.sendStatusActionBar(player, data);
			return 0;
		}

		return completeActiveMissionDebug(context);
	}

	private static int demoQuickPost(CommandContext<ServerCommandSource> context, MineFluenceMissionRoute route, boolean exaggerated) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		if (!data.isWaitingForPostingChoice()) {
			if (!data.hasActiveMission()) {
				data = startNextMissionForDemo(source, player, state, data, route);
			}
			if (data.hasActiveMission()) {
				completeActiveMissionDebug(context);
			}
		}

		MineFluencePlayerData updatedData = state.getPlayerData(player);
		if (!updatedData.isWaitingForPostingChoice()) {
			MineFluenceDisplay.sendChat(source, "Demo quick post could not find a mission ready to post.");
			MineFluenceDisplay.sendStatusActionBar(player, updatedData);
			return 0;
		}

		return postMission(context, exaggerated);
	}

	private static int demoCheck(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		MineFluenceDisplay.sendChat(source, "Demo Check:");
		MineFluenceDisplay.sendChat(source, "Job: " + data.getSelectedJob() + (data.getSelectedJob() == MineFluenceJob.FARMER ? " OK" : " MISSING"));
		for (MineFluenceAreaType type : REQUIRED_DEMO_AREAS) {
			MineFluenceDisplay.sendChat(source, areaChecklistLine(state, type));
		}
		MineFluenceDisplay.sendChat(source, "Mission State: " + missionStateForCheck(data));
		MineFluenceDisplay.sendChat(source, "Active Invasion: " + (data.hasActiveInvasion() ? data.getActiveInvasionIndex() : "None"));
		MineFluenceDisplay.sendChat(source, "Ending Triggered: " + (data.isEndingTriggered() ? MineFluenceEndingManager.endingDisplayName(data) : "No"));
		MineFluenceDisplay.sendChat(source, "Next: " + suggestedNextCommand(state, data));
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int startNextMission(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		return MineFluenceMissionSelectionService.prepareNextMission(context.getSource().getPlayerOrThrow()) ? 1 : 0;
	}

	private static int previewMissionOptions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);

		int missionIndex = data.hasPendingMissionSelection() ? data.getPendingMissionSelectionIndex() : data.getCompletedMissionCount() + 1;
		if (missionIndex > MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			MineFluenceDisplay.sendChat(source, "All Farmer demo missions are complete.");
			MineFluenceDisplay.sendStatusActionBar(player, data);
			return 1;
		}

		showMissionOptions(source, player, missionIndex);
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int chooseMission(CommandContext<ServerCommandSource> context, MineFluenceMissionRoute route) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		return MineFluenceMissionSelectionService.chooseMission(player, route, false) ? 1 : 0;
	}

	private static int showCurrentMission(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);

		if (data.hasPendingMissionSelection()) {
			showMissionOptions(source, player, data.getPendingMissionSelectionIndex());
			return 1;
		}
		if (data.isWaitingForPostingChoice()) {
			showPendingPosting(source, player, data);
			return 1;
		}
		if (data.hasActiveMission()) {
			showMission(source, player, missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute()));
			return 1;
		}

		MineFluenceDisplay.sendChat(source, "No active mission. Use /minefluence mission next.");
		MineFluenceDisplay.sendActionBar(player, "No active mission");
		return 0;
	}

	private static int showMissionProgress(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceDisplay.sendChat(source, "Progress: " + MineFluenceMissionProgressManager.progressText(player, data));
		if (data.hasActiveMission() && !data.isWaitingForPostingChoice()) {
			String areaLine = MineFluenceAreaGuideManager.requiredAreaProgressLine(player, data.getActiveMissionIndex(), data.getActiveMissionRoute());
			if (!areaLine.isBlank()) {
				MineFluenceDisplay.sendChat(source, areaLine);
			}
		}
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int completeActiveMissionDebug(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);

		if (data.isWaitingForPostingChoice()) {
			showPendingPosting(source, player, data);
			return 0;
		}
		if (!data.hasActiveMission()) {
			MineFluenceDisplay.sendChat(source, "No active mission to complete. Use /minefluence mission next.");
			MineFluenceDisplay.sendActionBar(player, "No active mission");
			return 0;
		}

		MineFluenceMission mission = missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		if (!MineFluenceMissionCompletionService.complete(player, state, data, mission)) {
			MineFluenceDisplay.sendChat(source, "Mission could not be completed because its state changed.");
			return 0;
		}
		MineFluenceDisplay.sendChat(source, "Mission objective completed: " + mission.route() + " - " + mission.title() + ".");
		MineFluenceDisplay.sendChat(source, "Choose posting style:");
		MineFluenceDisplay.sendChat(source, "/minefluence post normal");
		MineFluenceDisplay.sendChat(source, "/minefluence post exaggerate");
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int postMission(CommandContext<ServerCommandSource> context, boolean exaggerated) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		return MineFluencePostingService.postMission(player, exaggerated) ? 1 : 0;
	}

	private static int showInvasionStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);

		if (!data.hasActiveInvasion()) {
			MineFluenceDisplay.sendChat(source, "No active invasion. Last completed invasion=" + data.getLastCompletedInvasionIndex() + ".");
			return 1;
		}

		int remaining = MineFluenceInvasionManager.countRemainingTrackedMobs(source.getServer(), data);
		int supportAllies = MineFluenceInvasionSupportManager.countSupportAllies(player);
		MineFluenceDisplay.sendChat(source, "Active invasion=" + data.getActiveInvasionIndex()
				+ ", Invaders remaining=" + remaining
				+ ", Tracked mobs=" + data.getTrackedInvasionMobCount()
				+ ", Village defenders=" + supportAllies + ".");
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Invasion " + data.getActiveInvasionIndex() + ": Invaders remaining " + remaining);
		return 1;
	}

	private static int startInvasionDebug(CommandContext<ServerCommandSource> context, int invasionIndex) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);

		if (data.hasActiveInvasion()) {
			MineFluenceDisplay.sendChat(source, "Invasion " + data.getActiveInvasionIndex() + " is already active. Stop it before starting another.");
			return 0;
		}
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			MineFluenceDisplay.sendChat(source, "Debug start allowed even though selected job is " + data.getSelectedJob() + ".");
		}

		return MineFluenceInvasionManager.startInvasion(player, data, invasionIndex) ? 1 : 0;
	}

	private static int stopInvasionDebug(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceInvasionManager.stopInvasionDebug(player, data);
		return 1;
	}

	private static int countSupportAllies(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		int currentCount = MineFluenceInvasionSupportManager.countSupportAllies(player);
		int targetCount = MineFluenceBalance.getInvasionSupportCount(data.getSocialCredibility());
		MineFluenceDisplay.sendChat(source, "Village defenders: current=" + currentCount
				+ ", target=" + targetCount
				+ ", Social Credibility=" + data.getSocialCredibility() + ".");
		return 1;
	}

	private static int spawnSupportAllies(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		SpawnResult result = MineFluenceInvasionSupportManager.spawnForTesting(source.getPlayerOrThrow());
		MineFluenceDisplay.sendChat(source, "Support test spawn: spawned=" + result.spawnedCount()
				+ ", target=" + result.targetCount() + ".");
		return 1;
	}

	private static int clearSupportAllies(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		int removed = MineFluenceInvasionSupportManager.clearSupportAllies(source.getPlayerOrThrow());
		MineFluenceDisplay.sendChat(source, "Cleared " + removed + " MineFluence village defender(s).");
		return 1;
	}

	private static int setArea(CommandContext<ServerCommandSource> context, MineFluenceAreaType type, int radius) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceArea area = MineFluenceArea.atPlayer(type, player, radius);
		MineFluenceWorldState.get(source.getServer()).setArea(area);
		MineFluenceDisplay.sendChat(source, type.displayName() + " area set: " + area.describe() + ".");
		return 1;
	}

	private static int setAreaBox(
			CommandContext<ServerCommandSource> context,
			String typeText,
			int x1,
			int y1,
			int z1,
			int x2,
			int y2,
			int z2
	) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceAreaType type = MineFluenceAreaType.fromSerializedName(typeText);
		if (type == null) {
			MineFluenceDisplay.sendChat(source, "Area type must be garden, farm, shared, or farm_build.");
			return 0;
		}

		MineFluenceArea area = MineFluenceArea.box(
				type,
				player.getServerWorld().getRegistryKey().getValue().toString(),
				new BlockPos(x1, y1, z1),
				new BlockPos(x2, y2, z2)
		);
		MineFluenceWorldState.get(source.getServer()).setArea(area);
		MineFluenceDisplay.sendChat(source, "Area " + type.commandName() + " set to box " + MineFluenceArea.posText(area.min()) + " -> " + MineFluenceArea.posText(area.max()));
		return 1;
	}

	private static int loadAreaPreset(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		loadDemoMapPreset(source, MineFluenceWorldState.get(source.getServer()), true);
		return 1;
	}

	private static int listAreas(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluenceDisplay.sendChat(source, "Area setup:");
		for (MineFluenceAreaType type : REQUIRED_DEMO_AREAS) {
			MineFluenceArea area = state.getArea(type);
			if (area == null) {
				MineFluenceDisplay.sendChat(source, type.commandName() + ": MISSING");
			} else {
				MineFluenceDisplay.sendChat(source, type.commandName() + ": " + area.describe());
			}
		}
		return 1;
	}

	private static int showAreaInfo(CommandContext<ServerCommandSource> context, MineFluenceAreaType type) {
		ServerCommandSource source = context.getSource();
		MineFluenceArea area = MineFluenceWorldState.get(source.getServer()).getArea(type);
		if (area == null) {
			MineFluenceDisplay.sendChat(source, type.commandName() + ": MISSING. Run /minefluence area load_preset.");
			return 0;
		}

		MineFluenceDisplay.sendChat(source, type.commandName() + ": " + area.describe());
		return 1;
	}

	private static int showAreaGuide(CommandContext<ServerCommandSource> context, MineFluenceAreaType type) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		return MineFluenceAreaGuideManager.showArea(player, type) ? 1 : 0;
	}

	private static int clearArea(CommandContext<ServerCommandSource> context, MineFluenceAreaType type) {
		ServerCommandSource source = context.getSource();
		boolean removed = MineFluenceWorldState.get(source.getServer()).clearArea(type);
		if (removed) {
			MineFluenceDisplay.sendChat(source, type.displayName() + " area cleared.");
		} else {
			MineFluenceDisplay.sendChat(source, type.displayName() + " area was not configured.");
		}
		return 1;
	}

	private static int showWeaponStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceWeaponTier expectedTier = MineFluenceWeaponManager.determineTier(data.getFollower());
		MineFluenceDisplay.sendChat(source, "Weapon Status: Follower=" + data.getFollower() + ", Expected Tier=" + expectedTier + ", Stored Tier=" + data.getCurrentWeaponTier() + ", Job=" + data.getSelectedJob() + ".");
		return 1;
	}

	private static int updateWeaponDebug(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceWeaponTier tier = MineFluenceWeaponManager.updateWeapon(player, data);
		MineFluenceDisplay.sendChat(source, "Weapon update complete. Tier=" + tier + ".");
		return 1;
	}

	private static int giveWeaponDebug(CommandContext<ServerCommandSource> context, MineFluenceWeaponTier tier) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		MineFluenceWeaponManager.giveDebugWeapon(player, tier);
		return 1;
	}

	private static int previewEnding(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceEndingManager.previewEnding(player, data);
		return 1;
	}

	private static int testEndingVideo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		return MineFluenceEndingVideoLauncher.launchTheFamousVillain(player) ? 1 : 0;
	}

	private static int triggerEndingDebug(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).getPlayerData(player);
		MineFluenceEndingManager.triggerEnding(player, data);
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int resetEndingDebug(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).updatePlayerData(player, MineFluencePlayerData::clearEndingState);
		MineFluenceDisplay.sendChat(source, "Ending debug state reset.");
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int setEndingTest(CommandContext<ServerCommandSource> context, String followerTierText, String socialTierText) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceEndingTier followerTier = parseEndingTier(source, followerTierText, "Follower");
		MineFluenceEndingTier socialTier = parseEndingTier(source, socialTierText, "Social Credibility");
		if (followerTier == null || socialTier == null) {
			return 0;
		}

		int follower = MineFluenceEndingManager.representativeFollowerValue(followerTier);
		int socialCredibility = MineFluenceEndingManager.representativeSocialCredibilityValue(socialTier);
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).updatePlayerData(player, playerData -> {
			playerData.setFollower(follower);
			playerData.setSocialCredibility(socialCredibility);
			playerData.clearEndingState();
		});
		MineFluenceWeaponManager.updateWeapon(player, data);
		MineFluenceFanVillagers.syncFanVillagers(player);
		MineFluenceEnding ending = MineFluenceEndingManager.getEnding(data);
		MineFluenceDisplay.sendChat(source, "Ending test values set: Follower=" + data.getFollower() + " (" + followerTier + "), Social Credibility=" + data.getSocialCredibility() + " (" + socialTier + ").");
		MineFluenceDisplay.sendChat(source, "Current calculated ending: " + ending.displayName() + ".");
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int showStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluenceWorldState state = MineFluenceWorldState.get(source.getServer());
		MineFluencePlayerData data = state.getPlayerData(player);
		MineFluenceDisplay.sendChat(source, MineFluenceDisplay.debugStats(data) + ", Configured Areas=" + state.getConfiguredAreaCount());
		return 1;
	}

	private static int resetStats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceDemoFlow.resetDemoProgress(player);
		MineFluenceItems.ensureSingleSmartphone(player);
		MineFluenceDisplay.sendChat(source, "Demo reset to Tutorial start. " + shortStats(data));
		return 1;
	}

	private static int setFollower(CommandContext<ServerCommandSource> context, int value) throws CommandSyntaxException {
		int result = updateWeaponAfterDataChange(context, data -> data.setFollower(value), data -> "Follower set to " + data.getFollower() + ".");
		MineFluenceFanVillagers.syncFanVillagers(context.getSource().getPlayerOrThrow());
		return result;
	}

	private static int setSocialCredibility(CommandContext<ServerCommandSource> context, int value) throws CommandSyntaxException {
		return update(context, data -> data.setSocialCredibility(value), data -> "Social Credibility set to " + data.getSocialCredibility() + ".");
	}

	private static int setLieValue(CommandContext<ServerCommandSource> context, int value) throws CommandSyntaxException {
		return update(context, data -> data.setLieValue(value), data -> "Lie Value set to " + data.getLieValue() + ".");
	}

	private static int setCompletedMissionCount(CommandContext<ServerCommandSource> context, int value) throws CommandSyntaxException {
		return update(context, data -> data.setCompletedMissionCount(value), data -> "Completed missions set to " + data.getCompletedMissionCount() + ".");
	}

	private static int addFollower(CommandContext<ServerCommandSource> context, int delta) throws CommandSyntaxException {
		int result = updateWeaponAfterDataChange(context, data -> data.addFollower(delta), data -> "Follower changed by " + signed(delta) + " to " + data.getFollower() + ".");
		MineFluenceFanVillagers.syncFanVillagers(context.getSource().getPlayerOrThrow());
		return result;
	}

	private static int addSocialCredibility(CommandContext<ServerCommandSource> context, int delta) throws CommandSyntaxException {
		return update(context, data -> data.addSocialCredibility(delta), data -> "Social Credibility changed by " + signed(delta) + " to " + data.getSocialCredibility() + ".");
	}

	private static int addLieValue(CommandContext<ServerCommandSource> context, int delta) throws CommandSyntaxException {
		return update(context, data -> data.addLieValue(delta), data -> "Lie Value changed by " + signed(delta) + " to " + data.getLieValue() + ".");
	}

	private static int addCompletedMissionCount(CommandContext<ServerCommandSource> context, int delta) throws CommandSyntaxException {
		return update(context, data -> data.addCompletedMissionCount(delta), data -> "Completed missions changed by " + signed(delta) + " to " + data.getCompletedMissionCount() + ".");
	}

	private static int setJob(CommandContext<ServerCommandSource> context, MineFluenceJob job) throws CommandSyntaxException {
		return updateWeaponAfterDataChange(context, data -> data.setSelectedJob(job), data -> "Job set to " + data.getSelectedJob() + ".");
	}

	private static int update(
			CommandContext<ServerCommandSource> context,
			Consumer<MineFluencePlayerData> updater,
			Function<MineFluencePlayerData, String> messageFactory
	) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).updatePlayerData(player, updater);
		MineFluenceDisplay.sendChat(source, messageFactory.apply(data));
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static int updateWeaponAfterDataChange(
			CommandContext<ServerCommandSource> context,
			Consumer<MineFluencePlayerData> updater,
			Function<MineFluencePlayerData, String> messageFactory
	) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		MineFluencePlayerData data = MineFluenceWorldState.get(source.getServer()).updatePlayerData(player, updater);
		MineFluenceDisplay.sendChat(source, messageFactory.apply(data));
		MineFluenceWeaponManager.updateWeapon(player, data);
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return 1;
	}

	private static void showMissionOptions(ServerCommandSource source, ServerPlayerEntity player, int missionIndex) {
		MineFluenceDisplay.sendChat(source, "Mission " + missionIndex + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " route selection:");
		showMissionOption(source, missionOrFallback(missionIndex, MineFluenceMissionRoute.GOOD));
		showMissionOption(source, missionOrFallback(missionIndex, MineFluenceMissionRoute.BAD));
		MineFluenceDisplay.sendChat(source, "Choose /minefluence mission choose good or /minefluence mission choose bad.");
		MineFluenceDisplay.sendChat(source, "You can also press the MineFluence mission key to open the mission board.");
		MineFluenceDisplay.sendActionBar(player, "Choose mission " + missionIndex + " route");
	}

	private static void showMissionOption(ServerCommandSource source, MineFluenceMission mission) {
		MineFluenceDisplay.sendChat(source, mission.route() + ": " + mission.title()
				+ " | Objective: " + mission.objectiveText()
				+ " | Rewards: Follower " + signed(mission.baseFollowerReward())
				+ ", Social " + signed(mission.baseSocialCredibilityReward()));
	}

	private static void showMission(ServerCommandSource source, ServerPlayerEntity player, MineFluenceMission mission) {
		MineFluenceDisplay.sendChat(source, "Mission " + mission.index() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " " + mission.route() + ": " + mission.title());
		MineFluenceDisplay.sendChat(source, mission.description());
		MineFluenceDisplay.sendChat(source, "Objective: " + mission.objectiveText());
		MineFluenceDisplay.sendChat(source, "Target: " + mission.targetProgress());
		MineFluenceDisplay.sendChat(source, "Base rewards: Follower " + signed(mission.baseFollowerReward()) + ", Social Credibility " + signed(mission.baseSocialCredibilityReward()) + ".");
		if (mission.route() == MineFluenceMissionRoute.BAD && !MineFluenceMissionProgressManager.hasBadGameplayDetection(mission.index())) {
			MineFluenceDisplay.sendChat(source, "Bad mission detection is not implemented yet. Use /minefluence mission complete_debug for now.");
		}
		MineFluenceDisplay.sendActionBar(player, "Mission " + mission.index() + ": " + mission.title());
	}

	private static void showPendingPosting(ServerCommandSource source, ServerPlayerEntity player, MineFluencePlayerData data) {
		MineFluenceMission mission = missionOrFallback(data.getPendingPostingMissionIndex(), data.getPendingPostingMissionRoute());
		MineFluenceDisplay.sendChat(source, "Mission ready to post: " + mission.index() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " " + mission.route() + " - " + mission.title() + ".");
		MineFluenceDisplay.sendChat(source, "Choose /minefluence post normal or /minefluence post exaggerate.");
		MineFluenceDisplay.sendActionBar(player, "Choose a posting style");
	}

	private static MineFluencePlayerData startNextMissionForDemo(
			ServerCommandSource source,
			ServerPlayerEntity player,
			MineFluenceWorldState state,
			MineFluencePlayerData data,
			MineFluenceMissionRoute route
	) {
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			data = state.updatePlayerData(player, playerData -> playerData.setSelectedJob(MineFluenceJob.FARMER));
			MineFluenceWeaponManager.updateWeapon(player, data);
			MineFluenceDisplay.sendChat(source, "Demo helper selected Farmer.");
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			MineFluenceDisplay.sendChat(source, "All Farmer demo missions are already complete.");
			return data;
		}

		int nextMissionIndex = data.getCompletedMissionCount() + 1;
		MineFluenceMission mission = missionOrFallback(nextMissionIndex, route);
		int baseline = MineFluenceMissionProgressManager.baselineForMission(player, mission);
		MineFluencePlayerData updatedData = state.updatePlayerData(player, playerData -> {
			playerData.startMission(mission.index(), mission.route());
			playerData.setMissionBaselineValue(baseline);
			playerData.setActiveMissionProgress(0);
		});

		MineFluenceDisplay.sendChat(source, "Demo helper started mission " + mission.index() + " " + mission.route() + ".");
		showMission(source, player, mission);
		MineFluenceMissionSupplies.grantForMissionStart(player, updatedData, mission);
		MineFluenceDisplay.sendStatusActionBar(player, updatedData);
		MineFluenceAreaGuideManager.sendMissionAreaHint(player, mission.index(), mission.route());
		return updatedData;
	}

	private static void sendMissingAreaInstructions(ServerCommandSource source, MineFluenceWorldState state) {
		MineFluenceAreaType missingArea = firstMissingArea(state);
		if (missingArea == null) {
			MineFluenceDisplay.sendChat(source, "All required demo areas are configured.");
			return;
		}

		MineFluenceDisplay.sendChat(source, "Some demo areas are missing. Use /minefluence area load_preset or /minefluence area list for details.");
		for (MineFluenceAreaType type : REQUIRED_DEMO_AREAS) {
			if (state.getArea(type) == null) {
				MineFluenceDisplay.sendChat(source, "Set " + type.displayName() + " with /minefluence area set " + type.commandName() + " 8 or /minefluence area set_box.");
			}
		}
	}

	private static int loadMissingDemoMapPresetAreas(ServerCommandSource source, MineFluenceWorldState state) {
		return loadDemoMapPreset(source, state, false);
	}

	private static int loadDemoMapPreset(ServerCommandSource source, MineFluenceWorldState state, boolean overwriteExisting) {
		int loaded = MineFluenceDemoMapPreset.loadInto(state, overwriteExisting);

		if (loaded <= 0) {
			return 0;
		}

		String areaNames = MineFluenceDemoMapPreset.areaNameList();
		MineFluenceDisplay.sendChat(source, overwriteExisting
				? "Loaded new map area preset: " + areaNames + ". Existing definitions were overwritten."
				: "Loaded missing area preset definitions: " + areaNames + ".");
		return loaded;
	}

	private static MineFluenceAreaType firstMissingArea(MineFluenceWorldState state) {
		for (MineFluenceAreaType type : REQUIRED_DEMO_AREAS) {
			if (state.getArea(type) == null) {
				return type;
			}
		}
		return null;
	}

	private static String areaChecklistLine(MineFluenceWorldState state, MineFluenceAreaType type) {
		MineFluenceArea area = state.getArea(type);
		if (area == null) {
			return type.displayName() + " area: MISSING";
		}
		return type.displayName() + " area: " + area.describe();
	}

	private static String missionStateForCheck(MineFluencePlayerData data) {
		if (data.hasPendingMissionSelection()) {
			return "Mission " + data.getPendingMissionSelectionIndex() + " waiting for Good/Bad selection";
		}
		if (data.isWaitingForPostingChoice()) {
			return "Mission " + data.getPendingPostingMissionIndex() + " " + data.getPendingPostingMissionRoute() + " ready to post";
		}
		if (data.hasActiveMission()) {
			return "Mission " + data.getActiveMissionIndex() + " " + data.getActiveMissionRoute() + " active";
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return "All missions complete";
		}
		return "No active mission";
	}

	private static String suggestedNextCommand(MineFluenceWorldState state, MineFluencePlayerData data) {
		if (data.getSelectedJob() != MineFluenceJob.FARMER) {
			return "/minefluence demo setup";
		}

		MineFluenceAreaType missingArea = firstMissingArea(state);
		if (missingArea != null) {
			return "/minefluence area load_preset";
		}
		if (data.hasActiveInvasion()) {
			return "Defeat invasion mobs or /minefluence invasion stop_debug";
		}
		if (data.hasPendingMissionSelection()) {
			return "/minefluence mission choose good or /minefluence mission choose bad";
		}
		if (data.isWaitingForPostingChoice()) {
			return "/minefluence post normal or /minefluence post exaggerate";
		}
		if (data.hasActiveMission()) {
			return "Complete the objective or /minefluence demo skip_mission";
		}
		if (data.getCompletedMissionCount() < MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return "/minefluence mission next";
		}
		if (!data.isEndingTriggered() && data.getLastCompletedInvasionIndex() >= MineFluenceBalance.LAST_COMPLETED_INVASION_MAX) {
			return "/minefluence ending trigger_debug";
		}
		if (data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS
				&& data.getLastCompletedInvasionIndex() < MineFluenceBalance.LAST_COMPLETED_INVASION_MAX) {
			return "/minefluence invasion start 3";
		}
		if (data.isEndingTriggered()) {
			return "Demo route complete";
		}
		return "/minefluence status";
	}

	private static void startInvasionIfDue(ServerPlayerEntity player, MineFluencePlayerData data) {
		int invasionIndex = MineFluenceBalance.invasionIndexForCompletedMissionCount(data.getCompletedMissionCount());
		if (invasionIndex <= 0) {
			return;
		}

		MineFluenceInvasionManager.startInvasion(player, data, invasionIndex);
	}

	private static MineFluenceMission missionOrFallback(int missionIndex) {
		return missionOrFallback(missionIndex, MineFluenceMissionRoute.GOOD);
	}

	private static MineFluenceMission missionOrFallback(int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceMissionRoute resolvedRoute = route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route;
		return FarmerMissions.getMission(missionIndex, resolvedRoute)
				.orElseGet(() -> new MineFluenceMission(
						"unknown_" + missionIndex,
						missionIndex,
						resolvedRoute,
						"Unknown Mission",
						"No mission definition exists for this index.",
						"Reset or start the next valid Farmer mission.",
						1,
						0,
						0
				));
	}

	private static String shortStats(MineFluencePlayerData data) {
		return "Follower=" + data.getFollower()
				+ ", Social Credibility=" + data.getSocialCredibility()
				+ ", Missions=" + data.getCompletedMissionCount()
				+ ", Job=" + data.getSelectedJob()
				+ ", Pending Mission Selection=" + data.getPendingMissionSelectionIndex()
				+ ", Active Mission=" + data.getActiveMissionIndex()
				+ ", Active Mission Route=" + data.getActiveMissionRoute()
				+ ", Pending Posting Mission=" + data.getPendingPostingMissionIndex()
				+ ", Pending Posting Route=" + data.getPendingPostingMissionRoute()
				+ ", Active Mission Progress=" + data.getActiveMissionProgress()
				+ ", Mission Baseline=" + data.getMissionBaselineValue()
				+ ", Active Invasion=" + data.getActiveInvasionIndex()
				+ ", Last Completed Invasion=" + data.getLastCompletedInvasionIndex()
				+ ", Tracked Invasion Mobs=" + data.getTrackedInvasionMobCount()
				+ ", Stored Weapon Tier=" + data.getCurrentWeaponTier()
				+ ", Current Weapon Tier=" + MineFluenceWeaponManager.determineTier(data.getFollower())
				+ ", Ending Triggered=" + data.isEndingTriggered()
				+ ", Ending Id=" + data.getEndingId()
				+ ", Exposure Triggered=" + data.isExposureTriggered()
				+ ", Calculated Follower Ending Tier=" + MineFluenceEndingManager.calculateFollowerTier(data.getFollower())
				+ ", Calculated Social Ending Tier=" + MineFluenceEndingManager.calculateSocialTier(data.getSocialCredibility())
				+ ", Lie Value(debug hidden stat)=" + data.getLieValue()
				+ ", Lie Risk=" + MineFluenceBalance.getLieRiskLabel(data.getLieValue());
	}

	private static MineFluenceEndingTier parseEndingTier(ServerCommandSource source, String value, String label) {
		try {
			return MineFluenceEndingTier.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			MineFluenceDisplay.sendChat(source, label + " tier must be low, mid, or high.");
			return null;
		}
	}

	private static String signed(int value) {
		if (value >= 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}
}
