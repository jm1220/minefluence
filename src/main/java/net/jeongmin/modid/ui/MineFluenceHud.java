package net.jeongmin.modid.ui;

import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.ending.MineFluenceEndingManager;
import net.jeongmin.modid.invasion.MineFluenceInvasionManager;
import net.jeongmin.modid.mission.MineFluenceMission;
import net.jeongmin.modid.mission.MineFluencePostingService;
import net.jeongmin.modid.network.MineFluenceHudStatePayload;
import net.jeongmin.modid.weapon.MineFluenceWeaponManager;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class MineFluenceHud {
	private static final String OBJECTIVE_NAME = "mf_hud";
	private static final int HUD_SYNC_TICK_INTERVAL = 20;
	private static final List<String> LINE_KEYS = List.of(
			"mf_hud_line_1",
			"mf_hud_line_2",
			"mf_hud_line_3",
			"mf_hud_line_4",
			"mf_hud_line_5",
			"mf_hud_line_6",
			"mf_hud_line_7"
	);

	private MineFluenceHud() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MineFluenceHud::syncAllCustomHudStates);
	}

	public static void refresh(ServerPlayerEntity player, MineFluencePlayerData data) {
		Scoreboard scoreboard = player.getServer().getScoreboard();
		ScoreboardObjective objective = getOrCreateObjective(scoreboard);
		if (scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) != objective) {
			scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
		}

		List<String> lines = List.of(
				"Follower: " + data.getFollower(),
				"Social: " + data.getSocialCredibility(),
				missionLabel(data),
				"Job: " + data.getSelectedJob(),
				"Weapon: " + MineFluenceWeaponManager.determineTier(data.getFollower()),
				"Invasion: " + invasionLabel(data),
				"Ending: " + MineFluenceEndingManager.endingDisplayName(data)
		);

		for (int index = 0; index < lines.size(); index++) {
			ScoreHolder holder = ScoreHolder.fromName(LINE_KEYS.get(index));
			ScoreAccess score = scoreboard.getOrCreateScore(holder, objective);
			score.setScore(lines.size() - index);
			score.setDisplayText(Text.literal(lines.get(index)));
			score.setNumberFormat(BlankNumberFormat.INSTANCE);
		}

		syncCustomHud(player, data);
	}

	private static void syncAllCustomHudStates(MinecraftServer server) {
		if (server.getTicks() % HUD_SYNC_TICK_INTERVAL != 0) {
			return;
		}

		MineFluenceWorldState state = MineFluenceWorldState.get(server);
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			syncCustomHud(player, state.getPlayerData(player));
		}
	}

	private static void syncCustomHud(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (!ServerPlayNetworking.canSend(player, MineFluenceHudStatePayload.ID)) {
			return;
		}

		ServerPlayNetworking.send(player, hudState(player, data));
	}

	private static MineFluenceHudStatePayload hudState(ServerPlayerEntity player, MineFluencePlayerData data) {
		MineFluenceMission activeMission = null;
		if (data.hasActiveMission()) {
			activeMission = MineFluencePostingService.missionOrFallback(data.getActiveMissionIndex(), data.getActiveMissionRoute());
		}

		int missionProgress = 0;
		int missionTarget = 0;
		String missionRoute = "";
		String missionTitle = "";
		String missionObjective = "";
		if (activeMission != null) {
			missionProgress = Math.min(data.getActiveMissionProgress(), activeMission.targetProgress());
			missionTarget = activeMission.targetProgress();
			missionRoute = activeMission.route().serializedName();
			missionTitle = activeMission.title();
			missionObjective = activeMission.objectiveText();
		}

		int invasionRemaining = 0;
		int invasionTotal = 0;
		if (data.hasActiveInvasion()) {
			invasionRemaining = MineFluenceInvasionManager.countRemainingTrackedMobs(player.getServer(), data);
			invasionTotal = Math.max(data.getActiveInvasionTotal(), invasionRemaining);
		}

		return new MineFluenceHudStatePayload(
				data.getFollower(),
				data.getSocialCredibility(),
				data.getLieValue(),
				data.getCompletedMissionCount(),
				data.getSelectedJob().serializedName(),
				data.getActiveMissionIndex(),
				missionRoute,
				missionTitle,
				missionObjective,
				missionProgress,
				missionTarget,
				data.isWaitingForPostingChoice(),
				data.getPendingPostingMissionIndex(),
				data.getPendingPostingMissionRoute().serializedName(),
				data.getActiveInvasionIndex(),
				invasionRemaining,
				invasionTotal,
				data.isEndingTriggered() ? MineFluenceEndingManager.endingDisplayName(data) : ""
		);
	}

	private static ScoreboardObjective getOrCreateObjective(Scoreboard scoreboard) {
		ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
		if (objective != null) {
			objective.setDisplayName(Text.literal("MineFluence"));
			objective.setNumberFormat(BlankNumberFormat.INSTANCE);
			return objective;
		}

		return scoreboard.addObjective(
				OBJECTIVE_NAME,
				ScoreboardCriterion.DUMMY,
				Text.literal("MineFluence"),
				ScoreboardCriterion.RenderType.INTEGER,
				false,
				BlankNumberFormat.INSTANCE
		);
	}

	private static String invasionLabel(MineFluencePlayerData data) {
		if (!data.hasActiveInvasion()) {
			return "None";
		}
		return Integer.toString(data.getActiveInvasionIndex());
	}

	private static String missionLabel(MineFluencePlayerData data) {
		if (data.hasPendingMissionSelection()) {
			return "Mission: " + data.getPendingMissionSelectionIndex() + " Choice";
		}
		if (data.isWaitingForPostingChoice()) {
			return "Mission: " + data.getPendingPostingMissionIndex() + " " + data.getPendingPostingMissionRoute() + " Post";
		}
		if (data.hasActiveMission()) {
			return "Mission: " + data.getActiveMissionIndex() + " " + data.getActiveMissionRoute();
		}
		return "Mission: " + data.getCompletedMissionCount() + "/" + MineFluenceBalance.TOTAL_DEMO_MISSIONS;
	}
}
