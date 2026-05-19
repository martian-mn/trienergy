package com.trienergy;

public final class TriEnergy {
    public static final String MOD_ID = "trienergy";
    public static final String MOD_NAME = "TriEnergy";

    private TriEnergy() {}

    public static void init() {
        com.trienergy.registry.Registries.register();
    }
}
