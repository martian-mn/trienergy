package com.trienergy.api;

import net.minecraft.resources.ResourceLocation;

/**
 * Plug-in interface for a network's "physics." One {@code ConduitType} = one
 * kind of network (energy / mechanical / thermal / future datapack types).
 *
 * <p>Implementations live outside {@code api/} (typically in
 * {@code common/network/types/}) and are registered with
 * {@link ConduitTypeRegistry}.</p>
 */
public interface ConduitType {
    ResourceLocation id();

    /**
     * Run one tick of this network's physics. Called per game tick on ACTIVE
     * networks by {@code NetworkScheduler}.
     */
    void tick(Network network);
}
