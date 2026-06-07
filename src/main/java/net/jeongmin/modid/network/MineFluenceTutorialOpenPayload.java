package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluenceTutorialOpenPayload() implements CustomPayload {
	public static final CustomPayload.Id<MineFluenceTutorialOpenPayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "tutorial_open"));
	public static final PacketCodec<RegistryByteBuf, MineFluenceTutorialOpenPayload> CODEC =
			PacketCodec.unit(new MineFluenceTutorialOpenPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
