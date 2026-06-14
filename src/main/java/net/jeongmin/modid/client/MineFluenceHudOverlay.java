package net.jeongmin.modid.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.network.MineFluenceHudStatePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class MineFluenceHudOverlay {
	private static final boolean SHOW_RIGHT_STAT_PANEL = false;
	private static final boolean SHOW_MISSION_LOCATOR_BAR = true;
	private static final double MISSION_LOCATOR_FOV_DEGREES = 120.0;
	private static final double MISSION_LOCATOR_NEAR_DISTANCE = 5.0;
	private static final int MISSION_LOCATOR_BAR_WIDTH = 182;

	private static final Identifier GOOD_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/good_icon.png");
	private static final Identifier BAD_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/bad_icon.png");
	private static final Identifier INVASION_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/invasion_icon.png");

	private static final int CARD_WIDTH = 250;
	private static final int OBJECTIVE_HEIGHT = 58;
	private static final int STAT_ROW_HEIGHT = 22;
	private static final int ICON_SIZE = 30;
	private static final int SMALL_ICON_SIZE = 16;
	private static final float HOTBAR_STAT_SCALE = 1.2F;
	private static final int COMPACT_STAT_ICON_SIZE = 18;
	private static final int COMPACT_STAT_TEXT_GAP = 5;
	private static final int COMPACT_STAT_GROUP_GAP = 20;
	private static final int SCREEN_MARGIN = 12;
	private static final int COMPACT_STATS_BOTTOM_OFFSET = 90;
	private static final int COMPACT_STATS_WITH_LOCATOR_BOTTOM_OFFSET = 90;
	private static final int MISSION_LOCATOR_BOTTOM_OFFSET = 49;
	private static final int CARD_BACKGROUND = 0xB812151B;
	private static final int CARD_BORDER = 0x80394149;
	private static final int BAR_BACKGROUND = 0x80272931;
	private static final int LOCATOR_BAR_BACKGROUND = 0xA020242A;
	private static final int LOCATOR_BAR_LINE = 0xB06D7680;
	private static final int LOCATOR_MARKER_BORDER = 0xE0101216;
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

		if (SHOW_RIGHT_STAT_PANEL) {
			int statsY = drewObjective ? y + OBJECTIVE_HEIGHT + 8 : y;
			drawStatPanel(context, client.textRenderer, state, x, statsY, cardWidth);
		}

		MissionLocatorTarget locatorTarget = missionLocatorTarget(client, state);
		drawCompactStats(context, client.textRenderer, state, locatorTarget != null);
		if (locatorTarget != null) {
			drawMissionLocator(context, state, locatorTarget);
		}
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
		int rowCount = 2;
		int height = 12 + rowCount * STAT_ROW_HEIGHT;
		drawCard(context, x, y, width, height, CARD_BORDER);

		int rowY = y + 8;
		drawStatRow(context, textRenderer, x, rowY, width, BAD_ICON, "Followers", Integer.toString(state.follower()), BAD_COLOR);
		drawStatRow(context, textRenderer, x, rowY + STAT_ROW_HEIGHT, width, GOOD_ICON, "Social Credibility", signed(state.socialCredibility()), GOOD_COLOR);
	}

	private static void drawCompactStats(
			DrawContext context,
			TextRenderer textRenderer,
			MineFluenceHudStatePayload state,
			boolean locatorVisible
	) {
		String followerText = Integer.toString(state.follower());
		String trustText = signed(state.socialCredibility());
		int followerTextWidth = scaledTextWidth(textRenderer, followerText);
		int trustTextWidth = scaledTextWidth(textRenderer, trustText);
		int followerWidth = COMPACT_STAT_ICON_SIZE + COMPACT_STAT_TEXT_GAP + followerTextWidth;
		int trustWidth = COMPACT_STAT_ICON_SIZE + COMPACT_STAT_TEXT_GAP + trustTextWidth;
		int totalWidth = followerWidth + COMPACT_STAT_GROUP_GAP + trustWidth;
		int x = (context.getScaledWindowWidth() - totalWidth) / 2;
		int bottomOffset = locatorVisible ? COMPACT_STATS_WITH_LOCATOR_BOTTOM_OFFSET : COMPACT_STATS_BOTTOM_OFFSET;
		int y = Math.max(8, context.getScaledWindowHeight() - bottomOffset);

		drawIcon(context, BAD_ICON, x, y, COMPACT_STAT_ICON_SIZE);
		drawScaledText(
				context,
				textRenderer,
				followerText,
				x + COMPACT_STAT_ICON_SIZE + COMPACT_STAT_TEXT_GAP,
				y + 3,
				BAD_COLOR
		);

		int trustX = x + followerWidth + COMPACT_STAT_GROUP_GAP;
		drawIcon(context, GOOD_ICON, trustX, y, COMPACT_STAT_ICON_SIZE);
		drawScaledText(
				context,
				textRenderer,
				trustText,
				trustX + COMPACT_STAT_ICON_SIZE + COMPACT_STAT_TEXT_GAP,
				y + 3,
				GOOD_COLOR
		);
	}

	private static int scaledTextWidth(TextRenderer textRenderer, String text) {
		return Math.round(textRenderer.getWidth(text) * HOTBAR_STAT_SCALE);
	}

	private static void drawScaledText(
			DrawContext context,
			TextRenderer textRenderer,
			String text,
			int x,
			int y,
			int color
	) {
		int scaledX = Math.round(x / HOTBAR_STAT_SCALE);
		int scaledY = Math.round(y / HOTBAR_STAT_SCALE);
		context.getMatrices().push();
		context.getMatrices().scale(HOTBAR_STAT_SCALE, HOTBAR_STAT_SCALE, 1.0F);
		context.drawTextWithShadow(textRenderer, text, scaledX, scaledY, color);
		context.getMatrices().pop();
	}

	private static MissionLocatorTarget missionLocatorTarget(
			MinecraftClient client,
			MineFluenceHudStatePayload state
	) {
		if (!SHOW_MISSION_LOCATOR_BAR
				|| !state.hasActiveMissionArea()
				|| state.activeMissionIndex() <= 0
				|| state.waitingForPosting()
				|| state.activeMissionProgress() >= state.activeMissionTarget()
				|| state.activeMissionAreaName().isBlank()
				|| state.activeMissionAreaDimension().isBlank()) {
			return null;
		}

		String currentDimension = client.world.getRegistryKey().getValue().toString();
		if (!state.activeMissionAreaDimension().equals(currentDimension)) {
			return null;
		}

		double centerX = (state.activeMissionAreaMinX() + state.activeMissionAreaMaxX()) / 2.0;
		double centerY = (state.activeMissionAreaMinY() + state.activeMissionAreaMaxY()) / 2.0;
		double centerZ = (state.activeMissionAreaMinZ() + state.activeMissionAreaMaxZ()) / 2.0;
		double deltaX = centerX - client.player.getX();
		double deltaZ = centerZ - client.player.getZ();
		double horizontalDistance = Math.hypot(deltaX, deltaZ);
		boolean insideArea = isInsideArea(client, state);
		boolean nearby = insideArea || horizontalDistance <= MISSION_LOCATOR_NEAR_DISTANCE;

		double deltaAngle = 0.0;
		if (!nearby && horizontalDistance > 0.0001) {
			double angleToTarget = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
			double cameraYaw = client.gameRenderer.getCamera().getYaw();
			deltaAngle = MathHelper.wrapDegrees(angleToTarget - cameraYaw);
		}

		return new MissionLocatorTarget(centerY - client.player.getY(), horizontalDistance, deltaAngle, nearby);
	}

	private static boolean isInsideArea(MinecraftClient client, MineFluenceHudStatePayload state) {
		double playerX = client.player.getX();
		double playerY = client.player.getY();
		double playerZ = client.player.getZ();
		return playerX >= state.activeMissionAreaMinX()
				&& playerX < state.activeMissionAreaMaxX() + 1.0
				&& playerY >= state.activeMissionAreaMinY()
				&& playerY < state.activeMissionAreaMaxY() + 1.0
				&& playerZ >= state.activeMissionAreaMinZ()
				&& playerZ < state.activeMissionAreaMaxZ() + 1.0;
	}

	private static void drawMissionLocator(
			DrawContext context,
			MineFluenceHudStatePayload state,
			MissionLocatorTarget target
	) {
		int centerX = context.getScaledWindowWidth() / 2;
		int barY = context.getScaledWindowHeight() - MISSION_LOCATOR_BOTTOM_OFFSET;
		int barLeft = centerX - MISSION_LOCATOR_BAR_WIDTH / 2;
		double halfFov = MISSION_LOCATOR_FOV_DEGREES / 2.0;
		double normalizedAngle = MathHelper.clamp(target.deltaAngle() / halfFov, -1.0, 1.0);
		int markerX = target.nearby()
				? centerX
				: centerX + (int) Math.round(normalizedAngle * (MISSION_LOCATOR_BAR_WIDTH / 2.0));
		int markerRadius = target.horizontalDistance() <= 10.0 ? 4
				: target.horizontalDistance() <= 30.0 ? 3 : 2;
		int markerColor = "BAD".equalsIgnoreCase(state.activeMissionRoute()) ? BAD_COLOR : GOOD_COLOR;

		context.fill(barLeft, barY - 1, barLeft + MISSION_LOCATOR_BAR_WIDTH, barY + 2, LOCATOR_BAR_BACKGROUND);
		context.fill(barLeft + 1, barY, barLeft + MISSION_LOCATOR_BAR_WIDTH - 1, barY + 1, LOCATOR_BAR_LINE);
		context.fill(barLeft, barY - 3, barLeft + 1, barY + 4, LOCATOR_BAR_LINE);
		context.fill(
				barLeft + MISSION_LOCATOR_BAR_WIDTH - 1,
				barY - 3,
				barLeft + MISSION_LOCATOR_BAR_WIDTH,
				barY + 4,
				LOCATOR_BAR_LINE
		);
		drawDiamond(context, markerX, barY, markerRadius + 1, LOCATOR_MARKER_BORDER);
		drawDiamond(context, markerX, barY, markerRadius, markerColor);
		if (Math.abs(target.verticalDelta()) > 5.0) {
			int hintX = markerX >= centerX
					? markerX - markerRadius - 5
					: markerX + markerRadius + 5;
			drawVerticalHint(context, hintX, barY, target.verticalDelta() > 0.0, TEXT_COLOR);
		}
	}

	private static void drawDiamond(DrawContext context, int centerX, int centerY, int radius, int color) {
		for (int yOffset = -radius; yOffset <= radius; yOffset++) {
			int halfWidth = radius - Math.abs(yOffset);
			context.fill(
					centerX - halfWidth,
					centerY + yOffset,
					centerX + halfWidth + 1,
					centerY + yOffset + 1,
					color
			);
		}
	}

	private static void drawVerticalHint(DrawContext context, int x, int y, boolean pointsUp, int color) {
		if (pointsUp) {
			context.fill(x, y - 3, x + 1, y + 3, color);
			context.fill(x - 1, y - 2, x + 2, y - 1, color);
			context.fill(x - 2, y - 1, x + 3, y, color);
			return;
		}

		context.fill(x, y - 2, x + 1, y + 4, color);
		context.fill(x - 1, y + 1, x + 2, y + 2, color);
		context.fill(x - 2, y + 2, x + 3, y + 3, color);
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
		if (value > 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}

	private record MissionLocatorTarget(
			double verticalDelta,
			double horizontalDistance,
			double deltaAngle,
			boolean nearby
	) {
	}

}
