package com.trienergy.network;

import com.trienergy.api.ConduitTypeRegistry;
import com.trienergy.api.NetworkState;
import com.trienergy.network.types.EnergyConduitType;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkSchedulerTest {

    @BeforeAll static void setUp() {
        try { ConduitTypeRegistryImpl.install(); } catch (Exception ignored) {}
        try { ConduitTypeRegistry.instance().register(EnergyConduitType.INSTANCE); }
        catch (IllegalStateException ignored) {}
    }

    @Test
    void networkWithSupplyAndDemandTransitionsToActive() {
        NetworkRegistry registry = new NetworkRegistry();
        NetworkScheduler scheduler = new NetworkScheduler(registry);

        registry.placeConduit(new BlockPos(1, 64, 0), EnergyConduitType.INSTANCE);
        registry.placeMachine(new FakeEnergyMachine(new BlockPos(0, 64, 0), 100, 0)); // source
        registry.placeMachine(new FakeEnergyMachine(new BlockPos(2, 64, 0), 0, 100)); // consumer

        scheduler.tick(1L);

        NetworkImpl net = registry.networkAt(new BlockPos(1, 64, 0));
        assertEquals(NetworkState.ACTIVE, net.state());
    }

    @Test
    void networkWithoutDemandTransitionsToIdle() {
        NetworkRegistry registry = new NetworkRegistry();
        NetworkScheduler scheduler = new NetworkScheduler(registry);

        registry.placeConduit(new BlockPos(1, 64, 0), EnergyConduitType.INSTANCE);
        registry.placeMachine(new FakeEnergyMachine(new BlockPos(0, 64, 0), 100, 0)); // source only

        scheduler.tick(1L);

        NetworkImpl net = registry.networkAt(new BlockPos(1, 64, 0));
        assertEquals(NetworkState.IDLE, net.state());
    }
}
