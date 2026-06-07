package net.jeongmin.modid.entity;

import net.jeongmin.modid.MineFluence;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DdjModel extends GeoModel<DdjEntity> {
	@Override
	public Identifier getModelResource(DdjEntity entity) {
		return Identifier.of(MineFluence.MOD_ID, "geo/ddj.geo.json");
	}

	@Override
	public Identifier getTextureResource(DdjEntity entity) {
		return Identifier.of(MineFluence.MOD_ID, "textures/entity/ddj.png");
	}

	@Override
	public Identifier getAnimationResource(DdjEntity entity) {
		return Identifier.of(MineFluence.MOD_ID, "animations/ddj.animation.json");
	}
}
