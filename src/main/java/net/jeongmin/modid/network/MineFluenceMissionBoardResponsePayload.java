package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.mission.MineFluenceMissionBoardState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluenceMissionBoardResponsePayload(
		int missionIndex,
		boolean canChoose,
		boolean showOptions,
		String message,
		MissionOptionData goodOption,
		MissionOptionData badOption
) implements CustomPayload {
	private static final int MAX_MESSAGE_LENGTH = 512;
	private static final int MAX_ROUTE_LENGTH = 16;
	private static final int MAX_TITLE_LENGTH = 128;
	private static final int MAX_OBJECTIVE_LENGTH = 512;

	public static final CustomPayload.Id<MineFluenceMissionBoardResponsePayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "mission_board_response"));
	public static final PacketCodec<RegistryByteBuf, MineFluenceMissionBoardResponsePayload> CODEC =
			PacketCodec.ofStatic(MineFluenceMissionBoardResponsePayload::write, MineFluenceMissionBoardResponsePayload::new);

	public MineFluenceMissionBoardResponsePayload(MineFluenceMissionBoardState state) {
		this(
				state.missionIndex(),
				state.canChoose(),
				state.showOptions(),
				safeString(state.message()),
				new MissionOptionData(state.goodOption()),
				new MissionOptionData(state.badOption())
		);
	}

	public MineFluenceMissionBoardResponsePayload(RegistryByteBuf buf) {
		this(
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readString(MAX_MESSAGE_LENGTH),
				MissionOptionData.read(buf),
				MissionOptionData.read(buf)
		);
	}

	private static void write(RegistryByteBuf buf, MineFluenceMissionBoardResponsePayload payload) {
		buf.writeVarInt(payload.missionIndex);
		buf.writeBoolean(payload.canChoose);
		buf.writeBoolean(payload.showOptions);
		buf.writeString(safeString(payload.message), MAX_MESSAGE_LENGTH);
		payload.goodOption.write(buf);
		payload.badOption.write(buf);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static String safeString(String value) {
		return value == null ? "" : value;
	}

	public record MissionOptionData(
			String route,
			String title,
			String objectiveText,
			int followerReward,
			int socialCredibilityReward
	) {
		public MissionOptionData(MineFluenceMissionBoardState.MineFluenceMissionOption option) {
			this(
					safeString(option.route()),
					safeString(option.title()),
					safeString(option.objectiveText()),
					option.followerReward(),
					option.socialCredibilityReward()
			);
		}

		private static MissionOptionData read(RegistryByteBuf buf) {
			return new MissionOptionData(
					buf.readString(MAX_ROUTE_LENGTH),
					buf.readString(MAX_TITLE_LENGTH),
					buf.readString(MAX_OBJECTIVE_LENGTH),
					buf.readVarInt(),
					buf.readVarInt()
			);
		}

		private void write(RegistryByteBuf buf) {
			buf.writeString(safeString(route), MAX_ROUTE_LENGTH);
			buf.writeString(safeString(title), MAX_TITLE_LENGTH);
			buf.writeString(safeString(objectiveText), MAX_OBJECTIVE_LENGTH);
			buf.writeVarInt(followerReward);
			buf.writeVarInt(socialCredibilityReward);
		}

		public String rewardText() {
			return "Follower " + signed(followerReward) + ", Social " + signed(socialCredibilityReward);
		}

		private static String signed(int value) {
			if (value >= 0) {
				return "+" + value;
			}
			return Integer.toString(value);
		}
	}
}
