package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluenceTutorialPlayPayload() implements CustomPayload {
	public static final CustomPayload.Id<MineFluenceTutorialPlayPayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "tutorial_play"));
	public static final PacketCodec<RegistryByteBuf, MineFluenceTutorialPlayPayload> CODEC =
			PacketCodec.unit(new MineFluenceTutorialPlayPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
