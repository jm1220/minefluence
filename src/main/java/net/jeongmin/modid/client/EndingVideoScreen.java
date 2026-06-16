package net.jeongmin.modid.client;

import java.util.HashSet;
import java.util.Set;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.ending.MineFluenceEndingVideos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class EndingVideoScreen extends Screen {
	private static final int FRAME_COUNT = 169;
	private static final int FIRST_FRAME = 0;
	private static final int LAST_FRAME = FRAME_COUNT - 1;
	private static final long FRAME_DURATION_MS = 100L;
	private static final float TARGET_ASPECT_RATIO = 16.0F / 9.0F;
	private static final int BLACK = 0xFF000000;
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFB8C0CA;
	private static final int SOURCE_WIDTH = 640;
	private static final int SOURCE_HEIGHT = 360;

	private final Set<Integer> loggedMissingFrames = new HashSet<>();
	private final String endingId;
	private final long startTimeMs;

	public EndingVideoScreen(String endingId) {
		super(Text.literal("MineFluence Ending Video"));
		this.endingId = endingId == null || endingId.isBlank() ? MineFluenceEndingVideos.THE_FAMOUS_VILLAIN_ID : endingId;
		this.startTimeMs = Util.getMeasuringTimeMs();
	}

	public static EndingVideoScreen theFamousVillain() {
		return new EndingVideoScreen(MineFluenceEndingVideos.THE_FAMOUS_VILLAIN_ID);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, BLACK);

		long elapsedMs = Math.max(0L, Util.getMeasuringTimeMs() - startTimeMs);
		int rawFrameIndex = (int) (elapsedMs / FRAME_DURATION_MS);
		int frameIndex = Math.min(Math.max(FIRST_FRAME, rawFrameIndex), LAST_FRAME);
		Identifier frame = frameIdentifier(frameIndex);

		if (isFrameAvailable(frame)) {
			drawFrame(context, frame);
		} else {
			drawMissingFrameMessage(context, frameIndex);
		}

		if (rawFrameIndex > LAST_FRAME) {
			int centerY = height / 2;
			context.drawCenteredTextWithShadow(textRenderer, "Ending complete", width / 2, centerY + 118, TEXT_COLOR);
			context.drawCenteredTextWithShadow(textRenderer, "Press Esc to close", width / 2, centerY + 132, MUTED_TEXT_COLOR);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private void drawFrame(DrawContext context, Identifier frame) {
		int drawWidth = width;
		int drawHeight = Math.round(width / TARGET_ASPECT_RATIO);
		if (drawHeight > height) {
			drawHeight = height;
			drawWidth = Math.round(height * TARGET_ASPECT_RATIO);
		}

		int drawX = (width - drawWidth) / 2;
		int drawY = (height - drawHeight) / 2;
		context.drawTexture(frame, drawX, drawY, drawWidth, drawHeight, 0.0F, 0.0F, SOURCE_WIDTH, SOURCE_HEIGHT, SOURCE_WIDTH, SOURCE_HEIGHT);
	}

	private boolean isFrameAvailable(Identifier frame) {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		return minecraftClient.getResourceManager().getResource(frame).isPresent();
	}

	private void drawMissingFrameMessage(DrawContext context, int frameIndex) {
		String path = framePath(frameIndex);
		if (loggedMissingFrames.add(frameIndex)) {
			MineFluence.LOGGER.warn("[MineFluence] Missing ending frame: {}", path);
		}

		context.drawCenteredTextWithShadow(textRenderer, "Missing ending frame:", width / 2, height / 2 - 8, TEXT_COLOR);
		context.drawCenteredTextWithShadow(textRenderer, path, width / 2, height / 2 + 8, MUTED_TEXT_COLOR);
	}

	private Identifier frameIdentifier(int frameIndex) {
		return Identifier.of(MineFluence.MOD_ID, framePath(frameIndex));
	}

	private String framePath(int frameIndex) {
		return "textures/ending/" + endingId + "/frame_" + String.format("%04d", frameIndex) + ".png";
	}
}
