package net.jeongmin.modid.client;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.network.MineFluenceClientNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MineFluenceTutorialScreen extends Screen {
	private static final Identifier[] PAGES = {
			Identifier.of(MineFluence.MOD_ID, "textures/gui/tutorial/tutorial_1.png"),
			Identifier.of(MineFluence.MOD_ID, "textures/gui/tutorial/tutorial_2.png"),
			Identifier.of(MineFluence.MOD_ID, "textures/gui/tutorial/tutorial_3.png"),
			Identifier.of(MineFluence.MOD_ID, "textures/gui/tutorial/tutorial_4.png"),
			Identifier.of(MineFluence.MOD_ID, "textures/gui/tutorial/tutorial_5.png")
	};
	private static final float IMAGE_ASPECT_RATIO = 16.0F / 9.0F;
	private static final double BUTTON_LEFT_RATIO = 0.62D;
	private static final double BUTTON_RIGHT_RATIO = 0.85D;
	private static final double BUTTON_TOP_RATIO = 0.82D;
	private static final double BUTTON_BOTTOM_RATIO = 0.92D;
	private static final int BLACK = 0xFF000000;

	private final Screen returnScreen;
	private final boolean notifyServerOnCompletion;
	private int pageIndex;

	public MineFluenceTutorialScreen() {
		this(null, true);
	}

	private MineFluenceTutorialScreen(Screen returnScreen, boolean notifyServerOnCompletion) {
		super(Text.literal("MineFluence Tutorial"));
		this.returnScreen = returnScreen;
		this.notifyServerOnCompletion = notifyServerOnCompletion;
	}

	public static MineFluenceTutorialScreen replay(Screen returnScreen) {
		return new MineFluenceTutorialScreen(returnScreen, false);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, BLACK);
		drawCurrentPage(context);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInsideActionArea(mouseX, mouseY)) {
			advance();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
			advance();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		if (returnScreen != null && client != null) {
			client.setScreen(returnScreen);
			return;
		}
		super.close();
	}

	private void drawCurrentPage(DrawContext context) {
		int drawWidth = width;
		int drawHeight = Math.round(width / IMAGE_ASPECT_RATIO);
		if (drawHeight < height) {
			drawHeight = height;
			drawWidth = Math.round(height * IMAGE_ASPECT_RATIO);
		}

		int drawX = (width - drawWidth) / 2;
		int drawY = (height - drawHeight) / 2;
		context.drawTexture(PAGES[pageIndex], drawX, drawY, 0.0F, 0.0F, drawWidth, drawHeight, drawWidth, drawHeight);
	}

	private boolean isInsideActionArea(double mouseX, double mouseY) {
		int left = (int) Math.round(width * BUTTON_LEFT_RATIO);
		int right = (int) Math.round(width * BUTTON_RIGHT_RATIO);
		int top = (int) Math.round(height * BUTTON_TOP_RATIO);
		int bottom = (int) Math.round(height * BUTTON_BOTTOM_RATIO);
		return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
	}

	private void advance() {
		if (pageIndex < PAGES.length - 1) {
			pageIndex++;
			return;
		}

		if (notifyServerOnCompletion) {
			MineFluenceClientNetworking.finishTutorial();
		}
		close();
	}
}
