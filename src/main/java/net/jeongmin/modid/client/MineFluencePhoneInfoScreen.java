package net.jeongmin.modid.client;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.network.MineFluencePhoneStateResponsePayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MineFluencePhoneInfoScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFE8E8E8;
	private static final int MUTED_TEXT_COLOR = 0xFFB8B8B8;

	private final MineFluencePhoneStateResponsePayload state;
	private final String message;
	private ButtonWidget closeButton;

	public MineFluencePhoneInfoScreen(MineFluencePhoneStateResponsePayload state) {
		super(Text.literal(state.state().equals(MineFluencePhoneStateResponsePayload.STATE_MISSION_ACTIVE) ? "MineFluence Mission Status" : "MineFluence Phone"));
		this.state = state;
		this.message = state.message();
	}

	public MineFluencePhoneInfoScreen(String message) {
		super(Text.literal("MineFluence Phone"));
		this.state = null;
		this.message = message;
	}

	@Override
	protected void init() {
		closeButton = ButtonWidget.builder(Text.literal("Close"), button -> close())
				.dimensions(width / 2 - 40, height - 32, 80, 20)
				.build();
		addDrawableChild(closeButton);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, TEXT_COLOR);

		if (state != null && state.missionIndex() > 0) {
			context.drawCenteredTextWithShadow(textRenderer, "Mission " + state.missionIndex() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS + " - " + state.route(), width / 2, 44, TEXT_COLOR);
			context.drawCenteredTextWithShadow(textRenderer, trim(state.title(), width - 40), width / 2, 62, TEXT_COLOR);
			context.drawTextWrapped(textRenderer, Text.literal("Objective: " + state.objectiveText()), 24, 86, Math.max(80, width - 48), MUTED_TEXT_COLOR);
			context.drawCenteredTextWithShadow(textRenderer, "Progress: " + state.currentProgress() + "/" + state.targetProgress(), width / 2, 126, TEXT_COLOR);
			drawCenteredMessage(context, message, 148);
		} else {
			drawCenteredMessage(context, message, 60);
		}

		renderButtons(context, mouseX, mouseY, delta);
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		if (closeButton != null) {
			closeButton.render(context, mouseX, mouseY, delta);
		}
	}

	private void drawCenteredMessage(DrawContext context, String message, int y) {
		context.drawCenteredTextWithShadow(textRenderer, trim(message, Math.max(80, width - 40)), width / 2, y, MUTED_TEXT_COLOR);
	}

	private String trim(String value, int maxWidth) {
		String safeValue = value == null ? "" : value;
		return textRenderer.trimToWidth(safeValue, Math.max(20, maxWidth));
	}
}
