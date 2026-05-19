package com.trienergy.network;

import com.trienergy.api.ConduitType;
import com.trienergy.api.MachinePeripheral;
import com.trienergy.api.events.NetworkChangedEvent;
import com.trienergy.network.events.EventBus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class NetworkRegistry {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final EventBus eventBus = new EventBus();
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

    public EventBus eventBus() { return eventBus; }

    public int networkCount() { return byId.size(); }
    public NetworkImpl networkAt(BlockPos pos) { return byPosition.get(pos); }
    public Collection<NetworkImpl> allNetworks() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public NetworkImpl placeMachine(MachinePeripheral peripheral) {
        BlockPos pos = peripheral.position();
        if (byPosition.containsKey(pos)) {
            throw new IllegalStateException("Position already has a network node: " + pos);
        }

        ResourceLocation typeId = peripheral.conduitTypeId();
        ConduitType conduitType = com.trienergy.api.ConduitTypeRegistry.instance().get(typeId)
                .orElseThrow(() -> new IllegalStateException("Unknown conduit type: " + typeId));

        // Find adjacent networks of the matching type
        Set<NetworkImpl> adjacent = new HashSet<>();
        for (Direction dir : DIRECTIONS) {
            NetworkImpl n = byPosition.get(pos.relative(dir));
            if (n != null && n.conduitType().id().equals(conduitType.id())) {
                adjacent.add(n);
            }
        }

        NetworkImpl target = switch (adjacent.size()) {
            case 0 -> createNew(conduitType);
            case 1 -> adjacent.iterator().next();
            default -> mergeNetworks(adjacent);
        };

        Node node = new Node(pos, NodeType.MACHINE);
        node.setPeripheral(peripheral);
        node.setNetwork(target);
        target.nodesMap().put(pos, node);
        target.edgesMap().computeIfAbsent(pos, k -> new HashSet<>());
        byPosition.put(pos, target);
        byChunk.computeIfAbsent(chunkKey(pos), k -> new HashSet<>()).add(target.id());

        // Wire edges to adjacent nodes already in the network
        for (Direction dir : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(dir);
            if (target.nodesMap().containsKey(neighborPos) && !neighborPos.equals(pos)) {
                target.edgesMap().computeIfAbsent(pos, k -> new HashSet<>()).add(neighborPos);
                target.edgesMap().computeIfAbsent(neighborPos, k -> new HashSet<>()).add(pos);
                target.nodesMap().get(pos).neighbors().add(neighborPos);
                target.nodesMap().get(neighborPos).neighbors().add(pos);
            }
        }

        int roles = peripheral.roles();
        if ((roles & MachinePeripheral.Role.SOURCE.mask)   != 0) target.sourcesSet().add(pos);
        if ((roles & MachinePeripheral.Role.CONSUMER.mask) != 0) target.consumersSet().add(pos);
        if ((roles & MachinePeripheral.Role.STORAGE.mask)  != 0) target.storageSet().add(pos);

        eventBus.publish(new NetworkChangedEvent(
                java.util.List.of(),
                java.util.List.of(target.snapshot())
        ));
        return target;
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

        eventBus.publish(new NetworkChangedEvent(
                java.util.List.of(),
                java.util.List.of(target.snapshot())
        ));
        return target;
    }

    private NetworkImpl createNew(ConduitType conduitType) {
        NetworkImpl n = new NetworkImpl(UUID.randomUUID(), conduitType);
        n.setRegistry(this);
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

    public void breakConduit(BlockPos pos) {
        NetworkImpl net = byPosition.get(pos);
        if (net == null) return;

        Node broken = net.nodesMap().remove(pos);
        if (broken == null) return;

        Set<BlockPos> remainingNeighbors = new HashSet<>(broken.neighbors());
        // Clean up adjacency in the remaining nodes
        for (BlockPos n : remainingNeighbors) {
            Node neighborNode = net.nodesMap().get(n);
            if (neighborNode != null) {
                neighborNode.neighbors().remove(pos);
            }
            Set<BlockPos> nbrEdges = net.edgesMap().get(n);
            if (nbrEdges != null) nbrEdges.remove(pos);
        }
        net.edgesMap().remove(pos);
        byPosition.remove(pos);
        Set<UUID> chunkNets = byChunk.get(chunkKey(pos));
        if (chunkNets != null) {
            // Only remove the network from the chunk if it has no more nodes in that chunk.
            boolean stillInChunk = false;
            for (BlockPos other : net.nodes()) {
                if (chunkKey(other) == chunkKey(pos)) {
                    stillInChunk = true;
                    break;
                }
            }
            if (!stillInChunk) chunkNets.remove(net.id());
        }

        // If the network is now empty, delete it.
        if (net.nodesMap().isEmpty()) {
            byId.remove(net.id());
            eventBus.publish(new NetworkChangedEvent(java.util.List.of(), java.util.List.of()));
            return;
        }

        // Split detection — need at least 2 remaining neighbours to potentially split.
        if (remainingNeighbors.size() < 2) {
            return;
        }

        // BFS from each remaining neighbor; collect unique reachable sets.
        List<Set<BlockPos>> components = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        for (BlockPos start : remainingNeighbors) {
            if (visited.contains(start)) continue;
            if (!net.nodesMap().containsKey(start)) continue;
            Set<BlockPos> component = bfs(net, start);
            visited.addAll(component);
            components.add(component);
        }

        if (components.size() <= 1) return;  // still connected

        // Split: keep the largest component in the existing network, move the others to new networks.
        Set<BlockPos> keepers = components.stream()
                .max(Comparator.comparingInt(Set::size))
                .orElseThrow();
        for (Set<BlockPos> component : components) {
            if (component == keepers) continue;
            NetworkImpl newNet = createNew(net.conduitType());
            for (BlockPos p : component) {
                Node node = net.nodesMap().remove(p);
                node.setNetwork(newNet);
                newNet.nodesMap().put(p, node);
                Set<BlockPos> edges = net.edgesMap().remove(p);
                newNet.edgesMap().put(p, edges != null ? edges : new HashSet<>());
                byPosition.put(p, newNet);
                byChunk.computeIfAbsent(chunkKey(p), k -> new HashSet<>()).add(newNet.id());
            }
            moveCategories(net, newNet, component);
        }

        // Recompute the old network's byChunk presence (its nodes shrunk).
        rebuildByChunkFor(net);

        java.util.List<com.trienergy.api.NetworkSnapshot> afterSnapshots = new java.util.ArrayList<>();
        if (byId.containsKey(net.id())) afterSnapshots.add(net.snapshot());
        eventBus.publish(new NetworkChangedEvent(java.util.List.of(), afterSnapshots));
    }

    private Set<BlockPos> bfs(NetworkImpl net, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            Set<BlockPos> edges = net.edgesMap().get(current);
            if (edges == null) continue;
            for (BlockPos next : edges) {
                if (visited.add(next)) queue.add(next);
            }
        }
        return visited;
    }

    private void moveCategories(NetworkImpl from, NetworkImpl to, Set<BlockPos> positions) {
        for (BlockPos p : positions) {
            if (from.sourcesSet().remove(p))   to.sourcesSet().add(p);
            if (from.consumersSet().remove(p)) to.consumersSet().add(p);
            if (from.storageSet().remove(p))   to.storageSet().add(p);
        }
    }

    private void rebuildByChunkFor(NetworkImpl net) {
        // Find which chunks the network's remaining nodes are in.
        Set<Long> presentChunks = new HashSet<>();
        for (BlockPos p : net.nodes()) presentChunks.add(chunkKey(p));
        // Remove from chunks where it no longer has presence.
        for (Map.Entry<Long, Set<UUID>> entry : byChunk.entrySet()) {
            if (!presentChunks.contains(entry.getKey())) {
                entry.getValue().remove(net.id());
            }
        }
    }
}
