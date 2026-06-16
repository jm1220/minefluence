package net.jeongmin.modid.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.jeongmin.modid.config.MineFluenceBalance;
import net.jeongmin.modid.core.MineFluenceJob;
import net.jeongmin.modid.mission.MineFluenceMissionRoute;
import net.jeongmin.modid.weapon.MineFluenceWeaponTier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public final class MineFluencePlayerData {
	private static final String FOLLOWER_KEY = "follower";
	private static final String SOCIAL_CREDIBILITY_KEY = "socialCredibility";
	private static final String LIE_VALUE_KEY = "lieValue";
	private static final String COMPLETED_MISSION_COUNT_KEY = "completedMissionCount";
	private static final String SELECTED_JOB_KEY = "selectedJob";
	private static final String LAST_COMPLETED_INVASION_INDEX_KEY = "lastCompletedInvasionIndex";
	private static final String PENDING_MISSION_SELECTION_INDEX_KEY = "pendingMissionSelectionIndex";
	private static final String ACTIVE_MISSION_INDEX_KEY = "activeMissionIndex";
	private static final String ACTIVE_MISSION_ROUTE_KEY = "activeMissionRoute";
	private static final String PENDING_POSTING_MISSION_INDEX_KEY = "pendingPostingMissionIndex";
	private static final String PENDING_POSTING_MISSION_ROUTE_KEY = "pendingPostingMissionRoute";
	private static final String ACTIVE_INVASION_INDEX_KEY = "activeInvasionIndex";
	private static final String ACTIVE_INVASION_MOB_UUIDS_KEY = "activeInvasionMobUuids";
	private static final String ACTIVE_INVASION_TOTAL_KEY = "activeInvasionTotal";
	private static final String INVASION_STARTED_AT_TICK_KEY = "invasionStartedAtTick";
	private static final String CURRENT_WEAPON_TIER_KEY = "currentWeaponTier";
	private static final String ACTIVE_MISSION_PROGRESS_KEY = "activeMissionProgress";
	private static final String MISSION_BASELINE_VALUE_KEY = "missionBaselineValue";
	private static final String MISSION_5_CRAFTED_COMPOSTERS_KEY = "mission5CraftedComposters";
	private static final String MISSION_5_PLACED_COMPOSTERS_KEY = "mission5PlacedComposters";
	private static final String SUPPLIES_GRANTED_MISSION_INDEX_KEY = "suppliesGrantedMissionIndex";
	private static final String SUPPLIES_GRANTED_ROUTE_KEY = "suppliesGrantedRoute";
	private static final String ENDING_TRIGGERED_KEY = "endingTriggered";
	private static final String ENDING_ID_KEY = "endingId";
	private static final String EXPOSURE_TRIGGERED_KEY = "exposureTriggered";
	private static final String DEMO_STARTED_KEY = "demoStarted";

	private int follower = MineFluenceBalance.FOLLOWER_DEFAULT;
	private int socialCredibility = MineFluenceBalance.SOCIAL_CREDIBILITY_DEFAULT;
	private int lieValue = MineFluenceBalance.LIE_VALUE_DEFAULT;
	private int completedMissionCount = MineFluenceBalance.COMPLETED_MISSION_DEFAULT;
	private MineFluenceJob selectedJob = MineFluenceJob.NONE;
	private int lastCompletedInvasionIndex = MineFluenceBalance.LAST_COMPLETED_INVASION_DEFAULT;
	private int pendingMissionSelectionIndex;
	private int activeMissionIndex;
	private MineFluenceMissionRoute activeMissionRoute = MineFluenceMissionRoute.NONE;
	private int pendingPostingMissionIndex;
	private MineFluenceMissionRoute pendingPostingMissionRoute = MineFluenceMissionRoute.NONE;
	private int activeInvasionIndex;
	private int activeInvasionTotal;
	private long invasionStartedAtTick;
	private final List<UUID> activeInvasionMobUuids = new ArrayList<>();
	private MineFluenceWeaponTier currentWeaponTier = MineFluenceWeaponTier.WOOD;
	private int activeMissionProgress;
	private int missionBaselineValue;
	private int mission5CraftedComposters;
	private int mission5PlacedComposters;
	private int suppliesGrantedMissionIndex;
	private MineFluenceMissionRoute suppliesGrantedRoute = MineFluenceMissionRoute.NONE;
	private boolean endingTriggered;
	private String endingId = "";
	private boolean exposureTriggered;
	private boolean demoStarted;

	public static MineFluencePlayerData fromNbt(NbtCompound nbt) {
		MineFluencePlayerData data = new MineFluencePlayerData();

		if (nbt.contains(FOLLOWER_KEY, NbtElement.INT_TYPE)) {
			data.setFollower(nbt.getInt(FOLLOWER_KEY));
		}
		if (nbt.contains(SOCIAL_CREDIBILITY_KEY, NbtElement.INT_TYPE)) {
			data.setSocialCredibility(nbt.getInt(SOCIAL_CREDIBILITY_KEY));
		}
		if (nbt.contains(LIE_VALUE_KEY, NbtElement.INT_TYPE)) {
			data.setLieValue(nbt.getInt(LIE_VALUE_KEY));
		}
		if (nbt.contains(COMPLETED_MISSION_COUNT_KEY, NbtElement.INT_TYPE)) {
			data.setCompletedMissionCount(nbt.getInt(COMPLETED_MISSION_COUNT_KEY));
		}
		if (nbt.contains(SELECTED_JOB_KEY, NbtElement.STRING_TYPE)) {
			data.setSelectedJob(MineFluenceJob.fromSerializedName(nbt.getString(SELECTED_JOB_KEY)));
		}
		if (nbt.contains(LAST_COMPLETED_INVASION_INDEX_KEY, NbtElement.INT_TYPE)) {
			data.setLastCompletedInvasionIndex(nbt.getInt(LAST_COMPLETED_INVASION_INDEX_KEY));
		}
		if (nbt.contains(PENDING_MISSION_SELECTION_INDEX_KEY, NbtElement.INT_TYPE)) {
			data.setPendingMissionSelectionIndex(nbt.getInt(PENDING_MISSION_SELECTION_INDEX_KEY));
		}
		if (nbt.contains(ACTIVE_MISSION_INDEX_KEY, NbtElement.INT_TYPE)) {
			data.setActiveMissionIndex(nbt.getInt(ACTIVE_MISSION_INDEX_KEY));
		}
		if (nbt.contains(ACTIVE_MISSION_ROUTE_KEY, NbtElement.STRING_TYPE)) {
			data.setActiveMissionRoute(MineFluenceMissionRoute.fromSerializedName(nbt.getString(ACTIVE_MISSION_ROUTE_KEY)));
		}
		if (nbt.contains(PENDING_POSTING_MISSION_INDEX_KEY, NbtElement.INT_TYPE)) {
			data.setPendingPostingMissionIndex(nbt.getInt(PENDING_POSTING_MISSION_INDEX_KEY));
		}
		if (nbt.contains(PENDING_POSTING_MISSION_ROUTE_KEY, NbtElement.STRING_TYPE)) {
			data.setPendingPostingMissionRoute(MineFluenceMissionRoute.fromSerializedName(nbt.getString(PENDING_POSTING_MISSION_ROUTE_KEY)));
		}
		if (nbt.contains(ACTIVE_INVASION_INDEX_KEY, NbtElement.INT_TYPE)) {
			data.setActiveInvasionIndex(nbt.getInt(ACTIVE_INVASION_INDEX_KEY));
		}
		if (nbt.contains(ACTIVE_INVASION_TOTAL_KEY, NbtElement.INT_TYPE)) {
			data.setActiveInvasionTotal(nbt.getInt(ACTIVE_INVASION_TOTAL_KEY));
		}
		if (nbt.contains(INVASION_STARTED_AT_TICK_KEY, NbtElement.LONG_TYPE)) {
			data.setInvasionStartedAtTick(nbt.getLong(INVASION_STARTED_AT_TICK_KEY));
		}
		if (nbt.contains(ACTIVE_INVASION_MOB_UUIDS_KEY, NbtElement.LIST_TYPE)) {
			NbtList mobUuids = nbt.getList(ACTIVE_INVASION_MOB_UUIDS_KEY, NbtElement.STRING_TYPE);
			List<UUID> parsedUuids = new ArrayList<>();
			for (int index = 0; index < mobUuids.size(); index++) {
				try {
					parsedUuids.add(UUID.fromString(mobUuids.getString(index)));
				} catch (IllegalArgumentException ignored) {
					// Ignore malformed saved UUIDs so one bad mob entry does not break player data.
				}
			}
			data.setActiveInvasionMobUuids(parsedUuids);
		}
		if (nbt.contains(CURRENT_WEAPON_TIER_KEY, NbtElement.STRING_TYPE)) {
			data.setCurrentWeaponTier(MineFluenceWeaponTier.fromSerializedName(nbt.getString(CURRENT_WEAPON_TIER_KEY)));
		}
		if (nbt.contains(ACTIVE_MISSION_PROGRESS_KEY, NbtElement.INT_TYPE)) {
			data.setActiveMissionProgress(nbt.getInt(ACTIVE_MISSION_PROGRESS_KEY));
		}
		if (nbt.contains(MISSION_BASELINE_VALUE_KEY, NbtElement.INT_TYPE)) {
			data.setMissionBaselineValue(nbt.getInt(MISSION_BASELINE_VALUE_KEY));
		}
		if (nbt.contains(MISSION_5_CRAFTED_COMPOSTERS_KEY, NbtElement.INT_TYPE)) {
			data.setMission5CraftedComposters(nbt.getInt(MISSION_5_CRAFTED_COMPOSTERS_KEY));
		}
		if (nbt.contains(MISSION_5_PLACED_COMPOSTERS_KEY, NbtElement.INT_TYPE)) {
			data.setMission5PlacedComposters(nbt.getInt(MISSION_5_PLACED_COMPOSTERS_KEY));
		}
		if (nbt.contains(SUPPLIES_GRANTED_MISSION_INDEX_KEY, NbtElement.INT_TYPE)) {
			data.setSuppliesGrantedMissionIndex(nbt.getInt(SUPPLIES_GRANTED_MISSION_INDEX_KEY));
		}
		if (nbt.contains(SUPPLIES_GRANTED_ROUTE_KEY, NbtElement.STRING_TYPE)) {
			data.setSuppliesGrantedRoute(MineFluenceMissionRoute.fromSerializedName(nbt.getString(SUPPLIES_GRANTED_ROUTE_KEY)));
		}
		if (nbt.contains(ENDING_TRIGGERED_KEY, NbtElement.BYTE_TYPE)) {
			data.setEndingTriggered(nbt.getBoolean(ENDING_TRIGGERED_KEY));
		}
		if (nbt.contains(ENDING_ID_KEY, NbtElement.STRING_TYPE)) {
			data.setEndingId(nbt.getString(ENDING_ID_KEY));
		}
		if (nbt.contains(EXPOSURE_TRIGGERED_KEY, NbtElement.BYTE_TYPE)) {
			data.setExposureTriggered(nbt.getBoolean(EXPOSURE_TRIGGERED_KEY));
		}
		if (nbt.contains(DEMO_STARTED_KEY, NbtElement.BYTE_TYPE)) {
			data.setDemoStarted(nbt.getBoolean(DEMO_STARTED_KEY));
		} else {
			data.setDemoStarted(data.hasLegacyDemoProgress());
		}
		data.normalizeMissionRoutesAfterLoad();

		return data;
	}

	public NbtCompound writeNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putInt(FOLLOWER_KEY, follower);
		nbt.putInt(SOCIAL_CREDIBILITY_KEY, socialCredibility);
		nbt.putInt(LIE_VALUE_KEY, lieValue);
		nbt.putInt(COMPLETED_MISSION_COUNT_KEY, completedMissionCount);
		nbt.putString(SELECTED_JOB_KEY, selectedJob.serializedName());
		nbt.putInt(LAST_COMPLETED_INVASION_INDEX_KEY, lastCompletedInvasionIndex);
		nbt.putInt(PENDING_MISSION_SELECTION_INDEX_KEY, pendingMissionSelectionIndex);
		nbt.putInt(ACTIVE_MISSION_INDEX_KEY, activeMissionIndex);
		nbt.putString(ACTIVE_MISSION_ROUTE_KEY, activeMissionRoute.serializedName());
		nbt.putInt(PENDING_POSTING_MISSION_INDEX_KEY, pendingPostingMissionIndex);
		nbt.putString(PENDING_POSTING_MISSION_ROUTE_KEY, pendingPostingMissionRoute.serializedName());
		nbt.putInt(ACTIVE_INVASION_INDEX_KEY, activeInvasionIndex);
		nbt.putInt(ACTIVE_INVASION_TOTAL_KEY, activeInvasionTotal);
		nbt.putLong(INVASION_STARTED_AT_TICK_KEY, invasionStartedAtTick);

		NbtList mobUuids = new NbtList();
		for (UUID uuid : activeInvasionMobUuids) {
			mobUuids.add(NbtString.of(uuid.toString()));
		}
		nbt.put(ACTIVE_INVASION_MOB_UUIDS_KEY, mobUuids);
		nbt.putString(CURRENT_WEAPON_TIER_KEY, currentWeaponTier.serializedName());
		nbt.putInt(ACTIVE_MISSION_PROGRESS_KEY, activeMissionProgress);
		nbt.putInt(MISSION_BASELINE_VALUE_KEY, missionBaselineValue);
		nbt.putInt(MISSION_5_CRAFTED_COMPOSTERS_KEY, mission5CraftedComposters);
		nbt.putInt(MISSION_5_PLACED_COMPOSTERS_KEY, mission5PlacedComposters);
		nbt.putInt(SUPPLIES_GRANTED_MISSION_INDEX_KEY, suppliesGrantedMissionIndex);
		nbt.putString(SUPPLIES_GRANTED_ROUTE_KEY, suppliesGrantedRoute.serializedName());
		nbt.putBoolean(ENDING_TRIGGERED_KEY, endingTriggered);
		nbt.putString(ENDING_ID_KEY, endingId);
		nbt.putBoolean(EXPOSURE_TRIGGERED_KEY, exposureTriggered);
		nbt.putBoolean(DEMO_STARTED_KEY, demoStarted);
		return nbt;
	}

	public void resetForDemoStart() {
		resetDemoProgress();
		setDemoStarted(true);
	}

	public void resetForTutorialStart() {
		resetDemoProgress();
		setDemoStarted(false);
	}

	private void resetDemoProgress() {
		setFollower(MineFluenceBalance.DEMO_START_FOLLOWER);
		setSocialCredibility(MineFluenceBalance.DEMO_START_SOCIAL_CREDIBILITY);
		setLieValue(MineFluenceBalance.DEMO_START_LIE_VALUE);
		setCompletedMissionCount(MineFluenceBalance.DEMO_START_COMPLETED_MISSIONS);
		setSelectedJob(MineFluenceJob.NONE);
		setLastCompletedInvasionIndex(MineFluenceBalance.DEMO_START_LAST_COMPLETED_INVASION);
		clearMissionFlow();
		clearInvasionState();
		setCurrentWeaponTier(MineFluenceWeaponTier.WOOD);
		clearEndingState();
	}

	public int getFollower() {
		return follower;
	}

	public void setFollower(int follower) {
		this.follower = MineFluenceBalance.clampFollower(follower);
	}

	public void addFollower(int delta) {
		setFollower(follower + delta);
	}

	public int getSocialCredibility() {
		return socialCredibility;
	}

	public void setSocialCredibility(int socialCredibility) {
		this.socialCredibility = MineFluenceBalance.clampSocialCredibility(socialCredibility);
	}

	public void addSocialCredibility(int delta) {
		setSocialCredibility(socialCredibility + delta);
	}

	public int getLieValue() {
		return lieValue;
	}

	public void setLieValue(int lieValue) {
		this.lieValue = MineFluenceBalance.clampLieValue(lieValue);
	}

	public void addLieValue(int delta) {
		setLieValue(lieValue + delta);
	}

	public int getCompletedMissionCount() {
		return completedMissionCount;
	}

	public void setCompletedMissionCount(int completedMissionCount) {
		this.completedMissionCount = MineFluenceBalance.clampCompletedMissionCount(completedMissionCount);
	}

	public void addCompletedMissionCount(int delta) {
		setCompletedMissionCount(completedMissionCount + delta);
	}

	public MineFluenceJob getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(MineFluenceJob selectedJob) {
		this.selectedJob = selectedJob == null ? MineFluenceJob.NONE : selectedJob;
		if (this.selectedJob != MineFluenceJob.NONE) {
			setDemoStarted(true);
		}
	}

	public int getLastCompletedInvasionIndex() {
		return lastCompletedInvasionIndex;
	}

	public void setLastCompletedInvasionIndex(int lastCompletedInvasionIndex) {
		this.lastCompletedInvasionIndex = MineFluenceBalance.clampLastCompletedInvasionIndex(lastCompletedInvasionIndex);
	}

	public int getActiveMissionIndex() {
		return activeMissionIndex;
	}

	public void setActiveMissionIndex(int activeMissionIndex) {
		this.activeMissionIndex = MineFluenceBalance.clampMissionIndex(activeMissionIndex);
	}

	public boolean hasActiveMission() {
		return activeMissionIndex > 0;
	}

	public int getPendingMissionSelectionIndex() {
		return pendingMissionSelectionIndex;
	}

	public void setPendingMissionSelectionIndex(int pendingMissionSelectionIndex) {
		this.pendingMissionSelectionIndex = MineFluenceBalance.clampMissionIndex(pendingMissionSelectionIndex);
	}

	public boolean hasPendingMissionSelection() {
		return pendingMissionSelectionIndex > 0;
	}

	public MineFluenceMissionRoute getActiveMissionRoute() {
		return activeMissionRoute;
	}

	public void setActiveMissionRoute(MineFluenceMissionRoute activeMissionRoute) {
		this.activeMissionRoute = activeMissionRoute == null ? MineFluenceMissionRoute.NONE : activeMissionRoute;
	}

	public int getPendingPostingMissionIndex() {
		return pendingPostingMissionIndex;
	}

	public void setPendingPostingMissionIndex(int pendingPostingMissionIndex) {
		this.pendingPostingMissionIndex = MineFluenceBalance.clampMissionIndex(pendingPostingMissionIndex);
	}

	public boolean isWaitingForPostingChoice() {
		return pendingPostingMissionIndex > 0;
	}

	public void startMission(int missionIndex) {
		startMission(missionIndex, MineFluenceMissionRoute.GOOD);
	}

	public void startMission(int missionIndex, MineFluenceMissionRoute route) {
		setPendingMissionSelectionIndex(0);
		setActiveMissionIndex(missionIndex);
		setActiveMissionRoute(route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route);
		setPendingPostingMissionIndex(0);
		setPendingPostingMissionRoute(MineFluenceMissionRoute.NONE);
		setActiveMissionProgress(0);
		setMissionBaselineValue(0);
		clearMission5ComposterProgress();
		clearMissionSuppliesGrant();
	}

	public void markActiveMissionReadyToPost() {
		setPendingPostingMissionIndex(activeMissionIndex);
		setPendingPostingMissionRoute(activeMissionRoute == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : activeMissionRoute);
		setActiveMissionIndex(0);
		setActiveMissionRoute(MineFluenceMissionRoute.NONE);
	}

	public void clearMissionFlow() {
		setPendingMissionSelectionIndex(0);
		setActiveMissionIndex(0);
		setActiveMissionRoute(MineFluenceMissionRoute.NONE);
		setPendingPostingMissionIndex(0);
		setPendingPostingMissionRoute(MineFluenceMissionRoute.NONE);
		setActiveMissionProgress(0);
		setMissionBaselineValue(0);
		clearMission5ComposterProgress();
		clearMissionSuppliesGrant();
	}

	public MineFluenceMissionRoute getPendingPostingMissionRoute() {
		return pendingPostingMissionRoute;
	}

	public void setPendingPostingMissionRoute(MineFluenceMissionRoute pendingPostingMissionRoute) {
		this.pendingPostingMissionRoute = pendingPostingMissionRoute == null ? MineFluenceMissionRoute.NONE : pendingPostingMissionRoute;
	}

	public int getActiveInvasionIndex() {
		return activeInvasionIndex;
	}

	public void setActiveInvasionIndex(int activeInvasionIndex) {
		this.activeInvasionIndex = MineFluenceBalance.clampActiveInvasionIndex(activeInvasionIndex);
	}

	public boolean hasActiveInvasion() {
		return activeInvasionIndex > 0;
	}

	public long getInvasionStartedAtTick() {
		return invasionStartedAtTick;
	}

	public void setInvasionStartedAtTick(long invasionStartedAtTick) {
		this.invasionStartedAtTick = Math.max(0L, invasionStartedAtTick);
	}

	public List<UUID> getActiveInvasionMobUuids() {
		return Collections.unmodifiableList(activeInvasionMobUuids);
	}

	public int getTrackedInvasionMobCount() {
		return activeInvasionMobUuids.size();
	}

	public int getActiveInvasionTotal() {
		return activeInvasionTotal;
	}

	public void setActiveInvasionTotal(int activeInvasionTotal) {
		this.activeInvasionTotal = Math.max(0, activeInvasionTotal);
	}

	public void setActiveInvasionMobUuids(Collection<UUID> mobUuids) {
		activeInvasionMobUuids.clear();
		activeInvasionMobUuids.addAll(mobUuids);
	}

	public void startInvasion(int invasionIndex, Collection<UUID> mobUuids, long startedAtTick) {
		setActiveInvasionIndex(invasionIndex);
		setActiveInvasionMobUuids(mobUuids);
		setActiveInvasionTotal(mobUuids.size());
		setInvasionStartedAtTick(startedAtTick);
	}

	public void clearInvasionState() {
		setActiveInvasionIndex(0);
		activeInvasionMobUuids.clear();
		setActiveInvasionTotal(0);
		setInvasionStartedAtTick(0L);
	}

	public MineFluenceWeaponTier getCurrentWeaponTier() {
		return currentWeaponTier;
	}

	public void setCurrentWeaponTier(MineFluenceWeaponTier currentWeaponTier) {
		this.currentWeaponTier = currentWeaponTier == null ? MineFluenceWeaponTier.WOOD : currentWeaponTier;
	}

	public int getActiveMissionProgress() {
		return activeMissionProgress;
	}

	public void setActiveMissionProgress(int activeMissionProgress) {
		this.activeMissionProgress = MineFluenceBalance.clampMissionProgress(activeMissionProgress);
	}

	public void addActiveMissionProgress(int delta) {
		setActiveMissionProgress(activeMissionProgress + delta);
	}

	public int getMissionBaselineValue() {
		return missionBaselineValue;
	}

	public void setMissionBaselineValue(int missionBaselineValue) {
		this.missionBaselineValue = Math.max(0, missionBaselineValue);
	}

	public int getMission5CraftedComposters() {
		return mission5CraftedComposters;
	}

	public void setMission5CraftedComposters(int mission5CraftedComposters) {
		this.mission5CraftedComposters = Math.max(0, mission5CraftedComposters);
	}

	public int getMission5PlacedComposters() {
		return mission5PlacedComposters;
	}

	public void setMission5PlacedComposters(int mission5PlacedComposters) {
		this.mission5PlacedComposters = Math.max(0, mission5PlacedComposters);
	}

	public void clearMission5ComposterProgress() {
		setMission5CraftedComposters(0);
		setMission5PlacedComposters(0);
	}

	public int getSuppliesGrantedMissionIndex() {
		return suppliesGrantedMissionIndex;
	}

	public void setSuppliesGrantedMissionIndex(int suppliesGrantedMissionIndex) {
		this.suppliesGrantedMissionIndex = MineFluenceBalance.clampMissionIndex(suppliesGrantedMissionIndex);
	}

	public MineFluenceMissionRoute getSuppliesGrantedRoute() {
		return suppliesGrantedRoute;
	}

	public void setSuppliesGrantedRoute(MineFluenceMissionRoute suppliesGrantedRoute) {
		this.suppliesGrantedRoute = suppliesGrantedRoute == null ? MineFluenceMissionRoute.NONE : suppliesGrantedRoute;
	}

	public boolean hasMissionSuppliesGranted(int missionIndex, MineFluenceMissionRoute route) {
		MineFluenceMissionRoute resolvedRoute = route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route;
		return suppliesGrantedMissionIndex == missionIndex && suppliesGrantedRoute == resolvedRoute;
	}

	public void markMissionSuppliesGranted(int missionIndex, MineFluenceMissionRoute route) {
		setSuppliesGrantedMissionIndex(missionIndex);
		setSuppliesGrantedRoute(route == MineFluenceMissionRoute.NONE ? MineFluenceMissionRoute.GOOD : route);
	}

	public void clearMissionSuppliesGrant() {
		setSuppliesGrantedMissionIndex(0);
		setSuppliesGrantedRoute(MineFluenceMissionRoute.NONE);
	}

	public boolean isEndingTriggered() {
		return endingTriggered;
	}

	public void setEndingTriggered(boolean endingTriggered) {
		this.endingTriggered = endingTriggered;
	}

	public String getEndingId() {
		return endingId;
	}

	public void setEndingId(String endingId) {
		this.endingId = endingId == null ? "" : endingId;
	}

	public void clearEndingState() {
		setEndingTriggered(false);
		setEndingId("");
		setExposureTriggered(false);
	}

	public boolean isExposureTriggered() {
		return exposureTriggered;
	}

	public void setExposureTriggered(boolean exposureTriggered) {
		this.exposureTriggered = exposureTriggered;
	}

	public boolean isDemoStarted() {
		return demoStarted;
	}

	public void setDemoStarted(boolean demoStarted) {
		this.demoStarted = demoStarted;
	}

	private boolean hasLegacyDemoProgress() {
		return selectedJob != MineFluenceJob.NONE
				|| follower != MineFluenceBalance.FOLLOWER_DEFAULT
				|| socialCredibility != MineFluenceBalance.SOCIAL_CREDIBILITY_DEFAULT
				|| lieValue != MineFluenceBalance.LIE_VALUE_DEFAULT
				|| completedMissionCount != MineFluenceBalance.COMPLETED_MISSION_DEFAULT
				|| lastCompletedInvasionIndex != MineFluenceBalance.LAST_COMPLETED_INVASION_DEFAULT
				|| hasPendingMissionSelection()
				|| hasActiveMission()
				|| isWaitingForPostingChoice()
				|| hasActiveInvasion()
				|| endingTriggered
				|| exposureTriggered;
	}

	private void normalizeMissionRoutesAfterLoad() {
		if (hasActiveMission() && activeMissionRoute == MineFluenceMissionRoute.NONE) {
			setActiveMissionRoute(MineFluenceMissionRoute.GOOD);
		}
		if (isWaitingForPostingChoice() && pendingPostingMissionRoute == MineFluenceMissionRoute.NONE) {
			setPendingPostingMissionRoute(MineFluenceMissionRoute.GOOD);
		}
		if (suppliesGrantedMissionIndex > 0 && suppliesGrantedRoute == MineFluenceMissionRoute.NONE) {
			setSuppliesGrantedRoute(MineFluenceMissionRoute.GOOD);
		}
	}
}
