package com.trienergy.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Registry for {@link ConduitType} instances. Addons query this to look up
 * network physics by type. The engine wires the singleton at mod init.
 */
public interface ConduitTypeRegistry {
    /**
     * Register a new {@code ConduitType}.
     */
    void register(ConduitType type);

    /**
     * Look up a {@code ConduitType} by ID.
     */
    Optional<ConduitType> get(ResourceLocation id);

    /**
     * Returns the singleton implementation. Set by the engine at mod init.
     */
    static ConduitTypeRegistry instance() {
        return ConduitTypeRegistryHolder.INSTANCE;
    }
}

/**
 * Package-private holder so the engine can wire the impl at init.
 */
final class ConduitTypeRegistryHolder {
    static ConduitTypeRegistry INSTANCE = new ConduitTypeRegistry() {
        @Override
        public void register(ConduitType type) {
            throw new IllegalStateException("ConduitTypeRegistry not initialised yet");
        }

        @Override
        public Optional<ConduitType> get(ResourceLocation id) {
            throw new IllegalStateException("ConduitTypeRegistry not initialised yet");
        }
    };

    private ConduitTypeRegistryHolder() {}
}
