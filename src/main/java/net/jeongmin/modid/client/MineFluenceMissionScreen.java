package net.jeongmin.modid.client;

import java.util.List;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.network.MineFluenceClientNetworking;
import net.jeongmin.modid.network.MineFluenceMissionBoardResponsePayload;
import net.jeongmin.modid.network.MineFluenceMissionBoardResponsePayload.MissionOptionData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MineFluenceMissionScreen extends Screen {
	private static final Identifier GOOD_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/good_icon.png");
	private static final Identifier BAD_ICON = Identifier.of(MineFluence.MOD_ID, "textures/gui/hud/bad_icon.png");
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFBAC2CC;
	private static final int CARD_BACKGROUND = 0xCC11151B;
	private static final int SCREEN_OVERLAY = 0x66000000;
	private static final int DIVIDER_COLOR = 0x668A929C;
	private static final int GOOD_BORDER = 0xFF4EBD73;
	private static final int BAD_BORDER = 0xFFD76363;
	private static final int BUTTON_WIDTH = 116;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ICON_SIZE = 74;

	private MineFluenceMissionBoardResponsePayload board;
	private String localMessage = "Loading mission board...";
	private boolean requested;
	private ButtonWidget goodButton;
	private ButtonWidget badButton;
	private ButtonWidget closeButton;

	public MineFluenceMissionScreen() {
		super(Text.literal("MineFluence Mission Board"));
		MineFluenceMissionClientState.clearBoard();
	}

	public void updateBoard(MineFluenceMissionBoardResponsePayload board) {
		this.board = board;
		this.localMessage = "";
		if (client != null) {
			clearAndInit();
		}
	}

	@Override
	protected void init() {
		MissionLayout layout = layout();
		goodButton = ButtonWidget.builder(Text.literal("Select Good"), button -> choose(MineFluenceMissionRoute.GOOD))
				.dimensions(layout.goodButtonX(), layout.buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		badButton = ButtonWidget.builder(Text.literal("Select Bad"), button -> choose(MineFluenceMissionRoute.BAD))
				.dimensions(layout.badButtonX(), layout.buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		closeButton = ButtonWidget.builder(Text.literal("Close"), button -> close())
				.dimensions(width / 2 - 43, layout.closeButtonY(), 86, BUTTON_HEIGHT)
				.build();

		boolean canChoose = board != null && board.canChoose() && board.showOptions();
		goodButton.active = canChoose;
		badButton.active = canChoose;
		addDrawableChild(goodButton);
		addDrawableChild(badButton);
		addDrawableChild(closeButton);

		if (!requested) {
			requested = true;
			if (!MineFluenceClientNetworking.requestMissionBoard()) {
				localMessage = "Join a world with MineFluence loaded to use the mission board.";
			}
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		context.fill(0, 0, width, height, SCREEN_OVERLAY);

		if (board == null) {
			drawLargeCenteredText(context, screenTitleText(), width / 2, 28, TEXT_COLOR, 2.0F);
			drawCenteredMessage(context, localMessage.isBlank() ? "Loading mission board..." : localMessage, 72);
			renderButtons(context, mouseX, mouseY, delta);
			return;
		}

		MissionLayout layout = layout();
		if (board.showOptions()) {
			drawCardBackground(context, layout.goodCardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight());
			drawCardBackground(context, layout.badCardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight());
			drawCardBorder(context, layout.goodCardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight(), GOOD_BORDER);
			drawCardBorder(context, layout.badCardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight(), BAD_BORDER);
			drawCardIcon(context, GOOD_ICON, layout.goodCardX(), layout.cardY(), layout.cardWidth());
			drawCardIcon(context, BAD_ICON, layout.badCardX(), layout.cardY(), layout.cardWidth());
		}

		drawLargeCenteredText(context, screenTitleText(), width / 2, 28, TEXT_COLOR, 2.0F);
		drawCenteredMessage(context, board.message(), 64);

		if (board.showOptions()) {
			drawMissionCardText(context, board.goodOption(), layout.goodCardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight(), GOOD_BORDER);
			drawMissionCardText(context, board.badOption(), layout.badCardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight(), BAD_BORDER);
		}

		renderButtons(context, mouseX, mouseY, delta);
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean showRouteButtons = board != null && board.showOptions();
		if (showRouteButtons && goodButton != null) {
			goodButton.render(context, mouseX, mouseY, delta);
		}
		if (showRouteButtons && badButton != null) {
			badButton.render(context, mouseX, mouseY, delta);
		}
		if (closeButton != null) {
			closeButton.render(context, mouseX, mouseY, delta);
		}
	}

	private void choose(MineFluenceMissionRoute route) {
		if (MineFluenceClientNetworking.chooseMission(route)) {
			close();
			return;
		}

		localMessage = "Server mission selection channel is not available.";
		if (client != null) {
			clearAndInit();
		}
	}

	private String screenTitleText() {
		if (board == null || board.missionIndex() <= 0) {
			return "Mission";
		}
		return "Mission " + board.missionIndex();
	}

	private MissionLayout layout() {
		int desiredGap = width >= 760 ? 60 : 24;
		int availableWidth = Math.max(300, width - 64);
		int cardWidth = Math.min(330, (availableWidth - desiredGap) / 2);
		int gap = desiredGap;
		if (cardWidth < 210) {
			gap = 12;
			cardWidth = Math.max(150, (availableWidth - gap) / 2);
		}

		int cardHeight = Math.min(290, Math.max(220, height - 190));
		int cardY = Math.max(86, (height - cardHeight) / 2 - 4);
		int buttonY = cardY + cardHeight + 12;
		if (buttonY + 50 > height) {
			cardY = Math.max(58, height - cardHeight - 62);
			buttonY = cardY + cardHeight + 8;
		}

		int totalWidth = cardWidth * 2 + gap;
		int goodCardX = (width - totalWidth) / 2;
		int badCardX = goodCardX + cardWidth + gap;
		int closeButtonY = Math.min(height - BUTTON_HEIGHT - 8, buttonY + 28);
		return new MissionLayout(
				cardWidth,
				cardHeight,
				goodCardX,
				badCardX,
				cardY,
				goodCardX + (cardWidth - BUTTON_WIDTH) / 2,
				badCardX + (cardWidth - BUTTON_WIDTH) / 2,
				buttonY,
				closeButtonY
		);
	}

	private void drawLargeCenteredText(DrawContext context, String text, int centerX, int y, int color, float scale) {
		context.getMatrices().push();
		context.getMatrices().scale(scale, scale, 1.0F);
		context.drawCenteredTextWithShadow(textRenderer, text, Math.round(centerX / scale), Math.round(y / scale), color);
		context.getMatrices().pop();
	}

	private void drawCardBackground(DrawContext context, int x, int y, int width, int height) {
		context.fill(x, y, x + width, y + height, CARD_BACKGROUND);
	}

	private void drawCardBorder(DrawContext context, int x, int y, int width, int height, int borderColor) {
		context.drawBorder(x, y, width, height, borderColor);
		context.drawBorder(x + 1, y + 1, width - 2, height - 2, borderColor);
	}

	private void drawCardIcon(DrawContext context, Identifier icon, int x, int y, int width) {
		context.drawTexture(icon, x + (width - ICON_SIZE) / 2, y + 22, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
	}

	private void drawMissionCardText(DrawContext context, MissionOptionData option, int x, int y, int width, int height, int accentColor) {
		int centerX = x + width / 2;
		String route = routeLabel(option.route());
		context.drawCenteredTextWithShadow(textRenderer, route, centerX, y + 104, accentColor);

		int objectiveTop = y + 128;
		int objectiveHeight = Math.max(38, height - 210);
		drawCenteredWrappedText(context, option.objectiveText(), centerX, objectiveTop, width - 42, objectiveHeight, TEXT_COLOR);

		int dividerY = y + height - 96;
		context.fill(x + 34, dividerY, x + width - 34, dividerY + 1, DIVIDER_COLOR);
		drawRewardRow(context, "Followers", signed(option.followerReward()), x, dividerY + 18, width, accentColor);
		drawRewardRow(context, "Social Credibility", signed(option.socialCredibilityReward()), x, dividerY + 42, width, rewardColor(option.socialCredibilityReward(), accentColor));
	}

	private void drawRewardRow(DrawContext context, String label, String value, int x, int y, int width, int valueColor) {
		int labelX = x + 46;
		int valueWidth = textRenderer.getWidth(value);
		context.drawTextWithShadow(textRenderer, label, labelX, y, MUTED_TEXT_COLOR);
		context.drawTextWithShadow(textRenderer, value, x + width - 46 - valueWidth, y, valueColor);
	}

	private void drawCenteredWrappedText(DrawContext context, String value, int centerX, int y, int maxWidth, int maxHeight, int color) {
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal(safeText(value)), Math.max(40, maxWidth));
		int lineHeight = 12;
		int maxLines = Math.max(1, maxHeight / lineHeight);
		int lineCount = Math.min(lines.size(), maxLines);
		for (int index = 0; index < lineCount; index++) {
			OrderedText line = lines.get(index);
			int lineWidth = textRenderer.getWidth(line);
			context.drawTextWithShadow(textRenderer, line, centerX - lineWidth / 2, y + index * lineHeight, color);
		}
	}

	private void drawCenteredMessage(DrawContext context, String message, int y) {
		context.drawCenteredTextWithShadow(textRenderer, trim(message, Math.max(80, width - 40)), width / 2, y, MUTED_TEXT_COLOR);
	}

	private String trim(String value, int maxWidth) {
		String safeValue = value == null ? "" : value;
		return textRenderer.trimToWidth(safeValue, Math.max(20, maxWidth));
	}

	private static String routeLabel(String route) {
		if ("BAD".equalsIgnoreCase(route)) {
			return "Bad";
		}
		return "Good";
	}

	private static int rewardColor(int value, int defaultColor) {
		if (value < 0) {
			return BAD_BORDER;
		}
		return defaultColor;
	}

	private static String safeText(String value) {
		if (value == null || value.isBlank()) {
			return "No objective available.";
		}
		return value;
	}

	private static String signed(int value) {
		if (value >= 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}

	private record MissionLayout(
			int cardWidth,
			int cardHeight,
			int goodCardX,
			int badCardX,
			int cardY,
			int goodButtonX,
			int badButtonX,
			int buttonY,
			int closeButtonY
	) {
	}
}
