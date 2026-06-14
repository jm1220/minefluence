package net.jeongmin.modid.client;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.network.MineFluenceClientNetworking;
import net.jeongmin.modid.network.MineFluencePhoneStateResponsePayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MineFluencePostingScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFE8E8E8;
	private static final int MUTED_TEXT_COLOR = 0xFFB8B8B8;
	private static final int CARD_BACKGROUND = 0xCC20242A;
	private static final int NORMAL_BORDER = 0xFF4EBD73;
	private static final int EXAGGERATED_BORDER = 0xFFD7B763;

	private final MineFluencePhoneStateResponsePayload state;
	private String localMessage;
	private ButtonWidget normalButton;
	private ButtonWidget exaggeratedButton;
	private ButtonWidget closeButton;

	public MineFluencePostingScreen(MineFluencePhoneStateResponsePayload state) {
		super(Text.literal("MineFluence Upload"));
		this.state = state;
		this.localMessage = state.message();
	}

	@Override
	protected void init() {
		int buttonY = height - 32;
		int centerX = width / 2;
		normalButton = ButtonWidget.builder(Text.literal("Post Normally"), button -> post(false))
				.dimensions(centerX - 155, buttonY, 100, 20)
				.build();
		exaggeratedButton = ButtonWidget.builder(Text.literal("Post Exaggerated"), button -> post(true))
				.dimensions(centerX - 45, buttonY, 120, 20)
				.build();
		closeButton = ButtonWidget.builder(Text.literal("Close"), button -> close())
				.dimensions(centerX + 85, buttonY, 70, 20)
				.build();

		boolean canPost = MineFluencePhoneStateResponsePayload.STATE_READY_TO_UPLOAD.equals(state.state());
		normalButton.active = canPost;
		exaggeratedButton.active = canPost;
		addDrawableChild(normalButton);
		addDrawableChild(exaggeratedButton);
		addDrawableChild(closeButton);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);

		int availableWidth = Math.max(220, width - 48);
		int cardGap = 12;
		int cardWidth = Math.min(230, (availableWidth - cardGap) / 2);
		int cardHeight = 72;
		int totalWidth = cardWidth * 2 + cardGap;
		int leftX = (width - totalWidth) / 2;
		int topY = Math.min(height - 120, 142);
		int exaggeratedCardX = leftX + cardWidth + cardGap;

		drawRewardCardFrame(context, leftX, topY, cardWidth, cardHeight, NORMAL_BORDER);
		drawRewardCardFrame(context, exaggeratedCardX, topY, cardWidth, cardHeight, EXAGGERATED_BORDER);

		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, TEXT_COLOR);
		context.drawCenteredTextWithShadow(textRenderer, "Mission " + state.missionIndex() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " - " + state.route(), width / 2, 40, TEXT_COLOR);
		context.drawCenteredTextWithShadow(textRenderer, trim(state.title(), width - 40), width / 2, 56, TEXT_COLOR);
		drawCenteredMessage(context, localMessage, 74);
		context.drawTextWrapped(textRenderer, Text.literal("Objective: " + state.objectiveText()), 24, 94, Math.max(80, width - 48), MUTED_TEXT_COLOR);
		drawRewardCardText(
				context,
				"Normal Post",
				state.normalFollowerReward(),
				state.normalSocialCredibilityReward(),
				state.normalLieValueIncrease(),
				leftX,
				topY,
				NORMAL_BORDER
		);
		drawRewardCardText(
				context,
				"Exaggerated Post",
				state.exaggeratedFollowerReward(),
				state.exaggeratedSocialCredibilityReward(),
				state.exaggeratedLieValueIncrease(),
				exaggeratedCardX,
				topY,
				EXAGGERATED_BORDER
		);

		renderButtons(context, mouseX, mouseY, delta);
	}

	private void post(boolean exaggerated) {
		if (MineFluenceClientNetworking.postMission(exaggerated)) {
			close();
			return;
		}

		localMessage = "Server posting channel is not available.";
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		if (normalButton != null) {
			normalButton.render(context, mouseX, mouseY, delta);
		}
		if (exaggeratedButton != null) {
			exaggeratedButton.render(context, mouseX, mouseY, delta);
		}
		if (closeButton != null) {
			closeButton.render(context, mouseX, mouseY, delta);
		}
	}

	private void drawRewardCardFrame(DrawContext context, int x, int y, int width, int height, int borderColor) {
		context.fill(x, y, x + width, y + height, CARD_BACKGROUND);
		context.drawBorder(x, y, width, height, borderColor);
	}

	private void drawRewardCardText(DrawContext context, String label, int follower, int social, int lie, int x, int y, int borderColor) {
		context.drawTextWithShadow(textRenderer, label, x + 8, y + 8, borderColor);
		context.drawTextWithShadow(textRenderer, "Follower " + signed(follower), x + 8, y + 26, TEXT_COLOR);
		context.drawTextWithShadow(textRenderer, "Social " + signed(social), x + 8, y + 40, TEXT_COLOR);
		context.drawTextWithShadow(textRenderer, "Lie Risk: " + lieRiskPreview(lie), x + 8, y + 54, MUTED_TEXT_COLOR);
	}

	private void drawCenteredMessage(DrawContext context, String message, int y) {
		context.drawCenteredTextWithShadow(textRenderer, trim(message, Math.max(80, width - 40)), width / 2, y, MUTED_TEXT_COLOR);
	}

	private String trim(String value, int maxWidth) {
		String safeValue = value == null ? "" : value;
		return textRenderer.trimToWidth(safeValue, Math.max(20, maxWidth));
	}

	private static String signed(int value) {
		if (value >= 0) {
			return "+" + value;
		}
		return Integer.toString(value);
	}

	private static String lieRiskPreview(int lieIncrease) {
		if (lieIncrease <= 0) {
			return "None";
		}
		return "Hidden increase";
	}
}
