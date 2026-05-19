package com.trienergy.network;

import com.trienergy.api.MachinePeripheral;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public final class Node {
    private final BlockPos pos;
    private final NodeType type;
    private final Set<BlockPos> neighbors = new HashSet<>();
    /** Non-null iff {@code type == MACHINE}. */
    private MachinePeripheral peripheral;
    private Object network;   // back-reference; will be retyped to NetworkImpl in T3

    public Node(BlockPos pos, NodeType type) {
        this.pos = pos;
        this.type = type;
    }

    public BlockPos pos() { return pos; }
    public NodeType type() { return type; }
    public Set<BlockPos> neighbors() { return neighbors; }
    public Object network() { return network; }

    /** Package-private — only NetworkRegistry should reassign a node's network. */
    void setNetwork(Object network) { this.network = network; }

    public MachinePeripheral peripheral() { return peripheral; }
    public void setPeripheral(MachinePeripheral peripheral) { this.peripheral = peripheral; }
}
