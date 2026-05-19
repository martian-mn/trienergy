package com.trienergy.network.types;

import com.trienergy.TriEnergy;
import com.trienergy.api.ConduitType;
import com.trienergy.api.Network;
import net.minecraft.resources.ResourceLocation;

public final class EnergyConduitType implements ConduitType {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(TriEnergy.MOD_ID, "energy");
    public static final EnergyConduitType INSTANCE = new EnergyConduitType();

    private EnergyConduitType() {}

    @Override public ResourceLocation id() { return ID; }

    @Override
    public void tick(Network network) {
        // Distribution algorithm lands in T7. For now, no-op so the engine
        // can be wired up.
    }
}
