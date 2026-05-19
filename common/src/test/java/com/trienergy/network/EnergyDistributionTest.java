package com.trienergy.network;

import com.trienergy.api.ConduitTypeRegistry;
import com.trienergy.network.types.EnergyConduitType;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnergyDistributionTest {

    @BeforeAll static void setUp() {
        // Ensure the registry singleton is wired and EnergyConduitType is registered.
        // It may already be installed by a prior test; that's OK — install() throws
        // if double-registered, but the registry impl itself handles double-register
        // by re-throwing IllegalStateException. We just need ONE of these to succeed.
        try {
            ConduitTypeRegistryImpl.install();
        } catch (Exception ignored) {}
        try {
            ConduitTypeRegistry.instance().register(EnergyConduitType.INSTANCE);
        } catch (IllegalStateException ignored) {
            // Already registered by a previous test class — fine.
        }
    }

    @Test
    void singleSourceFillsSingleConsumerWhenSupplyEqualsDemand() {
        NetworkRegistry registry = new NetworkRegistry();
        // 3-node line: source — conduit — consumer
        registry.placeConduit(new BlockPos(1, 64, 0), EnergyConduitType.INSTANCE);
        FakeEnergyMachine source = new FakeEnergyMachine(new BlockPos(0, 64, 0), 100, 0);
        FakeEnergyMachine consumer = new FakeEnergyMachine(new BlockPos(2, 64, 0), 0, 100);
        registry.placeMachine(source);
        registry.placeMachine(consumer);

        EnergyConduitType.INSTANCE.tick(registry.networkAt(new BlockPos(1, 64, 0)));

        assertEquals(100, source.outputDelivered);
        assertEquals(100, consumer.intakeDelivered);
    }

    @Test
    void shortageSplitsAvailableProRataAmongConsumers() {
        NetworkRegistry registry = new NetworkRegistry();
        registry.placeConduit(new BlockPos(1, 64, 0), EnergyConduitType.INSTANCE);
        registry.placeConduit(new BlockPos(2, 64, 0), EnergyConduitType.INSTANCE);
        FakeEnergyMachine source = new FakeEnergyMachine(new BlockPos(0, 64, 0), 50, 0);
        FakeEnergyMachine c1 = new FakeEnergyMachine(new BlockPos(3, 64, 0), 0, 100);
        FakeEnergyMachine c2 = new FakeEnergyMachine(new BlockPos(2, 65, 0), 0, 100);
        registry.placeMachine(source);
        registry.placeMachine(c1);
        registry.placeMachine(c2);

        EnergyConduitType.INSTANCE.tick(registry.networkAt(new BlockPos(1, 64, 0)));

        // 50 FE split between two 100-FE consumers: 25 each
        assertEquals(50, source.outputDelivered);
        assertEquals(25, c1.intakeDelivered);
        assertEquals(25, c2.intakeDelivered);
    }
}
