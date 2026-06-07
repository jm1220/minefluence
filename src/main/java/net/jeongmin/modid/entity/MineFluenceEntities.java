package net.jeongmin.modid.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.jeongmin.modid.MineFluence;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class MineFluenceEntities {
	public static final Identifier DDJ_ID = Identifier.of(MineFluence.MOD_ID, "ddj");

	public static final EntityType<DdjEntity> DDJ = Registry.register(
			Registries.ENTITY_TYPE,
			DDJ_ID,
			EntityType.Builder.create(DdjEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.9F, 1.9F)
					.maxTrackingRange(8)
					.trackingTickInterval(3)
					.build(DDJ_ID.toString())
	);

	private MineFluenceEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(DDJ, DdjEntity.createAttributes());
		MineFluence.LOGGER.info("Registered MineFluence entities.");
	}
}
