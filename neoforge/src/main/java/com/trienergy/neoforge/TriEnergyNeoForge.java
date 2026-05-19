package com.trienergy.neoforge;

import com.trienergy.TriEnergy;
import com.trienergy.api.EnergyPeripheral;
import com.trienergy.neoforge.energy.NeoForgeEnergyAdapter;
import com.trienergy.registry.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(TriEnergy.MOD_ID)
public final class TriEnergyNeoForge {
    public TriEnergyNeoForge(IEventBus modEventBus) {
        TriEnergy.init();
        modEventBus.addListener(TriEnergyNeoForge::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Generator
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                Registries.SIMPLE_GENERATOR_BE.get(),
                (be, side) -> new NeoForgeEnergyAdapter((EnergyPeripheral) be));
        // Battery
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                Registries.BASIC_BATTERY_BE.get(),
                (be, side) -> new NeoForgeEnergyAdapter((EnergyPeripheral) be));
        // Consumer
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                Registries.SIMPLE_CONSUMER_BE.get(),
                (be, side) -> new NeoForgeEnergyAdapter((EnergyPeripheral) be));
        // Conduit — NOT an EnergyPeripheral itself, so skip. External mods
        // talking to a conduit position should route through the network
        // (future M2 work to expose network endpoints as capabilities).
    }
}
