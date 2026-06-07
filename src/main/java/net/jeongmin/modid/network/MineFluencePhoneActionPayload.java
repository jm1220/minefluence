package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluencePhoneActionPayload(String action) implements CustomPayload {
	private static final int MAX_ACTION_LENGTH = 48;

	public static final CustomPayload.Id<MineFluencePhoneActionPayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "phone_action"));
	public static final PacketCodec<RegistryByteBuf, MineFluencePhoneActionPayload> CODEC =
			PacketCodec.ofStatic(MineFluencePhoneActionPayload::write, MineFluencePhoneActionPayload::new);

	public MineFluencePhoneActionPayload(MineFluencePhoneAction action) {
		this(action == null ? "" : action.serializedName());
	}

	public MineFluencePhoneActionPayload(RegistryByteBuf buf) {
		this(buf.readString(MAX_ACTION_LENGTH));
	}

	private static void write(RegistryByteBuf buf, MineFluencePhoneActionPayload payload) {
		buf.writeString(payload.action == null ? "" : payload.action, MAX_ACTION_LENGTH);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
