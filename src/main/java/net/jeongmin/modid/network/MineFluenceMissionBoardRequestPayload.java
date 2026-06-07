package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluenceMissionBoardRequestPayload() implements CustomPayload {
	public static final CustomPayload.Id<MineFluenceMissionBoardRequestPayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "mission_board_request"));
	public static final PacketCodec<RegistryByteBuf, MineFluenceMissionBoardRequestPayload> CODEC =
			PacketCodec.unit(new MineFluenceMissionBoardRequestPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
