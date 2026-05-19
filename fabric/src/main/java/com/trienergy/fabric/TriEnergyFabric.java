package com.trienergy.fabric;

import com.trienergy.TriEnergy;
import com.trienergy.api.EnergyPeripheral;
import com.trienergy.fabric.energy.FabricEnergyAdapter;
import com.trienergy.registry.Registries;
import net.fabricmc.api.ModInitializer;
import team.reborn.energy.api.EnergyStorage;

public final class TriEnergyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TriEnergy.init();

        // Expose TriEnergy machines as Team Reborn Energy storages so other
        // Fabric mods can transfer energy to/from them via the standard TRE API.
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be instanceof EnergyPeripheral ep
                        ? new FabricEnergyAdapter(ep) : null,
                Registries.SIMPLE_GENERATOR_BE.get());
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be instanceof EnergyPeripheral ep
                        ? new FabricEnergyAdapter(ep) : null,
                Registries.BASIC_BATTERY_BE.get());
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be instanceof EnergyPeripheral ep
                        ? new FabricEnergyAdapter(ep) : null,
                Registries.SIMPLE_CONSUMER_BE.get());
        // Conduit BE is NOT an EnergyPeripheral — external mods should interact
        // with the network via machine endpoints, not conduit positions. Deferred
        // to M2 when network-endpoint capability exposure is designed.
    }
}

