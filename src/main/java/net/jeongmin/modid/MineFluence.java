package net.jeongmin.modid;

import net.fabricmc.api.ModInitializer;
import net.jeongmin.modid.area.MineFluenceAreaGuideManager;
import net.jeongmin.modid.billboard.MineFluenceBillboards;
import net.jeongmin.modid.command.MineFluenceCommands;
import net.jeongmin.modid.entity.MineFluenceEntities;
import net.jeongmin.modid.fan.MineFluenceFanVillagers;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.jeongmin.modid.invasion.MineFluenceInvasionSupportManager;
import net.jeongmin.modid.item.MineFluenceItems;
import net.jeongmin.modid.mission.MineFluenceMissionProgressManager;
import net.jeongmin.modid.network.MineFluenceNetworking;
import net.jeongmin.modid.ui.MineFluenceHud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineFluence implements ModInitializer {
	public static final String MOD_ID = "minefluence";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MineFluenceEntities.register();
		MineFluenceBillboards.register();
		MineFluenceItems.register();
		MineFluenceNetworking.register();
		MineFluenceHud.register();
		MineFluenceCommands.register();
		MineFluenceFanVillagers.register();
		MineFluenceInvasionSupportManager.register();
		MineFluenceInvasionManager.register();
		MineFluenceMissionProgressManager.register();
		MineFluenceAreaGuideManager.register();
		LOGGER.info("MineFluence initialized.");
	}
}
