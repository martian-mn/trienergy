package com.trienergy.network;

import com.trienergy.api.ConduitType;
import com.trienergy.api.ConduitTypeRegistry;
import com.trienergy.api.ConduitTypeRegistryHolderAccess;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ConduitTypeRegistryImpl implements ConduitTypeRegistry {
    private final Map<ResourceLocation, ConduitType> byId = new HashMap<>();

    @Override
    public void register(ConduitType type) {
        if (byId.putIfAbsent(type.id(), type) != null) {
            throw new IllegalStateException("ConduitType already registered: " + type.id());
        }
    }

    @Override
    public Optional<ConduitType> get(ResourceLocation id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** Wire this implementation as the api singleton. Call once at mod init. */
    public static void install() {
        ConduitTypeRegistryHolderAccess.set(new ConduitTypeRegistryImpl());
    }
}
