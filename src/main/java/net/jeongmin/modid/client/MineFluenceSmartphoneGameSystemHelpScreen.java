package net.jeongmin.modid.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.network.MineFluencePhoneStateResponsePayload;
import net.jeongmin.modid.weapon.MineFluenceWeaponTier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class MineFluenceSmartphoneGameSystemHelpScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFB8C0CA;
	private static final int SECTION_COLOR = 0xFF63D487;
	private static final int SUBSECTION_COLOR = 0xFFFFD27A;
	private static final int WARNING_COLOR = 0xFFE06B6B;
	private static final int PANEL_BACKGROUND = 0xE011151B;
	private static final int PANEL_BORDER = 0x668A929C;
	private static final int SCROLL_TRACK_COLOR = 0x553B424B;
	private static final int SCROLL_THUMB_COLOR = 0xAA8A929C;
	private static final int MAX_PANEL_WIDTH = 760;
	private static final int MAX_PANEL_HEIGHT = 640;
	private static final int PANEL_MARGIN = 16;
	private static final int CONTENT_PADDING = 16;
	private static final int LINE_HEIGHT = 11;
	private static final int SCROLL_STEP = 32;
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_HEIGHT = 20;

	private final MineFluencePhoneStateResponsePayload state;
	private final Screen returnScreen;
	private final List<RenderedHelpLine> renderedLines = new ArrayList<>();

	private ButtonWidget backButton;
	private ButtonWidget closeButton;
	private int panelLeft;
	private int panelTop;
	private int panelWidth;
	private int panelHeight;
	private int contentLeft;
	private int contentTop;
	private int contentRight;
	private int contentBottom;
	private int contentHeight;
	private int scrollOffset;

	public MineFluenceSmartphoneGameSystemHelpScreen(
			MineFluencePhoneStateResponsePayload state,
			Screen returnScreen
	) {
		super(Text.literal("Game System Help"));
		this.state = state;
		this.returnScreen = returnScreen;
	}

	@Override
	protected void init() {
		panelWidth = Math.min(MAX_PANEL_WIDTH, Math.max(260, width - PANEL_MARGIN * 2));
		panelHeight = Math.min(MAX_PANEL_HEIGHT, Math.max(160, height - PANEL_MARGIN * 2));
		panelLeft = (width - panelWidth) / 2;
		panelTop = (height - panelHeight) / 2;

		int buttonY = panelTop + panelHeight - 30;
		int centerX = width / 2;
		backButton = ButtonWidget.builder(Text.literal("Back"), button -> close())
				.dimensions(centerX - BUTTON_WIDTH - 6, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		closeButton = ButtonWidget.builder(Text.literal("Close"), button -> closeToGame())
				.dimensions(centerX + 6, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		addDrawableChild(backButton);
		addDrawableChild(closeButton);

		contentLeft = panelLeft + CONTENT_PADDING;
		contentRight = panelLeft + panelWidth - CONTENT_PADDING - 8;
		contentTop = panelTop + 44;
		contentBottom = buttonY - 10;
		rebuildWrappedContent();
		scrollOffset = Math.min(scrollOffset, maxScrollOffset());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		context.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, PANEL_BACKGROUND);
		context.drawBorder(panelLeft, panelTop, panelWidth, panelHeight, PANEL_BORDER);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, panelTop + 14, TEXT_COLOR);
		context.drawCenteredTextWithShadow(
				textRenderer,
				"Scroll to review MineFluence's core systems.",
				width / 2,
				panelTop + 29,
				MUTED_TEXT_COLOR
		);

		context.enableScissor(contentLeft, contentTop, contentRight, contentBottom);
		for (RenderedHelpLine line : renderedLines) {
			int y = contentTop + line.y() - scrollOffset;
			if (y + LINE_HEIGHT < contentTop || y >= contentBottom) {
				continue;
			}
			context.drawTextWithShadow(textRenderer, line.text(), contentLeft, y, line.color());
		}
		context.disableScissor();
		drawScrollbar(context);
		renderButtons(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (mouseX < panelLeft || mouseX > panelLeft + panelWidth
				|| mouseY < contentTop || mouseY > contentBottom) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		int scrollDelta = (int) Math.round(verticalAmount * SCROLL_STEP);
		scrollOffset = Math.max(0, Math.min(maxScrollOffset(), scrollOffset - scrollDelta));
		return true;
	}

	@Override
	public void close() {
		if (client != null) {
			client.setScreen(returnScreen != null ? returnScreen : new MineFluenceSmartphoneHelpScreen(state));
		}
	}

	private void closeToGame() {
		if (client != null) {
			client.setScreen(null);
		}
	}

	private void rebuildWrappedContent() {
		renderedLines.clear();
		int maxWidth = Math.max(80, contentRight - contentLeft);
		int y = 0;

		y = addSection("1. Weapon System", y, maxWidth);
		y = addParagraph("Your Weapon upgrades automatically based on your Followers.", y, maxWidth);
		y = addParagraph("Reach each threshold to receive a stronger Farmer Hoe:", y, maxWidth);
		for (MineFluenceWeaponTier tier : MineFluenceWeaponTier.values()) {
			y = addParagraph("- " + tier.displayName() + ": " + tier.minimumFollowers() + " Followers", y, maxWidth);
		}
		y = addParagraph("Stronger Weapon tiers deal more bonus damage to tracked monsters during an Invasion.", y, maxWidth);

		y = addSection("2. Invasion System", y + 5, maxWidth);
		y = addParagraph(
				"Invasions occur after missions "
						+ MineFluenceBalance.INVASION_1_TRIGGER_MISSION_COUNT + ", "
						+ MineFluenceBalance.INVASION_2_TRIGGER_MISSION_COUNT + ", and "
						+ MineFluenceBalance.INVASION_3_TRIGGER_MISSION_COUNT + ".",
				y,
				maxWidth
		);
		y = addParagraph("Each Invasion checks your Social Credibility. Higher values make the attack weaker and can provide more village defenders. Lower values make the attack stronger.", y, maxWidth);
		y = addSubsection("Invasion strength", y, maxWidth);
		y = addParagraph("- Weak: " + MineFluenceBalance.INVASION_WEAK_ZOMBIE_COUNT + " monster", y, maxWidth);
		y = addParagraph("- Medium: " + MineFluenceBalance.INVASION_MEDIUM_ZOMBIE_COUNT + " monsters", y, maxWidth);
		y = addParagraph("- Strong: " + MineFluenceBalance.INVASION_STRONG_ZOMBIE_COUNT + " monsters", y, maxWidth);
		y = addParagraph("Each story Invasion uses different Social Credibility thresholds, so maintaining Social Credibility becomes more important as the game progresses.", y, maxWidth);

		y = addSection("3. Villagers and Followers", y + 5, maxWidth);
		y = addParagraph(
				"The story treats the village audience as up to "
						+ MineFluenceBalance.FOLLOWER_MAX + " potential Followers.",
				y,
				maxWidth
		);
		y = addParagraph("Followers represent how many villagers are influenced by your posts and actions.", y, maxWidth);
		y = addParagraph("The world shows a smaller tiered group of visible fan villagers as feedback; it does not spawn one entity for every Follower.", y, maxWidth);

		y = addSection("4. Stats", y + 5, maxWidth);
		y = addSubsection("Followers", y, maxWidth);
		y = addParagraph("Provocative or exaggerated posts can increase Followers faster. Weapon power scales with Followers, helping you defend the village during Invasions.", y, maxWidth);

		y = addSubsection("Social Credibility", y + 3, maxWidth);
		y = addParagraph("Social Credibility represents how much the village trusts you. Helpful actions and trusted posts can raise it. Higher Social Credibility reduces Invasion danger and can increase village support.", y, maxWidth);

		y = addSubsection("Lie Gauge", y + 3, maxWidth);
		y = addParagraph("After each mission, you can post normally or exaggerate the story.", y, maxWidth);
		y = addParagraph(
				"Exaggeration applies "
						+ multiplierText(MineFluenceBalance.EXAGGERATED_POSTING_MULTIPLIER)
						+ " the mission's Followers and Social Credibility reward, rounded to a whole number. Negative Social Credibility rewards also become more negative.",
				y,
				maxWidth
		);
		y = addParagraph("Exaggeration also secretly increases the Lie Gauge. The exact exposure limit is hidden.", y, maxWidth);
		y = addLine(
				"If the Lie Gauge becomes too high, you are exposed and the run collapses into the Famous Villain ending. Stored Followers and Social Credibility are not reset by the current implementation.",
				WARNING_COLOR,
				y,
				maxWidth,
				2
		);

		contentHeight = Math.max(0, y);
	}

	private int addSection(String value, int y, int maxWidth) {
		return addLine(value, SECTION_COLOR, y, maxWidth, 5);
	}

	private int addSubsection(String value, int y, int maxWidth) {
		return addLine(value, SUBSECTION_COLOR, y, maxWidth, 2);
	}

	private int addParagraph(String value, int y, int maxWidth) {
		return addLine(value, TEXT_COLOR, y, maxWidth, 3);
	}

	private int addLine(String value, int color, int y, int maxWidth, int bottomGap) {
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal(value), maxWidth);
		for (OrderedText line : lines) {
			renderedLines.add(new RenderedHelpLine(line, color, y));
			y += LINE_HEIGHT;
		}
		return y + bottomGap;
	}

	private void drawScrollbar(DrawContext context) {
		int viewportHeight = Math.max(1, contentBottom - contentTop);
		if (contentHeight <= viewportHeight) {
			return;
		}

		int trackX = panelLeft + panelWidth - 10;
		context.fill(trackX, contentTop, trackX + 3, contentBottom, SCROLL_TRACK_COLOR);
		int thumbHeight = Math.max(18, viewportHeight * viewportHeight / contentHeight);
		int availableTrack = viewportHeight - thumbHeight;
		int thumbY = contentTop + (maxScrollOffset() == 0 ? 0 : scrollOffset * availableTrack / maxScrollOffset());
		context.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, SCROLL_THUMB_COLOR);
	}

	private int maxScrollOffset() {
		return Math.max(0, contentHeight - Math.max(1, contentBottom - contentTop));
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		if (backButton != null) {
			backButton.render(context, mouseX, mouseY, delta);
		}
		if (closeButton != null) {
			closeButton.render(context, mouseX, mouseY, delta);
		}
	}

	private static String multiplierText(double multiplier) {
		return String.format(Locale.ROOT, "%.1fx", multiplier);
	}

	private record RenderedHelpLine(OrderedText text, int color, int y) {
	}
}
