package net.jeongmin.modid.mission;

import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class MineFluenceMissionCompletionService {
	private static final int TITLE_FADE_IN_TICKS = 10;
	private static final int TITLE_STAY_TICKS = 40;
	private static final int TITLE_FADE_OUT_TICKS = 10;

	private MineFluenceMissionCompletionService() {
	}

	public static boolean complete(
			ServerPlayerEntity player,
			MineFluenceWorldState state,
			MineFluencePlayerData data,
			MineFluenceMission mission
	) {
		if (player == null
				|| state == null
				|| data == null
				|| mission == null
				|| !data.hasActiveMission()
				|| data.isWaitingForPostingChoice()
				|| data.getActiveMissionIndex() != mission.index()
				|| data.getActiveMissionRoute() != mission.route()) {
			return false;
		}

		int missionIndex = data.getActiveMissionIndex();
		data.setActiveMissionProgress(mission.targetProgress());
		data.markActiveMissionReadyToPost();
		state.markDirty();
		showClearFeedback(player, missionIndex);
		return true;
	}

	private static void showClearFeedback(ServerPlayerEntity player, int missionIndex) {
		player.networkHandler.sendPacket(new TitleFadeS2CPacket(
				TITLE_FADE_IN_TICKS,
				TITLE_STAY_TICKS,
				TITLE_FADE_OUT_TICKS
		));
		player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Mission " + missionIndex + " Clear!")));
		player.networkHandler.sendPacket(new SubtitleS2CPacket(
				Text.literal("Open your smartphone to upload your post.")
		));
		player.playSoundToPlayer(
				SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
				SoundCategory.MASTER,
				1.0F,
				1.0F
		);
	}
}
