package net.jeongmin.modid.ending;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.ui.MineFluenceDisplay;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MineFluenceEndingVideoLauncher {
	public static final String THE_FAMOUS_VILLAIN_ID = "the_famous_villain";
	public static final String THE_FAMOUS_VILLAIN_DISPLAY_NAME = "The Famous Villain";
	public static final String EXPECTED_DEMO_PATH = "run/minefluence_videos/the_famous_villain.mp4";

	private static final String VIDEO_FILE_NAME = "the_famous_villain.mp4";
	private static final Path RESOURCE_FALLBACK_PATH = Path.of(
			"src",
			"main",
			"resources",
			"assets",
			MineFluence.MOD_ID,
			"textures",
			"billboard",
			"ending",
			VIDEO_FILE_NAME
	);

	private MineFluenceEndingVideoLauncher() {
	}

	public static boolean launchTheFamousVillain(ServerPlayerEntity player) {
		Path videoPath = findTheFamousVillainVideo();
		if (videoPath == null) {
			warn(player, "Ending video not found. Expected " + EXPECTED_DEMO_PATH);
			warn(player, "Could not open ending video. Check " + EXPECTED_DEMO_PATH);
			return false;
		}

		Desktop desktop = supportedDesktop(player);
		if (desktop == null) {
			return false;
		}

		try {
			desktop.open(videoPath.toFile());
			MineFluenceDisplay.sendChat(player, "Playing ending video: " + THE_FAMOUS_VILLAIN_DISPLAY_NAME);
			return true;
		} catch (IOException | RuntimeException exception) {
			warn(player, "Could not open ending video. Check " + EXPECTED_DEMO_PATH);
			MineFluence.LOGGER.warn("Could not open MineFluence ending video at {}.", videoPath.toAbsolutePath(), exception);
			return false;
		}
	}

	private static Path findTheFamousVillainVideo() {
		for (Path candidate : videoCandidates()) {
			if (Files.isRegularFile(candidate)) {
				return candidate.toAbsolutePath().normalize();
			}
		}
		return null;
	}

	private static List<Path> videoCandidates() {
		List<Path> candidates = new ArrayList<>();
		Path gameDir = FabricLoader.getInstance().getGameDir();
		candidates.add(gameDir.resolve("minefluence_videos").resolve(VIDEO_FILE_NAME));
		candidates.add(Path.of("run", "minefluence_videos", VIDEO_FILE_NAME));
		candidates.add(RESOURCE_FALLBACK_PATH);

		Path gameDirParent = gameDir.getParent();
		if (gameDirParent != null) {
			candidates.add(gameDirParent.resolve(RESOURCE_FALLBACK_PATH));
		}
		return candidates;
	}

	private static Desktop supportedDesktop(ServerPlayerEntity player) {
		try {
			if (!Desktop.isDesktopSupported()) {
				warn(player, "Could not open ending video. Desktop OPEN is not supported on this system.");
				return null;
			}

			Desktop desktop = Desktop.getDesktop();
			if (!desktop.isSupported(Desktop.Action.OPEN)) {
				warn(player, "Could not open ending video. Desktop OPEN is not supported on this system.");
				return null;
			}
			return desktop;
		} catch (RuntimeException exception) {
			warn(player, "Could not open ending video. Check " + EXPECTED_DEMO_PATH);
			MineFluence.LOGGER.warn("Could not access java.awt.Desktop for MineFluence ending video.", exception);
			return null;
		}
	}

	private static void warn(ServerPlayerEntity player, String message) {
		MineFluenceDisplay.sendChat(player, message);
		MineFluence.LOGGER.warn(message);
	}
}
