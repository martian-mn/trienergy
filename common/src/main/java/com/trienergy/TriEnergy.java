package com.trienergy;

import com.trienergy.api.ConduitTypeRegistry;
import com.trienergy.network.ConduitTypeRegistryImpl;
import com.trienergy.network.types.EnergyConduitType;

public final class TriEnergy {
    public static final String MOD_ID = "trienergy";
    public static final String MOD_NAME = "TriEnergy";

    private TriEnergy() {}

    public static void init() {
        ConduitTypeRegistryImpl.install();
        ConduitTypeRegistry.instance().register(EnergyConduitType.INSTANCE);
        com.trienergy.registry.Registries.register();
    }
}
