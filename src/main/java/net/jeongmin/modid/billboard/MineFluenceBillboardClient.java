package net.jeongmin.modid.billboard;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public final class MineFluenceBillboardClient {
	private MineFluenceBillboardClient() {
	}

	public static void register() {
		BlockEntityRendererRegistry.register(
				MineFluenceBillboards.BILLBOARD_BLOCK_ENTITY,
				MineFluenceBillboardBlockEntityRenderer::new
		);
	}
}
