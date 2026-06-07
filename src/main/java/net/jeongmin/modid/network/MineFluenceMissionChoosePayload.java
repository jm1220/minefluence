package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluenceMissionChoosePayload(String route) implements CustomPayload {
	private static final int MAX_ROUTE_LENGTH = 16;

	public static final CustomPayload.Id<MineFluenceMissionChoosePayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "mission_choose"));
	public static final PacketCodec<RegistryByteBuf, MineFluenceMissionChoosePayload> CODEC =
			PacketCodec.ofStatic(MineFluenceMissionChoosePayload::write, MineFluenceMissionChoosePayload::new);

	public MineFluenceMissionChoosePayload(MineFluenceMissionRoute route) {
		this(route == null ? MineFluenceMissionRoute.GOOD.serializedName() : route.serializedName());
	}

	public MineFluenceMissionChoosePayload(RegistryByteBuf buf) {
		this(buf.readString(MAX_ROUTE_LENGTH));
	}

	private static void write(RegistryByteBuf buf, MineFluenceMissionChoosePayload payload) {
		buf.writeString(payload.route == null ? MineFluenceMissionRoute.GOOD.serializedName() : payload.route, MAX_ROUTE_LENGTH);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
