package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluencePhoneStateRequestPayload() implements CustomPayload {
	public static final CustomPayload.Id<MineFluencePhoneStateRequestPayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "phone_state_request"));
	public static final PacketCodec<RegistryByteBuf, MineFluencePhoneStateRequestPayload> CODEC =
			PacketCodec.unit(new MineFluencePhoneStateRequestPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
