package net.jeongmin.modid.ending;

import java.util.Locale;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.data.MineFluencePlayerData;
import net.jeongmin.modid.data.MineFluenceWorldState;
import net.jeongmin.modid.network.MineFluenceNetworking;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.jeongmin.modid.ui.MineFluenceHud;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class MineFluenceEndingManager {
	private MineFluenceEndingManager() {
	}

	public static MineFluenceEndingTier calculateFollowerTier(int follower) {
		if (follower >= MineFluenceBalance.ENDING_FOLLOWER_HIGH_THRESHOLD) {
			return MineFluenceEndingTier.HIGH;
		}
		if (follower >= MineFluenceBalance.ENDING_FOLLOWER_MID_THRESHOLD) {
			return MineFluenceEndingTier.MID;
		}
		return MineFluenceEndingTier.LOW;
	}

	public static MineFluenceEndingTier calculateSocialTier(int socialCredibility) {
		if (socialCredibility >= MineFluenceBalance.ENDING_SOCIAL_CREDIBILITY_HIGH_THRESHOLD) {
			return MineFluenceEndingTier.HIGH;
		}
		if (socialCredibility >= MineFluenceBalance.ENDING_SOCIAL_CREDIBILITY_MID_THRESHOLD) {
			return MineFluenceEndingTier.MID;
		}
		return MineFluenceEndingTier.LOW;
	}

	public static MineFluenceEnding getEnding(MineFluencePlayerData data) {
		if (data.isExposureTriggered()) {
			return famousVillainEnding();
		}
		return MineFluenceEndingRegistry.get(
				calculateFollowerTier(data.getFollower()),
				calculateSocialTier(data.getSocialCredibility())
		);
	}

	public static boolean triggerEndingIfReady(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (!isEndingReady(data)) {
			return false;
		}

		triggerEnding(player, data);
		return true;
	}

	public static boolean isEndingReady(MineFluencePlayerData data) {
		return data.getCompletedMissionCount() >= MineFluenceBalance.TOTAL_DEMO_MISSIONS
				&& data.getLastCompletedInvasionIndex() >= MineFluenceBalance.LAST_COMPLETED_INVASION_MAX
				&& !data.isEndingTriggered();
	}

	public static MineFluenceEnding triggerEnding(ServerPlayerEntity player, MineFluencePlayerData data) {
		boolean wasAlreadyTriggered = data.isEndingTriggered();
		MineFluenceEnding ending = wasAlreadyTriggered ? endingFromTriggeredData(data) : getEnding(data);
		if (!wasAlreadyTriggered) {
			data.setEndingTriggered(true);
			data.setEndingId(ending.id());
			MineFluenceWorldState.get(player.getServer()).markDirty();
		}
		showEndingSummary(player, data, ending, true);
		if (!wasAlreadyTriggered && isTheFamousVillainEnding(ending)) {
			MineFluenceNetworking.playEndingVideo(player);
		}
		MineFluenceHud.refresh(player, data);
		return ending;
	}

	public static boolean triggerExposureCollapse(ServerPlayerEntity player, MineFluencePlayerData data) {
		if (data.isExposureTriggered()) {
			return false;
		}

		MineFluenceEnding ending = famousVillainEnding();
		data.setExposureTriggered(true);
		data.setEndingTriggered(true);
		data.setEndingId(ending.id());
		data.clearMissionFlow();
		data.clearInvasionState();
		MineFluenceWorldState.get(player.getServer()).markDirty();

		MineFluenceDisplay.sendChat(player, "Your lies have been exposed.");
		MineFluenceDisplay.sendChat(player, "Everything collapses.");
		showEndingSummary(player, data, ending, true);
		MineFluenceNetworking.playEndingVideo(player);
		MineFluenceHud.refresh(player, data);
		return true;
	}

	public static MineFluenceEnding previewEnding(ServerPlayerEntity player, MineFluencePlayerData data) {
		MineFluenceEnding ending = getEnding(data);
		showEndingSummary(player, data, ending, false);
		return ending;
	}

	public static String endingDisplayName(MineFluencePlayerData data) {
		if (!data.isEndingTriggered()) {
			return "None";
		}
		return MineFluenceEndingRegistry.getById(data.getEndingId())
				.map(MineFluenceEnding::displayName)
				.orElse(data.getEndingId().isBlank() ? "Unknown" : data.getEndingId());
	}

	public static int representativeFollowerValue(MineFluenceEndingTier tier) {
		return switch (tier) {
			case LOW -> 10;
			case MID -> MineFluenceBalance.ENDING_FOLLOWER_MID_THRESHOLD + 20;
			case HIGH -> MineFluenceBalance.ENDING_FOLLOWER_HIGH_THRESHOLD + 20;
		};
	}

	public static int representativeSocialCredibilityValue(MineFluenceEndingTier tier) {
		return switch (tier) {
			case LOW -> -100;
			case MID -> 100;
			case HIGH -> MineFluenceBalance.ENDING_SOCIAL_CREDIBILITY_HIGH_THRESHOLD + 50;
		};
	}

	public static boolean isTheFamousVillainEnding(MineFluenceEnding ending) {
		if (ending == null) {
			return false;
		}

		String id = ending.id() == null ? "" : ending.id().trim().toLowerCase(Locale.ROOT);
		if (MineFluenceEndingVideos.THE_FAMOUS_VILLAIN_ID.equals(id)
				|| "villain_influencer".equals(id)
				|| "ending_high_low".equals(id)) {
			return true;
		}
		return ending.followerTier() == MineFluenceEndingTier.HIGH && ending.socialTier() == MineFluenceEndingTier.LOW;
	}

	private static MineFluenceEnding famousVillainEnding() {
		return MineFluenceEndingRegistry.getById(MineFluenceEndingVideos.THE_FAMOUS_VILLAIN_ID)
				.orElseGet(() -> MineFluenceEndingRegistry.get(MineFluenceEndingTier.HIGH, MineFluenceEndingTier.LOW));
	}

	private static MineFluenceEnding endingFromTriggeredData(MineFluencePlayerData data) {
		return MineFluenceEndingRegistry.getById(data.getEndingId()).orElseGet(() -> getEnding(data));
	}

	private static void showEndingSummary(ServerPlayerEntity player, MineFluencePlayerData data, MineFluenceEnding ending, boolean triggered) {
		sendTitle(player, ending.displayName());
		MineFluenceDisplay.sendChat(player, triggered ? "Ending: " + ending.displayName() : "Ending Preview: " + ending.displayName());
		MineFluenceDisplay.sendChat(player, "Follower Tier: " + ending.followerTier() + " (Follower=" + data.getFollower() + ")");
		MineFluenceDisplay.sendChat(player, "Social Credibility Tier: " + ending.socialTier() + " (Social Credibility=" + data.getSocialCredibility() + ")");
		MineFluenceDisplay.sendChat(player, "Description: " + ending.description());
		MineFluenceDisplay.sendChat(player, "Future media:");
		MineFluenceDisplay.sendChat(player, "- Follower video/image: " + ending.followerMediaPath());
		MineFluenceDisplay.sendChat(player, "- Social video/image: " + ending.socialMediaPath());
		MineFluenceDisplay.sendChat(player, "- Combined ending media: " + ending.combinedMediaPath());
		MineFluenceDisplay.sendActionBar(player, "[MineFluence] Ending: " + ending.displayName());
	}

	private static void sendTitle(ServerPlayerEntity player, String subtitle) {
		player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 80, 20));
		player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("MineFluence Ending")));
		player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(subtitle)));
	}
}
