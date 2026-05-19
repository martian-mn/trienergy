package com.trienergy.network;

import com.trienergy.api.ConduitType;
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

    @Test
    void placingFirstConduitCreatesNewNetwork() {
        NetworkRegistry registry = new NetworkRegistry();
        ConduitType type = TestOnlyConduitType.INSTANCE;
        BlockPos pos = new BlockPos(0, 64, 0);

        registry.placeConduit(pos, type);

        assertEquals(1, registry.networkCount());
        NetworkImpl net = registry.networkAt(pos);
        assertNotNull(net);
        assertEquals(1, net.nodes().size());
        assertTrue(net.nodes().contains(pos));
        assertSame(type, net.conduitType());
    }
}
