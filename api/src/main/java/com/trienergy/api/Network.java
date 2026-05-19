package com.trienergy.api;

import net.minecraft.core.BlockPos;

import java.util.Set;
import java.util.UUID;

/**
 * Read-only view of a network. Addons use this; mutation goes through internal
 * {@code NetworkImpl}. Snapshots are stable for the calling tick only — if you
 * need cross-tick stability, use {@link NetworkSnapshot}.
 */
public interface Network {
    UUID id();

    NetworkState state();

    ConduitType conduitType();

    Set<BlockPos> nodes();

    Set<BlockPos> sources();

    Set<BlockPos> consumers();

    Set<BlockPos> storage();

    /**
     * Defensive immutable copy for cross-tick reads.
     */
    NetworkSnapshot snapshot();
}
