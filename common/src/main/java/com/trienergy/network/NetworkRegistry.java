package com.trienergy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public final class NetworkRegistry {
    private final Map<UUID, NetworkImpl> byId = new HashMap<>();
    private final Map<BlockPos, NetworkImpl> byPosition = new HashMap<>();
    private final Map<ChunkPos, Set<UUID>> byChunk = new HashMap<>();

    public int networkCount() { return byId.size(); }
    public NetworkImpl networkAt(BlockPos pos) { return byPosition.get(pos); }
    public Collection<NetworkImpl> allNetworks() {
        return Collections.unmodifiableCollection(byId.values());
    }

    // place / break / merge / split methods come in T4 and T5.
}
