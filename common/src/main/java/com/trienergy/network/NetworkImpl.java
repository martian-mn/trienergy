package com.trienergy.network;

import com.trienergy.api.ConduitType;
import com.trienergy.api.Network;
import com.trienergy.api.NetworkSnapshot;
import com.trienergy.api.NetworkState;
import net.minecraft.core.BlockPos;

import java.util.*;

public final class NetworkImpl implements Network {
    private final UUID id;
    private final ConduitType conduitType;
    private final Map<BlockPos, Node> nodes = new HashMap<>();
    private final Map<BlockPos, Set<BlockPos>> edges = new HashMap<>();
    private final Set<BlockPos> sources = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private final Set<BlockPos> storage = new HashSet<>();
    private NetworkState state = NetworkState.IDLE;
    private long lastTickGameTime = 0L;

    public NetworkImpl(UUID id, ConduitType conduitType) {
        this.id = Objects.requireNonNull(id);
        this.conduitType = Objects.requireNonNull(conduitType);
    }

    @Override public UUID id() { return id; }
    @Override public NetworkState state() { return state; }
    @Override public ConduitType conduitType() { return conduitType; }
    @Override public Set<BlockPos> nodes() { return Collections.unmodifiableSet(nodes.keySet()); }
    @Override public Set<BlockPos> sources() { return Collections.unmodifiableSet(sources); }
    @Override public Set<BlockPos> consumers() { return Collections.unmodifiableSet(consumers); }
    @Override public Set<BlockPos> storage() { return Collections.unmodifiableSet(storage); }

    @Override public NetworkSnapshot snapshot() {
        return new NetworkSnapshot(id, state, conduitType.id(),
                Set.copyOf(nodes.keySet()),
                Set.copyOf(sources), Set.copyOf(consumers), Set.copyOf(storage));
    }

    // Package-private mutators — used by NetworkRegistry, NetworkScheduler, EnergyConduitType.
    Map<BlockPos, Node> nodesMap() { return nodes; }
    Map<BlockPos, Set<BlockPos>> edgesMap() { return edges; }
    Set<BlockPos> sourcesSet() { return sources; }
    Set<BlockPos> consumersSet() { return consumers; }
    Set<BlockPos> storageSet() { return storage; }
    void setState(NetworkState state) { this.state = state; }
    long lastTickGameTime() { return lastTickGameTime; }
    void setLastTickGameTime(long t) { this.lastTickGameTime = t; }
}
