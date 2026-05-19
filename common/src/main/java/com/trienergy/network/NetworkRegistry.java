package com.trienergy.network;

import com.trienergy.api.ConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.*;

public final class NetworkRegistry {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Map<UUID, NetworkImpl> byId = new HashMap<>();
    private final Map<BlockPos, NetworkImpl> byPosition = new HashMap<>();
    /**
     * Key = packed chunk coordinates: {@code ((long)(x >> 4) << 32) | ((z >> 4) & 0xFFFFFFFFL)}.
     * Avoids loading {@code ChunkPos} (and the MC bootstrap chain it triggers) in unit tests.
     * T10 will read this map when adding chunk-suspend support.
     */
    private final Map<Long, Set<UUID>> byChunk = new HashMap<>();

    /** Pack chunk coordinates as a long without touching ChunkPos. */
    private static long chunkKey(BlockPos pos) {
        return ((long)(pos.getX() >> 4) << 32) | ((pos.getZ() >> 4) & 0xFFFFFFFFL);
    }

    public int networkCount() { return byId.size(); }
    public NetworkImpl networkAt(BlockPos pos) { return byPosition.get(pos); }
    public Collection<NetworkImpl> allNetworks() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public NetworkImpl placeConduit(BlockPos pos, ConduitType conduitType) {
        if (byPosition.containsKey(pos)) {
            throw new IllegalStateException("Position already has a network node: " + pos);
        }

        // Collect distinct adjacent networks of matching conduit type (0..4 possible).
        Set<NetworkImpl> adjacent = new HashSet<>();
        for (Direction dir : DIRECTIONS) {
            NetworkImpl n = byPosition.get(pos.relative(dir));
            if (n != null && n.conduitType().id().equals(conduitType.id())) {
                adjacent.add(n);
            }
        }

        NetworkImpl target;
        if (adjacent.isEmpty()) {
            target = createNew(conduitType);
        } else if (adjacent.size() == 1) {
            target = adjacent.iterator().next();
        } else {
            target = mergeNetworks(adjacent);
        }

        Node node = new Node(pos, NodeType.CONDUIT);
        node.setNetwork(target);
        target.nodesMap().put(pos, node);
        target.edgesMap().computeIfAbsent(pos, k -> new HashSet<>());
        byPosition.put(pos, target);
        byChunk.computeIfAbsent(chunkKey(pos), k -> new HashSet<>()).add(target.id());

        // Wire bidirectional edges to existing adjacent nodes in the same network.
        for (Direction dir : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(dir);
            if (target.nodesMap().containsKey(neighborPos) && !neighborPos.equals(pos)) {
                target.edgesMap().computeIfAbsent(pos, k -> new HashSet<>()).add(neighborPos);
                target.edgesMap().computeIfAbsent(neighborPos, k -> new HashSet<>()).add(pos);
                target.nodesMap().get(pos).neighbors().add(neighborPos);
                target.nodesMap().get(neighborPos).neighbors().add(pos);
            }
        }

        return target;
    }

    private NetworkImpl createNew(ConduitType conduitType) {
        NetworkImpl n = new NetworkImpl(UUID.randomUUID(), conduitType);
        byId.put(n.id(), n);
        return n;
    }

    private NetworkImpl mergeNetworks(Set<NetworkImpl> adjacent) {
        // Merge into the network with the smallest UUID (deterministic across runs).
        NetworkImpl target = adjacent.stream()
                .min(Comparator.comparing(NetworkImpl::id))
                .orElseThrow();
        for (NetworkImpl other : adjacent) {
            if (other == target) continue;
            for (Map.Entry<BlockPos, Node> entry : other.nodesMap().entrySet()) {
                entry.getValue().setNetwork(target);
                target.nodesMap().put(entry.getKey(), entry.getValue());
                byPosition.put(entry.getKey(), target);
            }
            target.edgesMap().putAll(other.edgesMap());
            target.sourcesSet().addAll(other.sourcesSet());
            target.consumersSet().addAll(other.consumersSet());
            target.storageSet().addAll(other.storageSet());
            byId.remove(other.id());
            // byChunk cleanup
            for (Set<UUID> chunkNets : byChunk.values()) {
                if (chunkNets.remove(other.id())) {
                    chunkNets.add(target.id());
                }
            }
        }
        return target;
    }

    // break / split methods come in T5.
}
