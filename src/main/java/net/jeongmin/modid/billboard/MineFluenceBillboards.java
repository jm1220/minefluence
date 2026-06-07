package net.jeongmin.modid.billboard;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import net.jeongmin.modid.MineFluence;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class MineFluenceBillboards {
	public static final String MAIN_GROUP = "main";
	private static final int LOOKUP_DISTANCE = 16;

	private static final Identifier BILLBOARD_ANCHOR_ID = Identifier.of(MineFluence.MOD_ID, "billboard_anchor");

	public static final MineFluenceBillboardBlock BILLBOARD_ANCHOR = Registry.register(
			Registries.BLOCK,
			BILLBOARD_ANCHOR_ID,
			new MineFluenceBillboardBlock(AbstractBlock.Settings.create().strength(0.8F).nonOpaque())
	);

	public static final Item BILLBOARD_ANCHOR_ITEM = Registry.register(
			Registries.ITEM,
			BILLBOARD_ANCHOR_ID,
			new BlockItem(BILLBOARD_ANCHOR, new Item.Settings())
	);

	public static final BlockEntityType<MineFluenceBillboardBlockEntity> BILLBOARD_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			BILLBOARD_ANCHOR_ID,
			BlockEntityType.Builder.create(MineFluenceBillboardBlockEntity::new, BILLBOARD_ANCHOR).build(null)
	);

	private MineFluenceBillboards() {
	}

	public static void register() {
		MineFluence.LOGGER.info("Registered MineFluence billboard blocks.");
	}

	public static Optional<MineFluenceBillboardBlockEntity> findLookedAtBillboard(ServerPlayerEntity player) {
		HitResult hitResult = player.raycast(LOOKUP_DISTANCE, 0.0F, false);
		if (hitResult.getType() != HitResult.Type.BLOCK) {
			return Optional.empty();
		}

		BlockEntity blockEntity = player.getServerWorld().getBlockEntity(((BlockHitResult) hitResult).getBlockPos());
		if (blockEntity instanceof MineFluenceBillboardBlockEntity billboard) {
			return Optional.of(billboard);
		}
		return Optional.empty();
	}

	public static int setGroupImage(MinecraftServer server, String group, String imageId) {
		String normalizedGroup = normalizeGroup(group);
		String normalizedImageId = MineFluenceBillboardImageResolver.normalizeImageId(imageId);
		int updated = 0;
		for (ServerWorld world : server.getWorlds()) {
			updated += setGroupImageInLoadedChunks(world, normalizedGroup, normalizedImageId, server.getPlayerManager().getViewDistance() + 1);
		}
		return updated;
	}

	public static String normalizeGroup(String group) {
		if (group == null) {
			return MAIN_GROUP;
		}

		String normalized = group.trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty() || !Identifier.isPathValid(normalized)) {
			return MAIN_GROUP;
		}
		return normalized;
	}

	private static int setGroupImageInLoadedChunks(ServerWorld world, String group, String imageId, int chunkRadius) {
		List<ServerPlayerEntity> players = world.getPlayers();
		if (players.isEmpty()) {
			return 0;
		}

		int updated = 0;
		int radius = Math.max(2, chunkRadius);
		Set<Long> visitedChunks = new HashSet<>();
		for (ServerPlayerEntity player : players) {
			ChunkPos center = player.getChunkPos();
			for (int chunkX = center.x - radius; chunkX <= center.x + radius; chunkX++) {
				for (int chunkZ = center.z - radius; chunkZ <= center.z + radius; chunkZ++) {
					long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
					if (!visitedChunks.add(chunkKey) || !world.isChunkLoaded(chunkKey)) {
						continue;
					}

					WorldChunk chunk = world.getChunk(chunkX, chunkZ);
					for (BlockEntity blockEntity : List.copyOf(chunk.getBlockEntities().values())) {
						if (blockEntity instanceof MineFluenceBillboardBlockEntity billboard && group.equals(billboard.getGroup())) {
							billboard.setImageId(imageId);
							updated++;
						}
					}
				}
			}
		}
		return updated;
	}
}
