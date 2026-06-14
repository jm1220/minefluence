package net.jeongmin.modid.client;

import net.jeongmin.modid.network.MineFluencePhoneStateResponsePayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MineFluenceSmartphoneHelpScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFB8C0CA;
	private static final int PANEL_BACKGROUND = 0xCC11151B;
	private static final int PANEL_BORDER = 0x668A929C;
	private static final int PANEL_WIDTH = 320;
	private static final int PANEL_HEIGHT = 245;
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_STEP = 28;

	private final MineFluencePhoneStateResponsePayload state;
	private ButtonWidget replayTutorialButton;
	private ButtonWidget missionHelpButton;
	private ButtonWidget gameSystemHelpButton;
	private ButtonWidget backButton;
	private ButtonWidget closeButton;

	public MineFluenceSmartphoneHelpScreen(MineFluencePhoneStateResponsePayload state) {
		super(Text.literal("MineFluence Help"));
		this.state = state;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int buttonY = height / 2 - 45;
		replayTutorialButton = ButtonWidget.builder(
						Text.literal("Replay Tutorial"),
						button -> replayTutorial()
				)
				.dimensions(centerX - BUTTON_WIDTH / 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		missionHelpButton = ButtonWidget.builder(Text.literal("Mission Help"), button -> openMissionHelp())
				.dimensions(centerX - BUTTON_WIDTH / 2, buttonY + BUTTON_STEP, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		gameSystemHelpButton = ButtonWidget.builder(Text.literal("Game System Help"), button -> openGameSystemHelp())
				.dimensions(centerX - BUTTON_WIDTH / 2, buttonY + BUTTON_STEP * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		backButton = ButtonWidget.builder(Text.literal("Back"), button -> backToHome())
				.dimensions(centerX - BUTTON_WIDTH / 2, buttonY + BUTTON_STEP * 3, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		closeButton = ButtonWidget.builder(Text.literal("Close"), button -> closeToGame())
				.dimensions(centerX - BUTTON_WIDTH / 2, buttonY + BUTTON_STEP * 4, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		addDrawableChild(replayTutorialButton);
		addDrawableChild(missionHelpButton);
		addDrawableChild(gameSystemHelpButton);
		addDrawableChild(backButton);
		addDrawableChild(closeButton);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		int panelWidth = Math.min(PANEL_WIDTH, Math.max(180, width - 32));
		int panelHeight = Math.min(PANEL_HEIGHT, Math.max(120, height - 32));
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;
		context.fill(left, top, left + panelWidth, top + panelHeight, PANEL_BACKGROUND);
		context.drawBorder(left, top, panelWidth, panelHeight, PANEL_BORDER);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 20, TEXT_COLOR);
		context.drawCenteredTextWithShadow(textRenderer, "Choose a help topic.", width / 2, top + 44, MUTED_TEXT_COLOR);
		renderButtons(context, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		backToHome();
	}

	private void backToHome() {
		if (client != null) {
			client.setScreen(new MineFluenceSmartphoneHomeScreen(state));
		}
	}

	private void closeToGame() {
		if (client != null) {
			client.setScreen(null);
		}
	}

	private void replayTutorial() {
		if (client != null) {
			client.setScreen(MineFluenceTutorialScreen.replay(this));
		}
	}

	private void openMissionHelp() {
		if (client != null) {
			client.setScreen(new MineFluenceSmartphoneMissionHelpScreen(state, this));
		}
	}

	private void openGameSystemHelp() {
		if (client != null) {
			client.setScreen(new MineFluenceSmartphoneGameSystemHelpScreen(state, this));
		}
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		if (replayTutorialButton != null) {
			replayTutorialButton.render(context, mouseX, mouseY, delta);
		}
		if (missionHelpButton != null) {
			missionHelpButton.render(context, mouseX, mouseY, delta);
		}
		if (gameSystemHelpButton != null) {
			gameSystemHelpButton.render(context, mouseX, mouseY, delta);
		}
		if (backButton != null) {
			backButton.render(context, mouseX, mouseY, delta);
		}
		if (closeButton != null) {
			closeButton.render(context, mouseX, mouseY, delta);
		}
	}
}
