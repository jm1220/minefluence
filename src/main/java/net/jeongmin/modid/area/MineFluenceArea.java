package net.jeongmin.modid.area;

import java.util.Locale;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record MineFluenceArea(
		MineFluenceAreaType type,
		String dimensionId,
		Shape shape,
		BlockPos center,
		int radius,
		BlockPos min,
		BlockPos max
) {
	public enum Shape {
		RADIUS,
		BOX;

		private static Shape fromSerializedName(String value) {
			if (value == null || value.isBlank()) {
				return null;
			}

			try {
				return Shape.valueOf(value.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException exception) {
				return null;
			}
		}
	}

	private static final String TYPE_KEY = "type";
	private static final String DIMENSION_KEY = "dimension";
	private static final String SHAPE_KEY = "shape";
	private static final String X_KEY = "x";
	private static final String Y_KEY = "y";
	private static final String Z_KEY = "z";
	private static final String RADIUS_KEY = "radius";
	private static final String MIN_X_KEY = "minX";
	private static final String MIN_Y_KEY = "minY";
	private static final String MIN_Z_KEY = "minZ";
	private static final String MAX_X_KEY = "maxX";
	private static final String MAX_Y_KEY = "maxY";
	private static final String MAX_Z_KEY = "maxZ";

	public MineFluenceArea {
		shape = shape == null ? Shape.RADIUS : shape;
		if (shape == Shape.RADIUS) {
			radius = Math.max(1, radius);
			center = center == null ? BlockPos.ORIGIN : center;
			min = new BlockPos(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
			max = new BlockPos(center.getX() + radius, center.getY() + radius, center.getZ() + radius);
		} else {
			BlockPos safeMin = min == null ? BlockPos.ORIGIN : min;
			BlockPos safeMax = max == null ? safeMin : max;
			min = normalizedMin(safeMin, safeMax);
			max = normalizedMax(safeMin, safeMax);
			center = midpoint(min, max);
			radius = 0;
		}
	}

	public static MineFluenceArea atPlayer(MineFluenceAreaType type, ServerPlayerEntity player, int radius) {
		return radius(
				type,
				player.getServerWorld().getRegistryKey().getValue().toString(),
				player.getBlockPos(),
				radius
		);
	}

	public static MineFluenceArea radius(MineFluenceAreaType type, String dimensionId, BlockPos center, int radius) {
		return new MineFluenceArea(type, dimensionId, Shape.RADIUS, center, radius, null, null);
	}

	public static MineFluenceArea box(MineFluenceAreaType type, String dimensionId, BlockPos firstCorner, BlockPos secondCorner) {
		return new MineFluenceArea(type, dimensionId, Shape.BOX, null, 0, firstCorner, secondCorner);
	}

	public static MineFluenceArea fromNbt(NbtCompound nbt) {
		if (!nbt.contains(TYPE_KEY, NbtElement.STRING_TYPE)
				|| !nbt.contains(DIMENSION_KEY, NbtElement.STRING_TYPE)) {
			return null;
		}

		MineFluenceAreaType type = MineFluenceAreaType.fromSerializedName(nbt.getString(TYPE_KEY));
		if (type == null) {
			return null;
		}

		String dimensionId = nbt.getString(DIMENSION_KEY);
		Shape shape = nbt.contains(SHAPE_KEY, NbtElement.STRING_TYPE)
				? Shape.fromSerializedName(nbt.getString(SHAPE_KEY))
				: null;

		if (shape == Shape.BOX || hasBoxBounds(nbt)) {
			if (!hasBoxBounds(nbt)) {
				return null;
			}
			return box(
					type,
					dimensionId,
					new BlockPos(nbt.getInt(MIN_X_KEY), nbt.getInt(MIN_Y_KEY), nbt.getInt(MIN_Z_KEY)),
					new BlockPos(nbt.getInt(MAX_X_KEY), nbt.getInt(MAX_Y_KEY), nbt.getInt(MAX_Z_KEY))
			);
		}

		if (!nbt.contains(X_KEY, NbtElement.INT_TYPE)
				|| !nbt.contains(Y_KEY, NbtElement.INT_TYPE)
				|| !nbt.contains(Z_KEY, NbtElement.INT_TYPE)
				|| !nbt.contains(RADIUS_KEY, NbtElement.INT_TYPE)) {
			return null;
		}

		return radius(
				type,
				dimensionId,
				new BlockPos(nbt.getInt(X_KEY), nbt.getInt(Y_KEY), nbt.getInt(Z_KEY)),
				Math.max(1, nbt.getInt(RADIUS_KEY))
		);
	}

	public NbtCompound writeNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString(TYPE_KEY, type.serializedName());
		nbt.putString(DIMENSION_KEY, dimensionId);
		nbt.putString(SHAPE_KEY, shape.name());
		if (shape == Shape.RADIUS) {
			nbt.putInt(X_KEY, center.getX());
			nbt.putInt(Y_KEY, center.getY());
			nbt.putInt(Z_KEY, center.getZ());
			nbt.putInt(RADIUS_KEY, radius);
		} else {
			nbt.putInt(MIN_X_KEY, min.getX());
			nbt.putInt(MIN_Y_KEY, min.getY());
			nbt.putInt(MIN_Z_KEY, min.getZ());
			nbt.putInt(MAX_X_KEY, max.getX());
			nbt.putInt(MAX_Y_KEY, max.getY());
			nbt.putInt(MAX_Z_KEY, max.getZ());
		}
		return nbt;
	}

	public boolean isInWorld(ServerWorld world) {
		return dimensionId.equals(world.getRegistryKey().getValue().toString());
	}

	public boolean contains(BlockPos pos) {
		return pos.getX() >= min.getX() && pos.getX() <= max.getX()
				&& pos.getY() >= min.getY() && pos.getY() <= max.getY()
				&& pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
	}

	public boolean contains(ServerWorld world, BlockPos pos) {
		return isInWorld(world) && contains(pos);
	}

	public String describe() {
		if (shape == Shape.BOX) {
			return "BOX min=" + posText(min) + ", max=" + posText(max) + ", dimension=" + dimensionId;
		}
		return "RADIUS center=" + posText(center) + ", radius=" + radius
				+ ", min=" + posText(min) + ", max=" + posText(max)
				+ ", dimension=" + dimensionId;
	}

	public String commandConfirmation() {
		if (shape == Shape.BOX) {
			return "box " + posText(min) + " -> " + posText(max);
		}
		return "radius center=" + posText(center) + ", radius=" + radius;
	}

	public static String posText(BlockPos pos) {
		return "(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
	}

	private static boolean hasBoxBounds(NbtCompound nbt) {
		return nbt.contains(MIN_X_KEY, NbtElement.INT_TYPE)
				&& nbt.contains(MIN_Y_KEY, NbtElement.INT_TYPE)
				&& nbt.contains(MIN_Z_KEY, NbtElement.INT_TYPE)
				&& nbt.contains(MAX_X_KEY, NbtElement.INT_TYPE)
				&& nbt.contains(MAX_Y_KEY, NbtElement.INT_TYPE)
				&& nbt.contains(MAX_Z_KEY, NbtElement.INT_TYPE);
	}

	private static BlockPos normalizedMin(BlockPos first, BlockPos second) {
		return new BlockPos(
				Math.min(first.getX(), second.getX()),
				Math.min(first.getY(), second.getY()),
				Math.min(first.getZ(), second.getZ())
		);
	}

	private static BlockPos normalizedMax(BlockPos first, BlockPos second) {
		return new BlockPos(
				Math.max(first.getX(), second.getX()),
				Math.max(first.getY(), second.getY()),
				Math.max(first.getZ(), second.getZ())
		);
	}

	private static BlockPos midpoint(BlockPos min, BlockPos max) {
		return new BlockPos(
				(min.getX() + max.getX()) / 2,
				(min.getY() + max.getY()) / 2,
				(min.getZ() + max.getZ()) / 2
		);
	}
}
