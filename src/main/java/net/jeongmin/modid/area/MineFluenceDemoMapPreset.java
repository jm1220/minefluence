package net.jeongmin.modid.area;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.math.BlockPos;

public final class MineFluenceDemoMapPreset {
	public static final String DIMENSION_ID = "minecraft:overworld";

	private static final BlockPos GARDEN_MIN = new BlockPos(-15, -64, 11);
	private static final BlockPos GARDEN_MAX = new BlockPos(-6, -50, 21);
	private static final BlockPos FARM_MIN = new BlockPos(5, -64, 9);
	private static final BlockPos FARM_MAX = new BlockPos(35, -50, 37);
	private static final BlockPos SHARED_MIN = new BlockPos(11, -64, -18);
	private static final BlockPos SHARED_MAX = new BlockPos(20, -50, -5);
	private static final BlockPos FARM_BUILD_MIN = new BlockPos(-16, -64, 23);
	private static final BlockPos FARM_BUILD_MAX = new BlockPos(-5, -50, 34);

	private MineFluenceDemoMapPreset() {
	}

	public static Map<MineFluenceAreaType, MineFluenceArea> areas() {
		Map<MineFluenceAreaType, MineFluenceArea> areas = new LinkedHashMap<>();
		areas.put(MineFluenceAreaType.GARDEN, MineFluenceArea.box(MineFluenceAreaType.GARDEN, DIMENSION_ID, GARDEN_MIN, GARDEN_MAX));
		areas.put(MineFluenceAreaType.FARM, MineFluenceArea.box(MineFluenceAreaType.FARM, DIMENSION_ID, FARM_MIN, FARM_MAX));
		areas.put(MineFluenceAreaType.SHARED_SPACE, MineFluenceArea.box(MineFluenceAreaType.SHARED_SPACE, DIMENSION_ID, SHARED_MIN, SHARED_MAX));
		areas.put(MineFluenceAreaType.FARM_BUILD_AREA, MineFluenceArea.box(MineFluenceAreaType.FARM_BUILD_AREA, DIMENSION_ID, FARM_BUILD_MIN, FARM_BUILD_MAX));
		return Map.copyOf(areas);
	}
}
