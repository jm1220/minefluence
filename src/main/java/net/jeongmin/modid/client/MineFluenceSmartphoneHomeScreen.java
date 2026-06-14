package net.jeongmin.modid.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_STEP = 23;
	private static final int WIDE_LAYOUT_WIDTH = 300;

	private final MineFluencePhoneStateResponsePayload state;
	private final List<ButtonWidget> buttons = new ArrayList<>();
	private String localMessage;
	private PhoneLayout currentLayout;

	public MineFluenceSmartphoneHomeScreen(MineFluencePhoneStateResponsePayload state) {
		super(Text.literal("MineFluence Smartphone"));
		this.state = state;
		this.localMessage = state.message();
	}

	@Override
	protected void init() {
		buttons.clear();
		List<ButtonSpec> specs = buttonSpecs();
		currentLayout = layout(specs.size());

		for (int index = 0; index < specs.size(); index++) {
			ButtonSpec spec = specs.get(index);
			ButtonWidget button = ButtonWidget.builder(Text.literal(spec.label()), pressedButton -> spec.action().run())
					.dimensions(
							currentLayout.buttonX(),
							currentLayout.buttonStartY() + index * BUTTON_STEP,
							currentLayout.buttonWidth(),
							BUTTON_HEIGHT
					)
					.build();
			buttons.add(button);
			addDrawableChild(button);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		PhoneLayout layout = currentLayout == null ? layout(buttonSpecs().size()) : currentLayout;
		drawPanel(context, layout);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, TEXT_COLOR);
		drawStatusText(context, layout);
		renderButtons(context, mouseX, mouseY, delta);
	}

	private List<ButtonSpec> buttonSpecs() {
		List<ButtonSpec> specs = new ArrayList<>();
		if (isEndingState()) {
			if (state.endingVideoAvailable()) {
				specs.add(new ButtonSpec("Play Ending Video", () -> sendAction(MineFluencePhoneAction.PLAY_ENDING_VIDEO)));
			}
			specs.add(new ButtonSpec("Restart Demo", () -> sendAction(MineFluencePhoneAction.RESTART_DEMO)));
		} else if (isState(MineFluencePhoneStateResponsePayload.STATE_READY_TO_UPLOAD)) {
			specs.add(new ButtonSpec("Open Upload Screen", this::openUploadScreen));
		} else if (isState(MineFluencePhoneStateResponsePayload.STATE_MISSION_CHOICE)) {
			specs.add(new ButtonSpec("Open Mission Board", this::openMissionBoard));
		} else if (isState(MineFluencePhoneStateResponsePayload.STATE_MISSION_ACTIVE)) {
			if (!safeText(state.requiredAreaName()).isBlank()) {
				specs.add(new ButtonSpec("Show Mission Area", () -> sendAction(MineFluencePhoneAction.SHOW_MISSION_AREA)));
			}
		} else if (isState(MineFluencePhoneStateResponsePayload.STATE_NOT_STARTED)) {
			specs.add(new ButtonSpec("Tutorial", () -> sendAction(MineFluencePhoneAction.OPEN_TUTORIAL)));
		} else if (isState(MineFluencePhoneStateResponsePayload.STATE_CHOOSE_JOB)) {
			specs.add(new ButtonSpec("Choose Farmer", () -> sendAction(MineFluencePhoneAction.CHOOSE_FARMER)));
		} else if (state.completedMissionCount() < MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			specs.add(new ButtonSpec("Start Next Mission", () -> sendAction(MineFluencePhoneAction.START_NEXT_MISSION)));
		}

		if (!isState(MineFluencePhoneStateResponsePayload.STATE_NOT_STARTED)) {
			specs.add(new ButtonSpec("Help", this::openHelp));
		}
		specs.add(new ButtonSpec("Close", this::close));
		return specs;
	}

	private void drawPanel(DrawContext context, PhoneLayout layout) {
		int left = layout.panelX() - 8;
		int top = layout.panelY() - 8;
		int right = layout.panelX() + layout.panelWidth() + 8;
		context.fill(left, top, right, layout.panelBottom(), PANEL_BACKGROUND);
		context.drawBorder(left, top, right - left, layout.panelBottom() - top, PANEL_BORDER);
	}

	private void drawStatusText(DrawContext context, PhoneLayout layout) {
		int x = layout.panelX();
		int y = layout.panelY();
		int maxWidth = layout.panelWidth();
		int bottom = layout.panelBottom() - 7;

		y = drawWrappedLine(context, "Player Status", x, y, maxWidth, bottom, ACCENT_COLOR);
		y = drawWrappedLine(context, "Job: " + labelValue(state.selectedJob(), "None"), x, y, maxWidth, bottom, TEXT_COLOR);
		y = drawWrappedLine(context, "Followers: " + state.follower(), x, y, maxWidth, bottom, TEXT_COLOR);
		y = drawWrappedLine(context, "Social Credibility: " + signed(state.socialCredibility()), x, y, maxWidth, bottom, TEXT_COLOR);
		y = drawWrappedLine(context, "Lie Risk: " + MineFluenceBalance.getLieRiskLabel(state.lieValue()), x, y, maxWidth, bottom, TEXT_COLOR);
		y = drawWrappedLine(context, "Weapon: " + weaponName(), x, y, maxWidth, bottom, TEXT_COLOR);
		y = drawWrappedLine(context, "Missions: " + state.completedMissionCount() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS, x, y, maxWidth, bottom, TEXT_COLOR);
		y += 3;
		y = drawWrappedLine(context, "Current State", x, y, maxWidth, bottom, ACCENT_COLOR);
		y = drawWrappedLine(context, "State: " + stateLabel(), x, y, maxWidth, bottom, TEXT_COLOR);
		y = drawWrappedLine(context, "Next: " + nextAction(), x, y, maxWidth, bottom, WARNING_COLOR);

		for (String detail : stateDetails()) {
			y = drawWrappedLine(context, detail, x, y, maxWidth, bottom, TEXT_COLOR);
		}
		if (!safeText(localMessage).isBlank()) {
			drawWrappedLine(context, safeText(localMessage), x, y + 2, maxWidth, bottom, MUTED_TEXT_COLOR);
		}
	}

	private List<String> stateDetails() {
		List<String> lines = new ArrayList<>();
		if (isEndingState()) {
			lines.add("Ending: " + displayValue(state.endingName(), "Unknown"));
			return lines;
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_INVASION)) {
			lines.add("Invasion Level: " + state.activeInvasionIndex());
			lines.add("Enemies: " + state.invasionRemaining() + "/" + state.invasionTotal());
			lines.add("Support Allies: " + state.supportAllyCount());
			return lines;
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_READY_TO_UPLOAD)) {
			lines.add("Mission " + state.missionIndex() + " - " + routeLabel(state.route()));
			lines.add(displayValue(state.title(), "Upload pending"));
			return lines;
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_MISSION_CHOICE)) {
			int missionIndex = state.pendingMissionSelectionIndex() > 0
					? state.pendingMissionSelectionIndex()
					: state.missionIndex();
			lines.add("Mission: " + missionIndex + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS);
			return lines;
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_MISSION_ACTIVE)) {
			lines.add("Mission " + state.missionIndex() + " - " + routeLabel(state.route()));
			lines.add(displayValue(state.title(), "Active mission"));
			lines.add("Objective: " + displayValue(state.objectiveText(), "Complete the objective."));
			lines.add("Progress: " + state.currentProgress() + "/" + state.targetProgress());
			lines.add("Area: " + displayValue(state.requiredAreaName(), "Not required"));
		}
		return lines;
	}

	private String stateLabel() {
		if (state.exposureTriggered() || isState(MineFluencePhoneStateResponsePayload.STATE_EXPOSED)) {
			return "Exposed / Collapse";
		}
		return switch (state.state()) {
			case MineFluencePhoneStateResponsePayload.STATE_NOT_STARTED -> "Not Started";
			case MineFluencePhoneStateResponsePayload.STATE_CHOOSE_JOB -> "Choose Job";
			case MineFluencePhoneStateResponsePayload.STATE_READY -> "Ready";
			case MineFluencePhoneStateResponsePayload.STATE_MISSION_CHOICE -> "Mission Choice";
			case MineFluencePhoneStateResponsePayload.STATE_MISSION_ACTIVE -> "Mission Active";
			case MineFluencePhoneStateResponsePayload.STATE_READY_TO_UPLOAD -> "Ready to Upload";
			case MineFluencePhoneStateResponsePayload.STATE_INVASION -> "Invasion Active";
			case MineFluencePhoneStateResponsePayload.STATE_ENDING -> "Ending";
			default -> displayValue(state.state(), "Ready");
		};
	}

	private String nextAction() {
		if (state.exposureTriggered() || isState(MineFluencePhoneStateResponsePayload.STATE_EXPOSED)) {
			return state.endingVideoAvailable()
					? "Play the ending video or restart the demo."
					: "Restart the demo.";
		}
		if (isEndingState()) {
			return state.endingVideoAvailable()
					? "Play the ending video or restart the demo."
					: "Restart the demo.";
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_INVASION)) {
			return "Defend the village.";
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_READY_TO_UPLOAD)) {
			return "Open Upload Screen to choose a posting type.";
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_MISSION_CHOICE)) {
			return "Open Mission Board to choose Good or Bad.";
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_MISSION_ACTIVE)) {
			return "Complete the current objective.";
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_NOT_STARTED)) {
			return "Play the tutorial to begin.";
		}
		if (isState(MineFluencePhoneStateResponsePayload.STATE_CHOOSE_JOB)) {
			return "Choose Farmer.";
		}
		if (state.completedMissionCount() < MineFluenceBalance.TOTAL_DEMO_MISSIONS) {
			return "Start the next mission.";
		}
		return "Wait for the ending state.";
	}

	private int drawWrappedLine(
			DrawContext context,
			String value,
			int x,
			int y,
			int maxWidth,
			int bottom,
			int color
	) {
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal(safeText(value)), Math.max(40, maxWidth));
		for (OrderedText line : lines) {
			if (y + 10 > bottom) {
				return y;
			}
			context.drawTextWithShadow(textRenderer, line, x, y, color);
			y += 11;
		}
		return y + 1;
	}

	private PhoneLayout layout(int buttonCount) {
		boolean wide = width >= WIDE_LAYOUT_WIDTH;
		if (wide) {
			int buttonWidth = Math.min(154, Math.max(112, width / 3));
			int buttonX = width - buttonWidth - 18;
			int panelX = 18;
			int buttonStartY = Math.max(38, (height - buttonCount * BUTTON_STEP) / 2);
			return new PhoneLayout(
					panelX,
					36,
					Math.max(100, buttonX - panelX - 14),
					height - 16,
					buttonX,
					buttonStartY,
					buttonWidth
			);
		}

		int buttonWidth = Math.min(154, Math.max(100, width - 32));
		int buttonStartY = Math.max(104, height - buttonCount * BUTTON_STEP - 10);
		return new PhoneLayout(
				18,
				36,
				Math.max(80, width - 36),
				Math.max(90, buttonStartY - 8),
				(width - buttonWidth) / 2,
				buttonStartY,
				buttonWidth
		);
	}

	private void renderButtons(DrawContext context, int mouseX, int mouseY, float delta) {
		for (ButtonWidget button : buttons) {
			button.render(context, mouseX, mouseY, delta);
		}
	}

	private void sendAction(MineFluencePhoneAction action) {
		if (MineFluenceClientNetworking.sendPhoneAction(action)) {
			localMessage = "Action requested.";
			return;
		}
		localMessage = "Server smartphone action channel is not available.";
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

	private void openHelp() {
		if (client != null) {
			client.setScreen(new MineFluenceSmartphoneHelpScreen(state));
		}
	}

	private boolean isEndingState() {
		return state.endingTriggered()
				|| isState(MineFluencePhoneStateResponsePayload.STATE_ENDING)
				|| isState(MineFluencePhoneStateResponsePayload.STATE_EXPOSED);
	}

	private boolean isState(String expected) {
		return expected.equals(state.state());
	}

	private String weaponName() {
		return switch (safeText(state.weaponTier()).toLowerCase(Locale.ROOT)) {
			case "wood", "wooden" -> "Wooden Farmer Hoe";
			case "stone" -> "Stone Farmer Hoe";
			case "iron" -> "Iron Farmer Hoe";
			case "gold", "golden" -> "Gold Farmer Hoe";
			case "diamond" -> "Diamond Farmer Hoe";
			default -> displayValue(state.weaponTier(), "Wooden") + " Farmer Hoe";
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
		return safeValue.isBlank() ? fallback : safeValue;
	}

	private static String labelValue(String value, String fallback) {
		String safeValue = safeText(value);
		if (safeValue.isBlank()) {
			return fallback;
		}
		if (safeValue.length() == 1) {
			return safeValue.toUpperCase(Locale.ROOT);
		}
		return safeValue.substring(0, 1).toUpperCase(Locale.ROOT)
				+ safeValue.substring(1).toLowerCase(Locale.ROOT);
	}

	private static String signed(int value) {
		return value > 0 ? "+" + value : Integer.toString(value);
	}

	private static String safeText(String value) {
		return value == null ? "" : value;
	}

	private record ButtonSpec(String label, Runnable action) {
	}

	private record PhoneLayout(
			int panelX,
			int panelY,
			int panelWidth,
			int panelBottom,
			int buttonX,
			int buttonStartY,
			int buttonWidth
	) {
	}
}
