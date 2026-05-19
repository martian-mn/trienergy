package com.trienergy.content;

import com.trienergy.TriEnergy;
import com.trienergy.network.NetworkRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class TriEnergyWorldState extends SavedData {
    private static final String DATA_NAME = TriEnergy.MOD_ID;

    private final NetworkRegistry registry = new NetworkRegistry();

    public NetworkRegistry networkRegistry() { return registry; }

    public static TriEnergyWorldState get(ServerLevel serverLevel) {
        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(
                        TriEnergyWorldState::new,
                        TriEnergyWorldState::load,
                        DataFixTypes.LEVEL),
                DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        // Network state is persisted at the chunk level (NetworkRegistry.suspendChunk).
        // World-level save data is intentionally empty — this SavedData exists so the
        // NetworkRegistry instance has a stable handle for the lifetime of the level.
        return tag;
    }

    public static TriEnergyWorldState load(CompoundTag tag, HolderLookup.Provider lookup) {
        return new TriEnergyWorldState();
    }
}
