package net.jeongmin.modid.billboard;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public final class MineFluenceBillboardBlockEntityRenderer implements BlockEntityRenderer<MineFluenceBillboardBlockEntity> {
	public MineFluenceBillboardBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(
			MineFluenceBillboardBlockEntity billboard,
			float tickDelta,
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay
	) {
		Identifier texture = resolveTexture(billboard.getImageId());
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));

		BlockState state = billboard.getCachedState();
		Direction facing = state.get(MineFluenceBillboardBlock.FACING);
		float frontX = facing.getOffsetX();
		float frontZ = facing.getOffsetZ();
		float rightX = -frontZ;
		float rightZ = frontX;

		float halfWidth = billboard.getWidthBlocks() * 0.5F;
		float height = billboard.getHeightBlocks();
		float centerX = 0.5F + frontX * 0.53F;
		float centerZ = 0.5F + frontZ * 0.53F;

		float leftX = centerX - rightX * halfWidth;
		float leftZ = centerZ - rightZ * halfWidth;
		float rightEdgeX = centerX + rightX * halfWidth;
		float rightEdgeZ = centerZ + rightZ * halfWidth;

		MatrixStack.Entry entry = matrices.peek();
		vertex(vertices, entry, leftX, height, leftZ, 0.0F, 0.0F, light, frontX, frontZ);
		vertex(vertices, entry, leftX, 0.0F, leftZ, 0.0F, 1.0F, light, frontX, frontZ);
		vertex(vertices, entry, rightEdgeX, 0.0F, rightEdgeZ, 1.0F, 1.0F, light, frontX, frontZ);
		vertex(vertices, entry, rightEdgeX, height, rightEdgeZ, 1.0F, 0.0F, light, frontX, frontZ);
	}

	@Override
	public boolean rendersOutsideBoundingBox(MineFluenceBillboardBlockEntity billboard) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}

	private static Identifier resolveTexture(String imageId) {
		Identifier requested = MineFluenceBillboardImageResolver.textureForImageId(imageId);
		if (MinecraftClient.getInstance().getResourceManager().getResource(requested).isPresent()) {
			return requested;
		}
		return MineFluenceBillboardImageResolver.textureForImageId(MineFluenceBillboardImageResolver.DEFAULT_IMAGE_ID);
	}

	private static void vertex(
			VertexConsumer vertices,
			MatrixStack.Entry entry,
			float x,
			float y,
			float z,
			float u,
			float v,
			int light,
			float normalX,
			float normalZ
	) {
		vertices.vertex(entry, x, y, z)
				.color(255, 255, 255, 255)
				.texture(u, v)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(light)
				.normal(entry, normalX, 0.0F, normalZ);
	}
}
