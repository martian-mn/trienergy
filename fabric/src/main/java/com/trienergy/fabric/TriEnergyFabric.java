package com.trienergy.fabric;

import com.trienergy.TriEnergy;
import net.fabricmc.api.ModInitializer;

public final class TriEnergyFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TriEnergy.init();
    }
}
