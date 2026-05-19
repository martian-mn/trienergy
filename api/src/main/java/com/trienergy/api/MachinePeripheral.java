package com.trienergy.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * The "thing" attached to the network from a block entity. Implementations
 * declare which {@link ConduitType} they participate in and identify which
 * role they play (source / consumer / storage) via the role bitset.
 */
public interface MachinePeripheral {
    BlockPos position();

    ResourceLocation conduitTypeId();

    /**
     * Role bitset combining {@link Role} values.
     */
    int roles();

    /**
     * Notify subscribers (i.e., the containing {@link Network}) that this peripheral's
     * state changed (e.g. idle ↔ active, full ↔ partial). Drives the event side
     * of the hybrid wake mechanism.
     */
    void notifyStateChange();

    /**
     * Peripheral role flags.
     */
    enum Role {
        SOURCE(1 << 0),
        CONSUMER(1 << 1),
        STORAGE(1 << 2);

        public final int mask;

        Role(int mask) {
            this.mask = mask;
        }
    }
}
