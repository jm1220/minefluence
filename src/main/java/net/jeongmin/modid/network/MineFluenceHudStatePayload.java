package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluenceHudStatePayload(
		int follower,
		int socialCredibility,
		int lieValue,
		int completedMissionCount,
		String selectedJob,
		int activeMissionIndex,
		String activeMissionRoute,
		String activeMissionTitle,
		String activeMissionObjective,
		int activeMissionProgress,
		int activeMissionTarget,
		boolean waitingForPosting,
		int pendingPostingMissionIndex,
		String pendingPostingRoute,
		int activeInvasionIndex,
		int invasionRemaining,
		int invasionTotal,
		String endingName
) implements CustomPayload {
	private static final int MAX_JOB_LENGTH = 32;
	private static final int MAX_ROUTE_LENGTH = 16;
	private static final int MAX_TITLE_LENGTH = 128;
	private static final int MAX_OBJECTIVE_LENGTH = 512;
	private static final int MAX_ENDING_LENGTH = 128;

	public static final CustomPayload.Id<MineFluenceHudStatePayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "hud_state"));
	public static final PacketCodec<RegistryByteBuf, MineFluenceHudStatePayload> CODEC =
			PacketCodec.ofStatic(MineFluenceHudStatePayload::write, MineFluenceHudStatePayload::new);

	public MineFluenceHudStatePayload(RegistryByteBuf buf) {
		this(
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readString(MAX_JOB_LENGTH),
				buf.readVarInt(),
				buf.readString(MAX_ROUTE_LENGTH),
				buf.readString(MAX_TITLE_LENGTH),
				buf.readString(MAX_OBJECTIVE_LENGTH),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readVarInt(),
				buf.readString(MAX_ROUTE_LENGTH),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readString(MAX_ENDING_LENGTH)
		);
	}

	private static void write(RegistryByteBuf buf, MineFluenceHudStatePayload payload) {
		buf.writeVarInt(payload.follower);
		buf.writeVarInt(payload.socialCredibility);
		buf.writeVarInt(payload.lieValue);
		buf.writeVarInt(payload.completedMissionCount);
		buf.writeString(safeString(payload.selectedJob, MAX_JOB_LENGTH), MAX_JOB_LENGTH);
		buf.writeVarInt(payload.activeMissionIndex);
		buf.writeString(safeString(payload.activeMissionRoute, MAX_ROUTE_LENGTH), MAX_ROUTE_LENGTH);
		buf.writeString(safeString(payload.activeMissionTitle, MAX_TITLE_LENGTH), MAX_TITLE_LENGTH);
		buf.writeString(safeString(payload.activeMissionObjective, MAX_OBJECTIVE_LENGTH), MAX_OBJECTIVE_LENGTH);
		buf.writeVarInt(payload.activeMissionProgress);
		buf.writeVarInt(payload.activeMissionTarget);
		buf.writeBoolean(payload.waitingForPosting);
		buf.writeVarInt(payload.pendingPostingMissionIndex);
		buf.writeString(safeString(payload.pendingPostingRoute, MAX_ROUTE_LENGTH), MAX_ROUTE_LENGTH);
		buf.writeVarInt(payload.activeInvasionIndex);
		buf.writeVarInt(payload.invasionRemaining);
		buf.writeVarInt(payload.invasionTotal);
		buf.writeString(safeString(payload.endingName, MAX_ENDING_LENGTH), MAX_ENDING_LENGTH);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static String safeString(String value, int maxLength) {
		if (value == null) {
			return "";
		}
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
