package net.jeongmin.modid.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class DdjRenderer extends GeoEntityRenderer<DdjEntity> {
	public DdjRenderer(EntityRendererFactory.Context context) {
		super(context, new DdjModel());
		this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
	}
}
