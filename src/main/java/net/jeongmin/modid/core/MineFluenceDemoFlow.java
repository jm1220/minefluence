package net.jeongmin.modid.core;

import net.jeongmin.modid.area.MineFluenceDemoMapPreset;
import net.jeongmin.modid.area.MineFluenceAreaGuideManager;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.fan.MineFluenceFanVillagers;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.jeongmin.modid.item.MineFluenceItems;
import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.jeongmin.modid.network.MineFluenceNetworking;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.jeongmin.modid.weapon.MineFluenceWeaponManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluenceDemoFlow {
	private MineFluenceDemoFlow() {
	}

	public static MineFluencePlayerData startDemo(ServerPlayerEntity player, boolean openTutorial) {
		MineFluencePlayerData data = resetStartedDemoProgress(player);
		MineFluenceItems.ensureSingleSmartphone(player);
		loadMissingDemoMapPresetAreas(player);

		MineFluenceDisplay.sendChat(player, "Welcome to MineFluence.");
		MineFluenceDisplay.sendChat(player, "You are an influencer trying to gain followers and social credibility to defend the village.");
		MineFluenceDisplay.sendChat(player, "Every content choice has consequences.");
		MineFluenceDisplay.sendChat(player, "Choose Farmer from the smartphone or with /minefluence choose farmer.");
		MineFluenceDisplay.sendChat(player, "Use your MineFluence Smartphone as the main progression tool.");
		MineFluenceDisplay.sendActionBar(player, "MineFluence Demo Started");
		MineFluenceHud.refresh(player, data);
		if (openTutorial && !MineFluenceNetworking.openTutorial(player)) {
			MineFluenceDisplay.sendChat(player, "Run /minefluence tutorial to view the instructions.");
		}
		return data;
	}

	public static MineFluencePlayerData resetDemoProgress(ServerPlayerEntity player) {
		return resetDemoState(player, false);
	}

	public static MineFluencePlayerData resetStartedDemoProgress(ServerPlayerEntity player) {
		return resetDemoState(player, true);
	}

	public static MineFluencePlayerData chooseFarmer(ServerPlayerEntity player) {
		MineFluencePlayerData data = MineFluenceWorldState.get(player.getServer()).updatePlayerData(player, playerData -> playerData.setSelectedJob(MineFluenceJob.FARMER));
		MineFluenceWeaponManager.updateWeapon(player, data);
		MineFluenceItems.ensureSingleSmartphone(player);

		MineFluenceDisplay.sendChat(player, "Job selected: Farmer.");
		MineFluenceDisplay.sendChat(player, "The demo contains 7 Farmer missions.");
		MineFluenceDisplay.sendChat(player, "Invasions will happen after missions 2, 5, and 7.");
		MineFluenceDisplay.sendChat(player, "Use the smartphone to start missions, choose routes, and upload posts. Slash commands remain available.");
		MineFluenceDisplay.sendStatusActionBar(player, data);
		return data;
	}

	public static int loadMissingDemoMapPresetAreas(ServerPlayerEntity player) {
		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		int loaded = MineFluenceDemoMapPreset.loadInto(state, false);

		if (loaded > 0) {
			MineFluenceDisplay.sendChat(player, "Loaded missing area preset definitions: " + MineFluenceDemoMapPreset.areaNameList() + ".");
		}
		return loaded;
	}

	private static MineFluencePlayerData resetDemoState(ServerPlayerEntity player, boolean demoStarted) {
		MineFluenceWorldState state = MineFluenceWorldState.get(player.getServer());
		MineFluencePlayerData currentData = state.getPlayerData(player);
		MineFluenceInvasionManager.clearInvasionForReset(player, currentData);
		MineFluenceFanVillagers.clearFanVillagers(player);
		MineFluenceAreaGuideManager.clearGuide(player);
		MineFluenceMissionProgressManager.clearPlayerTransientState(player);

		MineFluencePlayerData data = state.updatePlayerData(player, playerData -> {
			if (demoStarted) {
				playerData.resetForDemoStart();
			} else {
				playerData.resetForTutorialStart();
			}
		});
		MineFluenceWeaponManager.updateWeapon(player, data);
		MineFluenceFanVillagers.syncFanVillagers(player);
		MineFluenceHud.refresh(player, data);
		return data;
	}
}
