package net.jeongmin.modid.client;

import java.util.ArrayList;
import java.util.List;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.network.MineFluenceClientNetworking;
import net.jeongmin.modid.network.MineFluencePhoneAction;
import net.jeongmin.modid.network.MineFluencePhoneStateResponsePayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class MineFluenceSmartphoneHomeScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFB8C0CA;
	private static final int ACCENT_COLOR = 0xFF63D487;
	private static final int WARNING_COLOR = 0xFFFFCC66;
	private static final int PANEL_BACKGROUND = 0xCC11151B;
	private static final int PANEL_BORDER = 0x668A929C;
	private static final int BUTTON_WIDTH = 154;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_STEP = 23;
	private static final int WIDE_LAYOUT_WIDTH = 390;

	private final MineFluencePhoneStateResponsePayload state;
	private final List<ButtonWidget> buttons = new ArrayList<>();
	private String localMessage;
	private int buttonStartY;

	public MineFluenceSmartphoneHomeScreen(MineFluencePhoneStateResponsePayload state) {
		super(Text.literal("MineFluence Smartphone"));
		this.state = state;
		this.localMessage = state.message();
	}

	@Override
	protected void init() {
		buttons.clear();
		List<ButtonSpec> specs = buttonSpecs();
		boolean wide = width >= WIDE_LAYOUT_WIDTH;
		int buttonWidth = Math.min(BUTTON_WIDTH, Math.max(120, width - 48));
		int buttonX = wide ? width - buttonWidth - 24 : (width - buttonWidth) / 2;
		buttonStartY = wide ? 52 : Math.max(96, height - specs.size() * BUTTON_STEP - 24);

		int y = buttonStartY;
		for (ButtonSpec spec : specs) {
			ButtonWidget button = ButtonWidget.builder(Text.literal(spec.label()), pressedButton -> spec.action().run())
					.dimensions(buttonX, y, buttonWidth, BUTTON_HEIGHT)
					.build();
			buttons.add(button);
			addDrawableChild(button);
			y += BUTTON_STEP;
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, TEXT_COLOR);
		drawStatusPanel(context);
		renderButtons(context, mouseX, mouseY, delta);
	}

	private List<ButtonSpec> buttonSpecs() {
		List<ButtonSpec> buttons = new ArrayList<>();
		if (state.endingTriggered()) {
			buttons.add(new ButtonSpec("View Ending", this::viewEnding));
			buttons.add(new ButtonSpec("Play Ending Video", () -> sendAction(MineFluencePhoneAction.PLAY_ENDING_VIDEO)));
			buttons.add(new ButtonSpec("Restart Demo", () -> sendAction(MineFluencePhoneAction.START_DEMO)));
			buttons.add(new ButtonSpec("Tutorial", this::openTutorial));
		} else if (state.activeInvasionIndex() > 0) {
			buttons.add(new ButtonSpec("Invasion Status", this::viewInvasionStatus));
			buttons.add(new ButtonSpec("Status Refresh", this::refresh));
			buttons.add(new ButtonSpec("Tutorial", this::openTutorial));
		} else if (isPosting()) {
			buttons.add(new ButtonSpec("Open Upload Screen", this::openUploadScreen));
			buttons.add(new ButtonSpec("Post Normally", () -> sendAction(MineFluencePhoneAction.POST_NORMAL)));
			buttons.add(new ButtonSpec("Post Exaggerated", () -> sendAction(MineFluencePhoneAction.POST_EXAGGERATE)));
		} else if (isPendingMissionSelection()) {
			buttons.add(new ButtonSpec("Open Mission Board", this::openMissionBoard));
			buttons.add(new ButtonSpec("Choose Good", () -> sendAction(MineFluencePhoneAction.CHOOSE_GOOD)));
			buttons.add(new ButtonSpec("Choose Bad", () -> sendAction(MineFluencePhoneAction.CHOOSE_BAD)));
		} else if (isActiveMission()) {
			buttons.add(new ButtonSpec("Show Mission Area", () -> sendAction(MineFluencePhoneAction.SHOW_MISSION_AREA)));
			buttons.add(new ButtonSpec("View Progress", this::viewProgress));
			buttons.add(new ButtonSpec("Open Mission Board", this::openMissionBoard));
		} else if (!hasFarmerJob()) {
			if (isLikelyResetState()) {
				buttons.add(new ButtonSpec("Start Demo", () -> sendAction(MineFluencePhoneAction.START_DEMO)));
			} else {
				buttons.add(new ButtonSpec("Choose Farmer", () -> sendAction(MineFluencePhoneAction.CHOOSE_FARMER)));
				buttons.add(new ButtonSpec("Start Demo", () -> sendAction(MineFluencePhoneAction.START_DEMO)));
			}
			buttons.add(new ButtonSpec("Tutorial", this::openTutorial));
		} else {
			if (state.completedMissionCount() < MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
				buttons.add(new ButtonSpec("Start Next Mission", () -> sendAction(MineFluencePhoneAction.START_NEXT_MISSION)));
			}
			buttons.add(new ButtonSpec("Tutorial", this::openTutorial));
			buttons.add(new ButtonSpec("Status Refresh", this::refresh));
		}

		buttons.add(new ButtonSpec("Close", this::close));
		return buttons;
	}

	private void drawStatusPanel(DrawContext context) {
		boolean wide = width >= WIDE_LAYOUT_WIDTH;
		int panelX = 24;
		int panelY = 38;
		int panelWidth = wide ? Math.max(180, width - BUTTON_WIDTH - 70) : Math.max(120, width - 48);
		int panelBottom = wide ? height - 32 : Math.max(panelY + 56, buttonStartY - 10);

		context.fill(panelX - 8, panelY - 8, panelX + panelWidth + 8, panelBottom, PANEL_BACKGROUND);
		context.drawBorder(panelX - 8, panelY - 8, panelWidth + 16, panelBottom - panelY + 8, PANEL_BORDER);

		int y = panelY;
		y = drawWrappedLine(context, "Status", panelX, y, panelWidth, ACCENT_COLOR);
		y = drawWrappedLine(context, "Job: " + labelValue(state.selectedJob(), "None"), panelX, y, panelWidth, TEXT_COLOR);
		y = drawWrappedLine(context, "Followers: " + state.follower(), panelX, y, panelWidth, TEXT_COLOR);
		y = drawWrappedLine(context, "Social Credibility: " + state.socialCredibility(), panelX, y, panelWidth, TEXT_COLOR);
		y = drawWrappedLine(context, "Lie Gauge: " + liePercent() + "%", panelX, y, panelWidth, TEXT_COLOR);
		y = drawWrappedLine(context, "Mission Count: " + state.completedMissionCount() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS, panelX, y, panelWidth, TEXT_COLOR);
		y = drawWrappedLine(context, "Weapon Tier: " + displayValue(state.weaponTier(), "Wood"), panelX, y, panelWidth, TEXT_COLOR);
		y += 4;
		y = drawWrappedLine(context, "Current Task:", panelX, y, panelWidth, ACCENT_COLOR);
		for (String line : taskLines()) {
			if (y + 12 > panelBottom) {
				break;
			}
			y = drawWrappedLine(context, line, panelX, y, panelWidth, TEXT_COLOR);
		}
		y += 4;
		if (y + 12 <= panelBottom) {
			y = drawWrappedLine(context, "Next: " + nextAction(), panelX, y, panelWidth, WARNING_COLOR);
		}
		if (!safeText(localMessage).isBlank() && y + 12 <= panelBottom) {
			drawWrappedLine(context, safeText(localMessage), panelX, y, panelWidth, MUTED_TEXT_COLOR);
		}
	}

	private List<String> taskLines() {
		List<String> lines = new ArrayList<>();
		if (state.endingTriggered()) {
			lines.add("Ending: " + displayValue(state.endingName(), "Unknown"));
			return lines;
		}
		if (state.activeInvasionIndex() > 0) {
			lines.add("Invasion " + state.activeInvasionIndex() + " - Defend the village");
			lines.add("Invaders: " + state.invasionRemaining() + "/" + state.invasionTotal());
			return lines;
		}
		if (isPosting()) {
			lines.add("Mission " + state.missionIndex() + " - " + routeLabel(state.route()) + " ready to upload");
			lines.add(displayValue(state.title(), "Content upload pending"));
			return lines;
		}
		if (isPendingMissionSelection()) {
			int missionIndex = state.pendingMissionSelectionIndex() > 0 ? state.pendingMissionSelectionIndex() : state.missionIndex();
			lines.add("Mission " + missionIndex + " route selection");
			lines.add("Choose Good or Bad.");
			return lines;
		}
		if (isActiveMission()) {
			lines.add("Mission " + state.missionIndex() + " - " + routeLabel(state.route()));
			lines.add(displayValue(state.title(), "Active mission"));
			lines.add(displayValue(state.objectiveText(), "Complete the current objective."));
			lines.add("Progress: " + state.currentProgress() + "/" + state.targetProgress());
			return lines;
		}
		lines.add(displayValue(state.message(), "No active mission."));
		return lines;
	}

	private String nextAction() {
		if (state.endingTriggered()) {
			return "Ending reached. Play the ending video.";
		}
		if (state.activeInvasionIndex() > 0) {
			return "Defend the village from invaders.";
		}
		if (isPosting()) {
			return "Choose how to upload your content.";
		}
		if (isPendingMissionSelection()) {
			return "Choose Good or Bad mission.";
		}
		if (isActiveMission()) {
			String area = requiredAreaName();
			if (area != null) {
				return "Go to the highlighted " + area + " area.";
			}
			return "Complete the current mission objective.";
		}
		if (!hasFarmerJob()) {
			return isLikelyResetState() ? "Press Start Demo to begin." : "Choose Farmer to start the demo.";
		}
		if (state.completedMissionCount() < MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return "Start the next mission.";
		}
		return "Check the ending or demo status.";
	}

	private int drawWrappedLine(DrawContext context, String value, int x, int y, int maxWidth, int color) {
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal(safeText(value)), Math.max(40, maxWidth));
		for (OrderedText line : lines) {
			context.drawTextWithShadow(textRenderer, line, x, y, color);
			y += 11;
		}
		return y + 1;
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		for (ButtonWidget button : buttons) {
			button.render(context, mouseX, mouseY, delta);
		}
	}

	private void sendAction(MineFluencePhoneAction action) {
		if (MineFluenceClientNetworking.sendPhoneAction(action)) {
			localMessage = "Action sent.";
			return;
		}
		localMessage = "Server smartphone action channel is not available.";
	}

	private void refresh() {
		if (!MineFluenceClientNetworking.requestPhoneState()) {
			localMessage = "Server smartphone state channel is not available.";
		}
	}

	private void openMissionBoard() {
		if (client != null) {
			client.setScreen(new MineFluenceMissionScreen());
		}
	}

	private void openUploadScreen() {
		if (client != null) {
			client.setScreen(new MineFluencePostingScreen(state));
		}
	}

	private void openTutorial() {
		if (client != null) {
			client.setScreen(new MineFluenceTutorialScreen());
		}
	}

	private void viewProgress() {
		localMessage = "Progress: " + state.currentProgress() + "/" + state.targetProgress() + ". " + state.message();
	}

	private void viewInvasionStatus() {
		localMessage = "Invasion " + state.activeInvasionIndex() + ": invaders remaining " + state.invasionRemaining() + "/" + state.invasionTotal() + ".";
	}

	private void viewEnding() {
		localMessage = "Ending: " + displayValue(state.endingName(), "Unknown") + ".";
	}

	private boolean isPosting() {
		return MineFluencePhoneStateResponsePayload.STATE_POSTING.equals(state.state());
	}

	private boolean isPendingMissionSelection() {
		return state.pendingMissionSelectionIndex() > 0 || MineFluencePhoneStateResponsePayload.STATE_MISSION_BOARD.equals(state.state());
	}

	private boolean isActiveMission() {
		return state.missionIndex() > 0 && MineFluencePhoneStateResponsePayload.STATE_STATUS.equals(state.state());
	}

	private boolean hasFarmerJob() {
		return "farmer".equalsIgnoreCase(safeText(state.selectedJob()));
	}

	private boolean isLikelyResetState() {
		return state.completedMissionCount() == MineFluenceBalance.COMPLETED_MISSION_DEFAULT
				&& state.follower() == MineFluenceBalance.FOLLOWER_DEFAULT
				&& state.socialCredibility() == MineFluenceBalance.SOCIAL_CREDIBILITY_DEFAULT
				&& state.lieValue() == MineFluenceBalance.LIE_VALUE_DEFAULT;
	}

	private int liePercent() {
		if (MineFluenceBalance.LIE_VALUE_MAX <= 0) {
			return 0;
		}
		return Math.round(state.lieValue() * 100.0F / MineFluenceBalance.LIE_VALUE_MAX);
	}

	private String requiredAreaName() {
		if (!"GOOD".equalsIgnoreCase(safeText(state.route()))) {
			return null;
		}
		return switch (state.missionIndex()) {
			case 1 -> "Garden";
			case 2 -> "Farm";
			case 6 -> "Shared";
			case 7 -> "Farm Build";
			default -> null;
		};
	}

	private static String routeLabel(String route) {
		if ("BAD".equalsIgnoreCase(route)) {
			return "Bad";
		}
		if ("GOOD".equalsIgnoreCase(route)) {
			return "Good";
		}
		return labelValue(route, "Unknown");
	}

	private static String displayValue(String value, String fallback) {
		String safeValue = safeText(value);
		if (safeValue.isBlank()) {
			return fallback;
		}
		return safeValue;
	}

	private static String labelValue(String value, String fallback) {
		String safeValue = safeText(value);
		if (safeValue.isBlank()) {
			return fallback;
		}
		if (safeValue.length() == 1) {
			return safeValue.toUpperCase();
		}
		return safeValue.substring(0, 1).toUpperCase() + safeValue.substring(1).toLowerCase();
	}

	private static String safeText(String value) {
		return value == null ? "" : value;
	}

	private record ButtonSpec(String label, Runnable action) {
	}
}
