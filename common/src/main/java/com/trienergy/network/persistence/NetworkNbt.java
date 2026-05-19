package com.trienergy.network.persistence;

import com.trienergy.api.NetworkState;
import com.trienergy.network.NetworkImpl;
import com.trienergy.network.Node;
import com.trienergy.network.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Serialization of {@link NetworkImpl} state to/from NBT for chunk save/load.
 * Schema version 1.
 */
public final class NetworkNbt {
    public static final int SCHEMA_VERSION = 1;
    private static final String KEY_VERSION = "schema_version";
    private static final String KEY_ID = "id";
    private static final String KEY_CONDUIT_TYPE = "conduit_type";
    private static final String KEY_NODES = "nodes";
    private static final String KEY_STATE = "state";

    private NetworkNbt() {}

    public static CompoundTag save(NetworkImpl net, Set<BlockPos> includePositions) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_VERSION, SCHEMA_VERSION);
        tag.putUUID(KEY_ID, net.id());
        tag.putString(KEY_CONDUIT_TYPE, net.conduitType().id().toString());
        tag.putString(KEY_STATE, net.state().name());

        ListTag nodes = new ListTag();
        for (BlockPos pos : includePositions) {
            Node node = net.nodesMap().get(pos);
            if (node == null) continue;
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putLong("pos", pos.asLong());
            nodeTag.putString("type", node.type().name());
            nodes.add(nodeTag);
        }
        tag.put(KEY_NODES, nodes);
        return tag;
    }

    public record RestoredNetwork(UUID id, String conduitTypeId, NetworkState state, Set<BlockPos> nodes) {}

    public static RestoredNetwork load(CompoundTag tag) {
        int v = tag.getInt(KEY_VERSION);
        if (v != SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported network NBT schema version: " + v);
        }
        UUID id = tag.getUUID(KEY_ID);
        String typeId = tag.getString(KEY_CONDUIT_TYPE);
        NetworkState state = NetworkState.valueOf(tag.getString(KEY_STATE));

        Set<BlockPos> nodes = new HashSet<>();
        ListTag nodeList = tag.getList(KEY_NODES, Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeList.size(); i++) {
            CompoundTag nodeTag = nodeList.getCompound(i);
            nodes.add(BlockPos.of(nodeTag.getLong("pos")));
        }
        return new RestoredNetwork(id, typeId, state, nodes);
    }
}
