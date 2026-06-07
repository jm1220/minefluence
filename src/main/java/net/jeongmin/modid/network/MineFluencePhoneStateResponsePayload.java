package net.jeongmin.modid.network;

import net.jeongmin.modid.MineFluence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineFluencePhoneStateResponsePayload(
		String state,
		int missionIndex,
		String route,
		String title,
		String objectiveText,
		int currentProgress,
		int targetProgress,
		int normalFollowerReward,
		int normalSocialCredibilityReward,
		int normalLieValueIncrease,
		int exaggeratedFollowerReward,
		int exaggeratedSocialCredibilityReward,
		int exaggeratedLieValueIncrease,
		String message,
		int follower,
		int socialCredibility,
		int lieValue,
		int completedMissionCount,
		String selectedJob,
		int pendingMissionSelectionIndex,
		int activeInvasionIndex,
		int invasionRemaining,
		int invasionTotal,
		boolean endingTriggered,
		String endingName,
		String weaponTier
) implements CustomPayload {
	private static final int MAX_STATE_LENGTH = 24;
	private static final int MAX_ROUTE_LENGTH = 16;
	private static final int MAX_TITLE_LENGTH = 128;
	private static final int MAX_OBJECTIVE_LENGTH = 512;
	private static final int MAX_MESSAGE_LENGTH = 512;
	private static final int MAX_JOB_LENGTH = 32;
	private static final int MAX_ENDING_LENGTH = 128;
	private static final int MAX_WEAPON_LENGTH = 32;

	public static final String STATE_MISSION_BOARD = "MISSION_BOARD";
	public static final String STATE_POSTING = "POSTING";
	public static final String STATE_STATUS = "STATUS";
	public static final String STATE_GUIDANCE = "GUIDANCE";

	public static final CustomPayload.Id<MineFluencePhoneStateResponsePayload> ID =
			new CustomPayload.Id<>(Identifier.of(MineFluence.MOD_ID, "phone_state_response"));
	public static final PacketCodec<RegistryByteBuf, MineFluencePhoneStateResponsePayload> CODEC =
			PacketCodec.ofStatic(MineFluencePhoneStateResponsePayload::write, MineFluencePhoneStateResponsePayload::new);

	public MineFluencePhoneStateResponsePayload(RegistryByteBuf buf) {
		this(
				buf.readString(MAX_STATE_LENGTH),
				buf.readVarInt(),
				buf.readString(MAX_ROUTE_LENGTH),
				buf.readString(MAX_TITLE_LENGTH),
				buf.readString(MAX_OBJECTIVE_LENGTH),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readString(MAX_MESSAGE_LENGTH),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readString(MAX_JOB_LENGTH),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readString(MAX_ENDING_LENGTH),
				buf.readString(MAX_WEAPON_LENGTH)
		);
	}

	public MineFluencePhoneStateResponsePayload(
			String state,
			int missionIndex,
			String route,
			String title,
			String objectiveText,
			int currentProgress,
			int targetProgress,
			int normalFollowerReward,
			int normalSocialCredibilityReward,
			int normalLieValueIncrease,
			int exaggeratedFollowerReward,
			int exaggeratedSocialCredibilityReward,
			int exaggeratedLieValueIncrease,
			String message
	) {
		this(
				state,
				missionIndex,
				route,
				title,
				objectiveText,
				currentProgress,
				targetProgress,
				normalFollowerReward,
				normalSocialCredibilityReward,
				normalLieValueIncrease,
				exaggeratedFollowerReward,
				exaggeratedSocialCredibilityReward,
				exaggeratedLieValueIncrease,
				message,
				0,
				0,
				0,
				0,
				"",
				0,
				0,
				0,
				0,
				false,
				"",
				""
		);
	}

	private static void write(RegistryByteBuf buf, MineFluencePhoneStateResponsePayload payload) {
		buf.writeString(safeString(payload.state), MAX_STATE_LENGTH);
		buf.writeVarInt(payload.missionIndex);
		buf.writeString(safeString(payload.route), MAX_ROUTE_LENGTH);
		buf.writeString(safeString(payload.title), MAX_TITLE_LENGTH);
		buf.writeString(safeString(payload.objectiveText), MAX_OBJECTIVE_LENGTH);
		buf.writeVarInt(payload.currentProgress);
		buf.writeVarInt(payload.targetProgress);
		buf.writeVarInt(payload.normalFollowerReward);
		buf.writeVarInt(payload.normalSocialCredibilityReward);
		buf.writeVarInt(payload.normalLieValueIncrease);
		buf.writeVarInt(payload.exaggeratedFollowerReward);
		buf.writeVarInt(payload.exaggeratedSocialCredibilityReward);
		buf.writeVarInt(payload.exaggeratedLieValueIncrease);
		buf.writeString(safeString(payload.message), MAX_MESSAGE_LENGTH);
		buf.writeVarInt(payload.follower);
		buf.writeVarInt(payload.socialCredibility);
		buf.writeVarInt(payload.lieValue);
		buf.writeVarInt(payload.completedMissionCount);
		buf.writeString(safeString(payload.selectedJob), MAX_JOB_LENGTH);
		buf.writeVarInt(payload.pendingMissionSelectionIndex);
		buf.writeVarInt(payload.activeInvasionIndex);
		buf.writeVarInt(payload.invasionRemaining);
		buf.writeVarInt(payload.invasionTotal);
		buf.writeBoolean(payload.endingTriggered);
		buf.writeString(safeString(payload.endingName), MAX_ENDING_LENGTH);
		buf.writeString(safeString(payload.weaponTier), MAX_WEAPON_LENGTH);
	}

	public static MineFluencePhoneStateResponsePayload guidance(String message) {
		return new MineFluencePhoneStateResponsePayload(
				STATE_GUIDANCE,
				0,
				"",
				"",
				"",
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				message
		);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static String safeString(String value) {
		return value == null ? "" : value;
	}
}
