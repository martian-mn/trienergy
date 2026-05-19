package com.trienergy.network;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkRegistryTest {

    @Test
    void newRegistryHasNoNetworks() {
        NetworkRegistry registry = new NetworkRegistry();
        assertEquals(0, registry.networkCount());
        assertNull(registry.networkAt(new BlockPos(0, 0, 0)));
    }
}
