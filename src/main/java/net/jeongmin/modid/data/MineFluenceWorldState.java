package net.jeongmin.modid.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.jeongmin.modid.MineFluence;
import net.jeongmin.modid.area.MineFluenceArea;
import net.jeongmin.modid.area.MineFluenceAreaType;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

public final class MineFluenceWorldState extends PersistentState {
	private static final String STATE_KEY = MineFluence.MOD_ID + "_player_data";
	private static final String PLAYERS_KEY = "players";
	private static final String AREAS_KEY = "areas";

	private static final Type<MineFluenceWorldState> TYPE = new Type<>(
			MineFluenceWorldState::new,
			MineFluenceWorldState::fromNbt,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Map<UUID, MineFluencePlayerData> players = new HashMap<>();
	private final Map<MineFluenceAreaType, MineFluenceArea> areas = new HashMap<>();

	public static MineFluenceWorldState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, STATE_KEY);
	}

	private static MineFluenceWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		MineFluenceWorldState state = new MineFluenceWorldState();

		if (!nbt.contains(PLAYERS_KEY, NbtElement.COMPOUND_TYPE)) {
			loadAreas(nbt, state);
			return state;
		}

		NbtCompound playersNbt = nbt.getCompound(PLAYERS_KEY);
		for (String uuidText : playersNbt.getKeys()) {
			if (!playersNbt.contains(uuidText, NbtElement.COMPOUND_TYPE)) {
				continue;
			}

			try {
				UUID uuid = UUID.fromString(uuidText);
				state.players.put(uuid, MineFluencePlayerData.fromNbt(playersNbt.getCompound(uuidText)));
			} catch (IllegalArgumentException ignored) {
				// Ignore malformed saved keys so one bad entry does not break the world state.
			}
		}

		loadAreas(nbt, state);
		return state;
	}

	private static void loadAreas(NbtCompound nbt, MineFluenceWorldState state) {
		if (!nbt.contains(AREAS_KEY, NbtElement.COMPOUND_TYPE)) {
			return;
		}

		NbtCompound areasNbt = nbt.getCompound(AREAS_KEY);
		for (String key : areasNbt.getKeys()) {
			if (!areasNbt.contains(key, NbtElement.COMPOUND_TYPE)) {
				continue;
			}

			MineFluenceArea area = MineFluenceArea.fromNbt(areasNbt.getCompound(key));
			if (area != null) {
				state.areas.put(area.type(), area);
			}
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		NbtCompound playersNbt = new NbtCompound();
		players.forEach((uuid, data) -> playersNbt.put(uuid.toString(), data.writeNbt()));
		nbt.put(PLAYERS_KEY, playersNbt);

		NbtCompound areasNbt = new NbtCompound();
		areas.forEach((type, area) -> areasNbt.put(type.serializedName(), area.writeNbt()));
		nbt.put(AREAS_KEY, areasNbt);
		return nbt;
	}

	public MineFluencePlayerData getPlayerData(ServerPlayerEntity player) {
		return getPlayerData(player.getUuid());
	}

	public MineFluencePlayerData getPlayerData(UUID uuid) {
		MineFluencePlayerData data = players.get(uuid);
		if (data == null) {
			data = new MineFluencePlayerData();
			players.put(uuid, data);
			markDirty();
		}
		return data;
	}

	public MineFluencePlayerData resetPlayerData(ServerPlayerEntity player) {
		MineFluencePlayerData data = new MineFluencePlayerData();
		players.put(player.getUuid(), data);
		markDirty();
		return data;
	}

	public MineFluencePlayerData updatePlayerData(ServerPlayerEntity player, Consumer<MineFluencePlayerData> updater) {
		MineFluencePlayerData data = getPlayerData(player);
		updater.accept(data);
		markDirty();
		return data;
	}

	public void setArea(MineFluenceArea area) {
		areas.put(area.type(), area);
		markDirty();
	}

	public MineFluenceArea getArea(MineFluenceAreaType type) {
		return areas.get(type);
	}

	public Map<MineFluenceAreaType, MineFluenceArea> getAreas() {
		return Map.copyOf(areas);
	}

	public boolean clearArea(MineFluenceAreaType type) {
		boolean removed = areas.remove(type) != null;
		if (removed) {
			markDirty();
		}
		return removed;
	}

	public int getConfiguredAreaCount() {
		return areas.size();
	}
}
