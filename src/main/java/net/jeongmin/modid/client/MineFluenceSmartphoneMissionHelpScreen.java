package net.jeongmin.modid.client;

import java.util.ArrayList;
import java.util.List;

import net.jeongmin.modid.mission.FarmerMissions;
import net.jeongmin.modid.mission.MineFluenceMission;
import net.jeongmin.modid.network.MineFluencePhoneStateResponsePayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class MineFluenceSmartphoneMissionHelpScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFECEFF2;
	private static final int MUTED_TEXT_COLOR = 0xFFB8C0CA;
	private static final int GOOD_COLOR = 0xFF63D487;
	private static final int BAD_COLOR = 0xFFE06B6B;
	private static final int MISSION_TITLE_COLOR = 0xFFFFD27A;
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

	public MineFluenceSmartphoneMissionHelpScreen(
			MineFluencePhoneStateResponsePayload state,
			Screen returnScreen
	) {
		super(Text.literal("Mission Help"));
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
				"마우스 휠로 모든 미션 설명을 확인하세요.",
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

		y = addLine("Farmer Missions (Good)", GOOD_COLOR, y, maxWidth, 5);
		List<MineFluenceMission> goodMissions = FarmerMissions.allGood();
		y = addMission(goodMissions.get(0), List.of(
				"화단 밖 잔디밭에 자란 꽃을 좌클릭으로 부숴서 획득할 수 있습니다.",
				"그 후 마을 garden 영역으로 이동해 바닥을 우클릭하여 꽃 "
						+ goodMissions.get(0).targetProgress() + "개를 심어주세요."
		), y, maxWidth);
		y = addMission(goodMissions.get(1), List.of(
				"긴 잔디를 부수면 확률적으로 밀 씨앗을 얻을 수 있습니다.",
				"호미로 흙이나 잔디 블록을 우클릭해 경작지로 만든 뒤, farm 영역에 밀 씨앗 "
						+ goodMissions.get(1).targetProgress() + "개를 심어주세요."
		), y, maxWidth);
		y = addMission(goodMissions.get(2), List.of(
				"밀짚모자를 쓴 농부 주민을 찾아 우클릭하면 거래 창이 열립니다.",
				"현재 미션 판정은 거래 완료 횟수가 아니라 농부 주민 우클릭 "
						+ goodMissions.get(2).targetProgress() + "회입니다."
		), y, maxWidth);
		y = addMission(goodMissions.get(3), List.of(
				"감자를 손에 든 상태로 일반 주민을 우클릭하세요.",
				"우클릭할 때 감자 1개가 전달되며, 총 "
						+ goodMissions.get(3).targetProgress() + "번 전달하면 완료됩니다. Q키로 던지는 방식은 현재 판정되지 않습니다."
		), y, maxWidth);
		y = addMission(goodMissions.get(4), List.of(
				"농부 직업 블록인 퇴비통(Composter) "
						+ goodMissions.get(4).targetProgress() + "개를 제작해 인벤토리에 넣으세요.",
				"조합대에서 나무 반블록 7개를 U자 모양으로 배치하면 퇴비통 1개를 만들 수 있습니다.",
				"퇴비통 2개에는 나무 반블록 14개가 필요하며, 나무판자 6개(원목 약 2개)로 만들 수 있습니다."
		), y, maxWidth);
		y = addMission(goodMissions.get(5), List.of(
				"조합대의 9칸을 밀로 채우면 건초더미 1개를 만들 수 있습니다.",
				"밀은 물 근처의 경작지에 씨앗을 심어 기르고, 뼛가루를 사용하면 빠르게 성장시킬 수 있습니다.",
				"뼛가루는 스켈레톤을 처치해 얻은 뼈를 조합 칸에서 분해하면 얻을 수 있습니다.",
				"건초더미를 만든 뒤 shared 영역에 "
						+ goodMissions.get(5).targetProgress() + "개 설치하세요."
		), y, maxWidth);
		y = addMission(goodMissions.get(6), List.of(
				"farm_build 영역의 중심 한 칸을 파고 물양동이로 물을 채우세요.",
				"물 주변 8칸을 호미로 경작지로 만들고, 밭 옆에 퇴비통 1개를 설치하세요.",
				"현재 판정은 영역 안의 물 1칸, 경작지 8칸, 퇴비통 1개입니다.",
				"지하에서 철광석을 채굴해 화로에서 구우면 철 주괴를 얻을 수 있습니다.",
				"양동이는 철 주괴 3개를 V자 모양으로 배치해 만들며, 강이나 바다의 물 소스를 우클릭하면 물양동이가 됩니다.",
				"퇴비통 1개는 조합대에서 나무 반블록 7개를 U자 모양으로 배치해 제작합니다."
		), y, maxWidth);

		y = addLine("Farmer Missions (Bad)", BAD_COLOR, y + 6, maxWidth, 5);
		List<MineFluenceMission> badMissions = FarmerMissions.allBad();
		y = addMission(badMissions.get(0), List.of(
				"마을의 황금색 종을 찾아 우클릭하세요.",
				"false alarm으로 종을 총 " + badMissions.get(0).targetProgress() + "번 울리면 완료됩니다."
		), y, maxWidth);
		y = addMission(badMissions.get(1), List.of(
				"farm 영역의 경작지 위에서 점프해 흙으로 만들거나 경작지 블록을 직접 부수세요.",
				"총 " + badMissions.get(1).targetProgress() + "개의 경작지를 망가뜨리면 완료됩니다."
		), y, maxWidth);
		y = addMission(badMissions.get(2), List.of(
				"일반 마을 주민을 좌클릭하여 총 " + badMissions.get(2).targetProgress() + "번 때리세요."
		), y, maxWidth);
		y = addMission(badMissions.get(3), List.of(
				"shared 영역 안의 상자 또는 통을 열고 아이템을 인벤토리로 옮기세요.",
				"총 " + badMissions.get(3).targetProgress() + "개를 꺼낸 뒤 상자 화면을 닫으면 진행도가 반영됩니다."
		), y, maxWidth);
		y = addMission(badMissions.get(4), List.of(
				"퇴비통을 찾아 좌클릭을 길게 눌러 총 " + badMissions.get(4).targetProgress() + "개 부수세요."
		), y, maxWidth);
		y = addMission(badMissions.get(5), List.of(
				"farm 영역에서 경작지나 밀, 당근, 감자, 비트 작물 블록을 부수세요.",
				"현재 미션 판정은 농장 전체가 아니라 유효한 농장 블록 "
						+ badMissions.get(5).targetProgress() + "개 파괴입니다."
		), y, maxWidth);
		y = addMission(badMissions.get(6), List.of(
				"일반 주민을 지속적으로 좌클릭하여 처치하세요.",
				"정확한 목표 수는 현재 미션 목표 표시를 따르세요."
		), y, maxWidth);

		contentHeight = Math.max(0, y);
	}

	private int addMission(
			MineFluenceMission mission,
			List<String> paragraphs,
			int y,
			int maxWidth
	) {
		y = addLine(mission.index() + ". " + mission.title(), MISSION_TITLE_COLOR, y + 5, maxWidth, 2);
		for (String paragraph : paragraphs) {
			y = addLine(paragraph, TEXT_COLOR, y, maxWidth, 2);
		}
		return y + 3;
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

	private record RenderedHelpLine(OrderedText text, int color, int y) {
	}
}
