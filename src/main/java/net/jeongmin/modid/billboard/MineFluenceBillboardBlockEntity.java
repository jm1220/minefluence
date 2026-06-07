package net.jeongmin.modid.billboard;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public final class MineFluenceBillboardBlockEntity extends BlockEntity {
	private static final String WIDTH_BLOCKS_KEY = "widthBlocks";
	private static final String HEIGHT_BLOCKS_KEY = "heightBlocks";
	private static final String LEGACY_WIDTH_KEY = "width";
	private static final String LEGACY_HEIGHT_KEY = "height";
	private static final String GROUP_KEY = "group";
	private static final String IMAGE_ID_KEY = "imageId";
	private static final String AUTO_MODE_KEY = "autoMode";

	public static final int DEFAULT_WIDTH_BLOCKS = 8;
	public static final int DEFAULT_HEIGHT_BLOCKS = 4;
	public static final int MIN_WIDTH_BLOCKS = 1;
	public static final int MAX_WIDTH_BLOCKS = 32;
	public static final int MIN_HEIGHT_BLOCKS = 1;
	public static final int MAX_HEIGHT_BLOCKS = 18;

	private int widthBlocks = DEFAULT_WIDTH_BLOCKS;
	private int heightBlocks = DEFAULT_HEIGHT_BLOCKS;
	private String group = MineFluenceBillboards.MAIN_GROUP;
	private String imageId = MineFluenceBillboardImageResolver.DEFAULT_IMAGE_ID;
	private boolean autoMode = true;

	public MineFluenceBillboardBlockEntity(BlockPos pos, BlockState state) {
		super(MineFluenceBillboards.BILLBOARD_BLOCK_ENTITY, pos, state);
	}

	public int getWidthBlocks() {
		return widthBlocks;
	}

	public void setWidthBlocks(int widthBlocks) {
		this.widthBlocks = clampWidth(widthBlocks);
		sync();
	}

	public int getHeightBlocks() {
		return heightBlocks;
	}

	public void setHeightBlocks(int heightBlocks) {
		this.heightBlocks = clampHeight(heightBlocks);
		sync();
	}

	public void setSizeBlocks(int widthBlocks, int heightBlocks) {
		this.widthBlocks = clampWidth(widthBlocks);
		this.heightBlocks = clampHeight(heightBlocks);
		sync();
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = MineFluenceBillboards.normalizeGroup(group);
		sync();
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = MineFluenceBillboardImageResolver.normalizeImageId(imageId);
		sync();
	}

	public boolean isAutoMode() {
		return autoMode;
	}

	public void setAutoMode(boolean autoMode) {
		this.autoMode = autoMode;
		sync();
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		if (nbt.contains(WIDTH_BLOCKS_KEY, NbtElement.INT_TYPE)) {
			widthBlocks = clampWidth(nbt.getInt(WIDTH_BLOCKS_KEY));
		} else if (nbt.contains(LEGACY_WIDTH_KEY, NbtElement.INT_TYPE)) {
			widthBlocks = clampWidth(nbt.getInt(LEGACY_WIDTH_KEY));
		}
		if (nbt.contains(HEIGHT_BLOCKS_KEY, NbtElement.INT_TYPE)) {
			heightBlocks = clampHeight(nbt.getInt(HEIGHT_BLOCKS_KEY));
		} else if (nbt.contains(LEGACY_HEIGHT_KEY, NbtElement.INT_TYPE)) {
			heightBlocks = clampHeight(nbt.getInt(LEGACY_HEIGHT_KEY));
		}
		if (nbt.contains(GROUP_KEY, NbtElement.STRING_TYPE)) {
			group = MineFluenceBillboards.normalizeGroup(nbt.getString(GROUP_KEY));
		}
		if (nbt.contains(IMAGE_ID_KEY, NbtElement.STRING_TYPE)) {
			imageId = MineFluenceBillboardImageResolver.normalizeImageId(nbt.getString(IMAGE_ID_KEY));
		}
		if (nbt.contains(AUTO_MODE_KEY, NbtElement.BYTE_TYPE)) {
			autoMode = nbt.getBoolean(AUTO_MODE_KEY);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putInt(WIDTH_BLOCKS_KEY, widthBlocks);
		nbt.putInt(HEIGHT_BLOCKS_KEY, heightBlocks);
		nbt.putString(GROUP_KEY, group);
		nbt.putString(IMAGE_ID_KEY, imageId);
		nbt.putBoolean(AUTO_MODE_KEY, autoMode);
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}

	private void sync() {
		markDirty();
		if (world != null) {
			BlockState state = world.getBlockState(pos);
			world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
		}
	}

	private static int clampWidth(int value) {
		return Math.max(MIN_WIDTH_BLOCKS, Math.min(MAX_WIDTH_BLOCKS, value));
	}

	private static int clampHeight(int value) {
		return Math.max(MIN_HEIGHT_BLOCKS, Math.min(MAX_HEIGHT_BLOCKS, value));
	}
}
