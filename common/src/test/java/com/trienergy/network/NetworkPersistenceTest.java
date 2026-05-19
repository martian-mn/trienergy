package com.trienergy.network;

import com.trienergy.api.ConduitTypeRegistry;
import com.trienergy.network.types.EnergyConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkPersistenceTest {

    @BeforeAll static void setUp() {
        try { ConduitTypeRegistryImpl.install(); } catch (Exception ignored) {}
        try { ConduitTypeRegistry.instance().register(EnergyConduitType.INSTANCE); }
        catch (IllegalStateException ignored) {}
    }

    @Test
    void suspendAndRestoreRoundTripPreservesNetwork() {
        NetworkRegistry r = new NetworkRegistry();
        // Place 3 conduits in chunk (0, 0) — positions (1..3, 64, 1)
        r.placeConduit(new BlockPos(1, 64, 1), EnergyConduitType.INSTANCE);
        r.placeConduit(new BlockPos(2, 64, 1), EnergyConduitType.INSTANCE);
        r.placeConduit(new BlockPos(3, 64, 1), EnergyConduitType.INSTANCE);
        assertEquals(1, r.networkCount());

        long chunkKey = NetworkRegistry.chunkKey(new BlockPos(1, 64, 1));
        CompoundTag tag = r.suspendChunk(chunkKey);
        assertNotNull(tag, "suspendChunk should return a non-null tag");
        assertEquals(0, r.networkCount());

        r.restoreChunk(chunkKey, tag);
        assertEquals(1, r.networkCount());
        assertEquals(3, r.networkAt(new BlockPos(1, 64, 1)).nodes().size());
    }

    @Test
    void suspendEmptyChunkReturnsNull() {
        NetworkRegistry r = new NetworkRegistry();
        long emptyChunkKey = NetworkRegistry.chunkKey(new BlockPos(100, 64, 100));
        assertNull(r.suspendChunk(emptyChunkKey));
    }

    @Test
    void restoreChunkWithNullTagIsNoOp() {
        NetworkRegistry r = new NetworkRegistry();
        // Should not throw
        r.restoreChunk(0L, null);
        assertEquals(0, r.networkCount());
    }
}
