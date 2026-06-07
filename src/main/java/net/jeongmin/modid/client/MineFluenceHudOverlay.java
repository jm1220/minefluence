package net.jeongmin.modid.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.network.MineFluenceHudStatePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class MineFluenceHudOverlay {
	private static final boolean SHOW_LIE_GAUGE = true;

	private static final Identifier GOOD_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/good_icon.png");
	private static final Identifier BAD_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/bad_icon.png");
	private static final Identifier INVASION_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/invasion_icon.png");
	private static final Identifier LIE_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/lie_icon.png");

	private static final int CARD_WIDTH = 250;
	private static final int OBJECTIVE_HEIGHT = 58;
	private static final int STAT_ROW_HEIGHT = 22;
	private static final int ICON_SIZE = 30;
	private static final int SMALL_ICON_SIZE = 16;
	private static final int SCREEN_MARGIN = 12;
	private static final int CARD_BACKGROUND = 0xB812151B;
	private static final int CARD_BORDER = 0x80394149;
	private static final int BAR_BACKGROUND = 0x80272931;
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFBAC2CC;
	private static final int GOOD_COLOR = 0xFF4EBD73;
	private static final int BAD_COLOR = 0xFFD76363;
	private static final int INVASION_COLOR = 0xFFD7B763;

	private MineFluenceHudOverlay() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
	}

	private static void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (shouldSkip(client)) {
			return;
		}

		MineFluenceHudStatePayload state = MineFluenceHudState.snapshot();
		if (state == null) {
			return;
		}

		int screenWidth = context.getScaledWindowWidth();
		int cardWidth = Math.min(CARD_WIDTH, Math.max(160, screenWidth - SCREEN_MARGIN * 2));
		int x = Math.max(8, screenWidth - cardWidth - SCREEN_MARGIN);
		int y = SCREEN_MARGIN;
		boolean drewObjective = drawObjectiveCard(context, client.textRenderer, state, x, y, cardWidth);

		int statsY = drewObjective ? y + OBJECTIVE_HEIGHT + 8 : y;
		drawStatPanel(context, client.textRenderer, state, x, statsY, cardWidth);
	}

	private static boolean shouldSkip(MinecraftClient client) {
		return client.player == null
				|| client.world == null
				|| client.currentScreen != null
				|| client.options.hudHidden
				|| client.inGameHud.getDebugHud().shouldShowDebugHud();
	}

	private static boolean drawObjectiveCard(DrawContext context, TextRenderer textRenderer, MineFluenceHudStatePayload state, int x, int y, int width) {
		if (state.activeInvasionIndex() > 0) {
			int total = Math.max(1, Math.max(state.invasionTotal(), state.invasionRemaining()));
			int defeated = Math.max(0, total - Math.max(0, state.invasionRemaining()));
			drawObjectiveCard(
					context,
					textRenderer,
					x,
					y,
					width,
					INVASION_ICON,
					INVASION_COLOR,
					"Under Invasion",
					"Defeat all monsters",
					defeated + "/" + total,
					defeated,
					total
			);
			return true;
		}

		if (state.activeMissionIndex() > 0) {
			boolean badRoute = "BAD".equalsIgnoreCase(state.activeMissionRoute());
			int accentColor = badRoute ? BAD_COLOR : GOOD_COLOR;
			Identifier icon = badRoute ? BAD_ICON : GOOD_ICON;
			String route = badRoute ? "Bad" : "Good";
			int target = Math.max(1, state.activeMissionTarget());
			int progress = Math.min(Math.max(0, state.activeMissionProgress()), target);
			drawObjectiveCard(
					context,
					textRenderer,
					x,
					y,
					width,
					icon,
					accentColor,
					"Mission " + state.activeMissionIndex() + " - " + route,
					state.activeMissionObjective(),
					progress + "/" + target,
					progress,
					target
			);
			return true;
		}

		return false;
	}

	private static void drawObjectiveCard(
			DrawContext context,
			TextRenderer textRenderer,
			int x,
			int y,
			int width,
			Identifier icon,
			int accentColor,
			String title,
			String subtitle,
			String progressText,
			int progress,
			int target
	) {
		drawCard(context, x, y, width, OBJECTIVE_HEIGHT, accentColor);
		drawIcon(context, icon, x + 10, y + 14, ICON_SIZE);

		int textX = x + 50;
		int progressWidth = textRenderer.getWidth(progressText);
		int textWidth = Math.max(32, width - 64 - progressWidth);
		context.drawTextWithShadow(textRenderer, trim(textRenderer, title, textWidth), textX, y + 9, accentColor);
		context.drawTextWithShadow(textRenderer, trim(textRenderer, subtitle, textWidth), textX, y + 25, MUTED_TEXT_COLOR);
		context.drawTextWithShadow(textRenderer, progressText, x + width - 10 - progressWidth, y + 25, TEXT_COLOR);
		drawProgressBar(context, textX, y + 45, Math.max(24, width - 60), 4, accentColor, progress, target);
	}

	private static void drawStatPanel(DrawContext context, TextRenderer textRenderer, MineFluenceHudStatePayload state, int x, int y, int width) {
		int rowCount = SHOW_LIE_GAUGE ? 3 : 2;
		int height = 12 + rowCount * STAT_ROW_HEIGHT;
		drawCard(context, x, y, width, height, CARD_BORDER);

		int rowY = y + 8;
		drawStatRow(context, textRenderer, x, rowY, width, BAD_ICON, "Followers", Integer.toString(state.follower()), BAD_COLOR);
		drawStatRow(context, textRenderer, x, rowY + STAT_ROW_HEIGHT, width, GOOD_ICON, "Social Credibility", signed(state.socialCredibility()), GOOD_COLOR);
		if (SHOW_LIE_GAUGE) {
			drawStatRow(context, textRenderer, x, rowY + STAT_ROW_HEIGHT * 2, width, LIE_ICON, "Lie Gauge", liePercent(state.lieValue()) + "%", INVASION_COLOR);
		}
	}

	private static void drawStatRow(
			DrawContext context,
			TextRenderer textRenderer,
			int x,
			int y,
			int width,
			Identifier icon,
			String label,
			String value,
			int accentColor
	) {
		drawIcon(context, icon, x + 10, y + 1, SMALL_ICON_SIZE);
		int labelX = x + 32;
		int valueWidth = textRenderer.getWidth(value);
		int labelWidth = Math.max(24, width - 48 - valueWidth);
		context.drawTextWithShadow(textRenderer, trim(textRenderer, label, labelWidth), labelX, y + 4, MUTED_TEXT_COLOR);
		context.drawTextWithShadow(textRenderer, value, x + width - 10 - valueWidth, y + 4, accentColor);
	}

	private static void drawCard(DrawContext context, int x, int y, int width, int height, int borderColor) {
		context.fill(x, y, x + width, y + height, CARD_BACKGROUND);
		context.drawBorder(x, y, width, height, borderColor);
	}

	private static void drawIcon(DrawContext context, Identifier icon, int x, int y, int size) {
		context.drawTexture(icon, x, y, 0.0F, 0.0F, size, size, size, size);
	}

	private static void drawProgressBar(DrawContext context, int x, int y, int width, int height, int color, int progress, int target) {
		context.fill(x, y, x + width, y + height, BAR_BACKGROUND);
		if (target <= 0) {
			return;
		}

		int filledWidth = Math.max(0, Math.min(width, progress * width / target));
		if (filledWidth > 0) {
			context.fill(x, y, x + filledWidth, y + height, color);
		}
	}

	private static String trim(TextRenderer textRenderer, String value, int maxWidth) {
		String safeValue = value == null || value.isBlank() ? "No objective" : value;
		return textRenderer.trimToWidth(safeValue, Math.max(20, maxWidth));
	}

	private static String signed(int value) {
		if (value >= 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}

	private static int liePercent(int lieValue) {
		int max = Math.max(1, MineFluenceBalance.LIE_VALUE_MAX);
		return Math.max(0, Math.min(100, lieValue * 100 / max));
	}
}
