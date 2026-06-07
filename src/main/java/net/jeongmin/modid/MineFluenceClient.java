package net.jeongmin.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.jeongmin.modid.billboard.MineFluenceBillboardClient;
import net.jeongmin.modid.client.MineFluenceMissionScreen;
import net.jeongmin.modid.client.MineFluenceHudOverlay;
import net.jeongmin.modid.client.MineFluencePhoneInfoScreen;
import net.jeongmin.modid.entity.DdjRenderer;
import net.jeongmin.modid.entity.MineFluenceEntities;
import net.jeongmin.modid.item.MineFluenceItems;
import net.jeongmin.modid.network.MineFluenceClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import org.lwjgl.glfw.GLFW;

public class MineFluenceClient implements ClientModInitializer {
	private static final String MISSION_KEY_TRANSLATION = "key.minefluence.open_mission_tab";
	private static final String MISSION_KEY_CATEGORY = "category.minefluence";

	private KeyBinding openMissionTabKey;

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(MineFluenceEntities.DDJ, DdjRenderer::new);
		MineFluenceBillboardClient.register();
		MineFluenceClientNetworking.register();
		MineFluenceHudOverlay.register();
		registerSmartphoneUse();
		openMissionTabKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				MISSION_KEY_TRANSLATION,
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				MISSION_KEY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMissionTabKey.wasPressed()) {
				openMissionScreen(client);
			}
		});
	}

	private static void registerSmartphoneUse() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!stack.isOf(MineFluenceItems.SMARTPHONE)) {
				return TypedActionResult.pass(stack);
			}

			if (world.isClient()) {
				openSmartphone(MinecraftClient.getInstance());
			}
			return TypedActionResult.success(stack, world.isClient());
		});
	}

	private static void openSmartphone(MinecraftClient client) {
		if (client.currentScreen != null) {
			return;
		}

		if (!MineFluenceClientNetworking.requestPhoneState()) {
			client.setScreen(new MineFluencePhoneInfoScreen("Join a world with MineFluence loaded to use the smartphone."));
		}
	}

	private static void openMissionScreen(MinecraftClient client) {
		if (client.currentScreen == null) {
			client.setScreen(new MineFluenceMissionScreen());
		}
	}
}
