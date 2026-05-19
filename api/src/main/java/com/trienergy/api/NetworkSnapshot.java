package com.trienergy.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.UUID;

/**
 * Immutable copy of a network's state at a point in time. Safe to retain
 * across ticks (unlike {@link Network}, which may be mutated by the engine).
 */
public record NetworkSnapshot(
        UUID id,
        NetworkState state,
        ResourceLocation conduitTypeId,
        Set<BlockPos> nodes,
        Set<BlockPos> sources,
        Set<BlockPos> consumers,
        Set<BlockPos> storage
) {}
