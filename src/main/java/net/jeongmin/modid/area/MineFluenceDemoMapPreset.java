package net.jeongmin.modid.area;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.jeongmin.modid.data.MineFluenceWorldState;
import net.minecraft.util.math.BlockPos;

public final class MineFluenceDemoMapPreset {
	public static final String DIMENSION_ID = "minecraft:overworld";
	private static final List<MineFluenceAreaType> AREA_TYPES = List.of(
			MineFluenceAreaType.GARDEN,
			MineFluenceAreaType.FARM,
			MineFluenceAreaType.SHARED_SPACE,
			MineFluenceAreaType.FARM_BUILD_AREA
	);
	private static final BlockPos VILLAGE_CENTER = new BlockPos(10, -60, 15);
	private static final BlockPos FAN_VILLAGE_CENTER = VILLAGE_CENTER;
	private static final List<BlockPos> FAN_SPAWN_POINTS = List.of(
			new BlockPos(-2, -60, 8),
			new BlockPos(2, -60, 8),
			new BlockPos(6, -60, 12),
			new BlockPos(10, -60, 16),
			new BlockPos(14, -60, 20),
			new BlockPos(18, -60, 24),
			new BlockPos(22, -60, 20),
			new BlockPos(18, -60, 12),
			new BlockPos(10, -60, 4),
			new BlockPos(4, -60, 2)
	);
	private static final List<BlockPos> INVASION_SUPPORT_SPAWN_POINTS = List.of(
			new BlockPos(0, -60, 10),
			new BlockPos(8, -60, 10),
			new BlockPos(16, -60, 16),
			new BlockPos(8, -60, 22),
			new BlockPos(20, -60, 10)
	);

	private static final int VERTICAL_PADDING_BELOW = 3;
	private static final int VERTICAL_PADDING_ABOVE = 6;

	private static final BlockPos GARDEN_CORNER_1 = new BlockPos(-827, 65, 723);
	private static final BlockPos GARDEN_CORNER_2 = new BlockPos(-839, 65, 711);
	private static final BlockPos FARM_CORNER_1 = new BlockPos(-857, 64, 689);
	private static final BlockPos FARM_CORNER_2 = new BlockPos(-839, 64, 709);
	private static final BlockPos SHARED_CORNER_1 = new BlockPos(-846, 64, 754);
	private static final BlockPos SHARED_CORNER_2 = new BlockPos(-833, 65, 764);
	private static final BlockPos FARM_BUILD_CORNER_1 = new BlockPos(-859, 68, 729);
	private static final BlockPos FARM_BUILD_CORNER_2 = new BlockPos(-867, 68, 723);

	private MineFluenceDemoMapPreset() {
	}

	public static Map<MineFluenceAreaType, MineFluenceArea> areas() {
		Map<MineFluenceAreaType, MineFluenceArea> areas = new LinkedHashMap<>();
		areas.put(MineFluenceAreaType.GARDEN, paddedArea(MineFluenceAreaType.GARDEN, GARDEN_CORNER_1, GARDEN_CORNER_2));
		areas.put(MineFluenceAreaType.FARM, paddedArea(MineFluenceAreaType.FARM, FARM_CORNER_1, FARM_CORNER_2));
		areas.put(MineFluenceAreaType.SHARED_SPACE, paddedArea(MineFluenceAreaType.SHARED_SPACE, SHARED_CORNER_1, SHARED_CORNER_2));
		areas.put(MineFluenceAreaType.FARM_BUILD_AREA, paddedArea(MineFluenceAreaType.FARM_BUILD_AREA, FARM_BUILD_CORNER_1, FARM_BUILD_CORNER_2));
		return Map.copyOf(areas);
	}

	public static List<MineFluenceAreaType> areaTypes() {
		return AREA_TYPES;
	}

	public static int loadInto(MineFluenceWorldState state, boolean overwriteExisting) {
		Map<MineFluenceAreaType, MineFluenceArea> presetAreas = areas();
		int loaded = 0;
		for (MineFluenceAreaType type : AREA_TYPES) {
			if (!overwriteExisting && state.getArea(type) != null) {
				continue;
			}

			MineFluenceArea area = presetAreas.get(type);
			if (area != null) {
				state.setArea(area);
				loaded++;
			}
		}
		return loaded;
	}

	public static String areaNameList() {
		return AREA_TYPES.stream()
				.map(MineFluenceAreaType::commandName)
				.reduce((left, right) -> left + ", " + right)
				.orElse("");
	}

	public static BlockPos fanVillageCenter() {
		return FAN_VILLAGE_CENTER;
	}

	public static List<BlockPos> fanSpawnPoints() {
		return FAN_SPAWN_POINTS;
	}

	public static BlockPos villageCenter() {
		return VILLAGE_CENTER;
	}

	public static List<BlockPos> invasionSupportSpawnPoints() {
		return INVASION_SUPPORT_SPAWN_POINTS;
	}

	private static MineFluenceArea paddedArea(
			MineFluenceAreaType type,
			BlockPos firstCorner,
			BlockPos secondCorner
	) {
		int minY = Math.min(firstCorner.getY(), secondCorner.getY()) - VERTICAL_PADDING_BELOW;
		int maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + VERTICAL_PADDING_ABOVE;
		BlockPos firstPaddedCorner = new BlockPos(firstCorner.getX(), minY, firstCorner.getZ());
		BlockPos secondPaddedCorner = new BlockPos(secondCorner.getX(), maxY, secondCorner.getZ());
		return MineFluenceArea.box(type, DIMENSION_ID, firstPaddedCorner, secondPaddedCorner);
	}
}
