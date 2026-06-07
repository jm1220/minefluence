package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluencePostingChoicePayload(boolean exaggerated) implements CustomPayload {
	public static final CustomPayload.Id<MineFluencePostingChoicePayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "posting_choice"));
	public static final PacketCodec<RegistryByteBuf, MineFluencePostingChoicePayload> CODEC =
			PacketCodec.ofStatic(MineFluencePostingChoicePayload::write, MineFluencePostingChoicePayload::new);

	public MineFluencePostingChoicePayload(RegistryByteBuf buf) {
		this(buf.readBoolean());
	}

	private static void write(RegistryByteBuf buf, MineFluencePostingChoicePayload payload) {
		buf.writeBoolean(payload.exaggerated);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
