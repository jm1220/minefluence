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
		boolean exposureTriggered,
		String endingName,
		boolean endingVideoAvailable,
		String weaponTier,
		int supportAllyCount,
		String requiredAreaName
) implements CustomPayload {
	private static final int MAX_STATE_LENGTH = 24;
	private static final int MAX_ROUTE_LENGTH = 16;
	private static final int MAX_TITLE_LENGTH = 128;
	private static final int MAX_OBJECTIVE_LENGTH = 512;
	private static final int MAX_MESSAGE_LENGTH = 512;
	private static final int MAX_JOB_LENGTH = 32;
	private static final int MAX_ENDING_LENGTH = 128;
	private static final int MAX_WEAPON_LENGTH = 32;
	private static final int MAX_AREA_LENGTH = 64;

	public static final String STATE_NOT_STARTED = "NOT_STARTED";
	public static final String STATE_CHOOSE_JOB = "CHOOSE_JOB";
	public static final String STATE_READY = "READY";
	public static final String STATE_MISSION_CHOICE = "MISSION_CHOICE";
	public static final String STATE_MISSION_ACTIVE = "MISSION_ACTIVE";
	public static final String STATE_READY_TO_UPLOAD = "READY_TO_UPLOAD";
	public static final String STATE_INVASION = "INVASION";
	public static final String STATE_ENDING = "ENDING";
	public static final String STATE_EXPOSED = "EXPOSED";

	@Deprecated
	public static final String STATE_MISSION_BOARD = STATE_MISSION_CHOICE;
	@Deprecated
	public static final String STATE_POSTING = STATE_READY_TO_UPLOAD;
	@Deprecated
	public static final String STATE_STATUS = STATE_MISSION_ACTIVE;
	@Deprecated
	public static final String STATE_GUIDANCE = STATE_READY;

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
				buf.readBoolean(),
				buf.readString(MAX_ENDING_LENGTH),
				buf.readBoolean(),
				buf.readString(MAX_WEAPON_LENGTH),
				buf.readVarInt(),
				buf.readString(MAX_AREA_LENGTH)
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
				false,
				"",
				false,
				"",
				0,
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
		buf.writeBoolean(payload.exposureTriggered);
		buf.writeString(safeString(payload.endingName), MAX_ENDING_LENGTH);
		buf.writeBoolean(payload.endingVideoAvailable);
		buf.writeString(safeString(payload.weaponTier), MAX_WEAPON_LENGTH);
		buf.writeVarInt(payload.supportAllyCount);
		buf.writeString(safeString(payload.requiredAreaName), MAX_AREA_LENGTH);
	}

	public static MineFluencePhoneStateResponsePayload guidance(String message) {
		return new MineFluencePhoneStateResponsePayload(
				STATE_READY,
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
