package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluencePlayEndingVideoPayload() implements CustomPayload {
	public static final CustomPayload.Id<MineFluencePlayEndingVideoPayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "play_ending_video"));
	public static final PacketCodec<RegistryByteBuf, MineFluencePlayEndingVideoPayload> CODEC =
			PacketCodec.unit(new MineFluencePlayEndingVideoPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
