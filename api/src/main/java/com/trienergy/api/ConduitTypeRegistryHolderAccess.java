package com.trienergy.api;

/**
 * Package-private bridge accessible to the engine impl. Not part of the addon API surface
 * (will be cleaned up before the M7 API freeze).
 */
public final class ConduitTypeRegistryHolderAccess {
    private ConduitTypeRegistryHolderAccess() {}

    public static void set(ConduitTypeRegistry instance) {
        ConduitTypeRegistryHolder.INSTANCE = instance;
    }
}
