package com.trienergy.network;

import com.trienergy.api.ConduitTypeRegistry;
import com.trienergy.api.EnergyPeripheral;
import com.trienergy.api.MachinePeripheral;
import com.trienergy.api.NetworkState;
import com.trienergy.network.types.EnergyConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndTest {

    @BeforeAll static void setUp() {
        try { ConduitTypeRegistryImpl.install(); } catch (Exception ignored) {}
        try { ConduitTypeRegistry.instance().register(EnergyConduitType.INSTANCE); }
        catch (IllegalStateException ignored) {}
    }

    /**
     * Generator with finite fuel feeds 5 conduits into a consumer. Verify that
     * energy flows for as long as the generator has fuel, the consumer accumulates
     * intake, and the network transitions to IDLE once the generator depletes.
     */
    @Test
    void generatorConduitConsumerChainDepletesAndIdles() {
        NetworkRegistry registry = new NetworkRegistry();
        NetworkScheduler scheduler = new NetworkScheduler(registry);

        // Generator with 500 FE of fuel, outputs 100 FE/tick → 5 ticks of supply.
        FiniteFuelGenerator gen = new FiniteFuelGenerator(new BlockPos(0, 64, 0), 100L, 500L);
        registry.placeMachine(gen);

        // 5 conduits in a line
        for (int i = 1; i <= 5; i++) {
            registry.placeConduit(new BlockPos(i, 64, 0), EnergyConduitType.INSTANCE);
        }

        // Consumer with 100 FE/tick intake
        FakeEnergyMachine consumer = new FakeEnergyMachine(new BlockPos(6, 64, 0), 0, 100);
        registry.placeMachine(consumer);

        NetworkImpl net = registry.networkAt(new BlockPos(1, 64, 0));
        assertNotNull(net);

        // Tick 5 times — generator should fully drain (5 * 100 = 500 = initial fuel).
        for (int t = 1; t <= 5; t++) {
            scheduler.tick(t);
        }

        // Consumer should have received energy across the 5 active ticks.
        assertTrue(consumer.intakeDelivered >= 400,
                "Expected ~500 FE delivered, got " + consumer.intakeDelivered);
        assertEquals(0L, gen.remaining(), "Generator fuel should be depleted");

        // Tick once more — generator now has 0 output, network should go IDLE.
        scheduler.tick(6L);
        assertEquals(NetworkState.IDLE, net.state(),
                "Network should be IDLE after generator depletes");
    }

    /** Generator with finite fuel — used to verify the IDLE transition. */
    private static final class FiniteFuelGenerator implements EnergyPeripheral {
        private final BlockPos pos;
        private final long outputRate;
        private long remaining;

        FiniteFuelGenerator(BlockPos pos, long outputRate, long initialFuel) {
            this.pos = pos;
            this.outputRate = outputRate;
            this.remaining = initialFuel;
        }

        long remaining() { return remaining; }

        @Override public BlockPos position() { return pos; }
        @Override public ResourceLocation conduitTypeId() { return EnergyConduitType.ID; }
        @Override public int roles() { return MachinePeripheral.Role.SOURCE.mask; }
        @Override public void notifyStateChange() { /* unused in tests */ }
        @Override public long maxOutputThisTick() {
            return remaining > 0 ? Math.min(outputRate, remaining) : 0L;
        }
        @Override public long maxIntakeThisTick() { return 0L; }
        @Override public void onOutput(long amount) { remaining = Math.max(0L, remaining - amount); }
        @Override public void onIntake(long amount) { /* never */ }
    }
}
