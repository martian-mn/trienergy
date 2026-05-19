package com.trienergy.network;

import com.trienergy.api.ConduitType;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkSplitMergeTest {

    @Test
    void placingConduitBetweenTwoNetworksMergesThem() {
        NetworkRegistry registry = new NetworkRegistry();
        ConduitType type = TestOnlyConduitType.INSTANCE;

        // Two separate conduits 2 blocks apart on the X axis
        registry.placeConduit(new BlockPos(0, 64, 0), type);
        registry.placeConduit(new BlockPos(2, 64, 0), type);
        assertEquals(2, registry.networkCount());

        // Place the bridge in the middle
        registry.placeConduit(new BlockPos(1, 64, 0), type);

        // Two networks merge into one
        assertEquals(1, registry.networkCount());
        NetworkImpl bridged = registry.networkAt(new BlockPos(1, 64, 0));
        assertEquals(3, bridged.nodes().size());
        assertSame(bridged, registry.networkAt(new BlockPos(0, 64, 0)));
        assertSame(bridged, registry.networkAt(new BlockPos(2, 64, 0)));
    }

    @Test
    void placingConduitTouchingFourNetworksMergesAllFour() {
        NetworkRegistry registry = new NetworkRegistry();
        ConduitType type = TestOnlyConduitType.INSTANCE;

        // Four conduits at compass-cardinals around (0, 64, 0)
        registry.placeConduit(new BlockPos(1, 64, 0), type);
        registry.placeConduit(new BlockPos(-1, 64, 0), type);
        registry.placeConduit(new BlockPos(0, 64, 1), type);
        registry.placeConduit(new BlockPos(0, 64, -1), type);
        assertEquals(4, registry.networkCount());

        // Place the centre — bridges all four
        registry.placeConduit(new BlockPos(0, 64, 0), type);

        assertEquals(1, registry.networkCount());
        NetworkImpl merged = registry.networkAt(new BlockPos(0, 64, 0));
        assertEquals(5, merged.nodes().size());
    }

    @Test
    void breakingMidConduitOnLineSplitsTheNetwork() {
        NetworkRegistry registry = new NetworkRegistry();
        ConduitType type = TestOnlyConduitType.INSTANCE;

        // Line: (0,64,0) - (1,64,0) - (2,64,0)
        registry.placeConduit(new BlockPos(0, 64, 0), type);
        registry.placeConduit(new BlockPos(1, 64, 0), type);
        registry.placeConduit(new BlockPos(2, 64, 0), type);
        assertEquals(1, registry.networkCount());

        // Break the middle
        registry.breakConduit(new BlockPos(1, 64, 0));

        // Should split into 2 networks
        assertEquals(2, registry.networkCount());
        NetworkImpl left = registry.networkAt(new BlockPos(0, 64, 0));
        NetworkImpl right = registry.networkAt(new BlockPos(2, 64, 0));
        assertNotNull(left);
        assertNotNull(right);
        assertNotSame(left, right);
    }

    @Test
    void breakingDeadEndConduitDoesNotSplit() {
        NetworkRegistry registry = new NetworkRegistry();
        ConduitType type = TestOnlyConduitType.INSTANCE;

        registry.placeConduit(new BlockPos(0, 64, 0), type);
        registry.placeConduit(new BlockPos(1, 64, 0), type);
        registry.placeConduit(new BlockPos(2, 64, 0), type);

        // Break the end — only 1 neighbour remaining, no split possible
        registry.breakConduit(new BlockPos(2, 64, 0));

        assertEquals(1, registry.networkCount());
        NetworkImpl remaining = registry.networkAt(new BlockPos(0, 64, 0));
        assertEquals(2, remaining.nodes().size());
    }

    @Test
    void breakingTJunctionSplitsIntoThree() {
        NetworkRegistry registry = new NetworkRegistry();
        ConduitType type = TestOnlyConduitType.INSTANCE;

        // T-shape:
        //   (0,64,0) - (1,64,0) - (2,64,0)
        //              |
        //           (1,64,1)
        registry.placeConduit(new BlockPos(0, 64, 0), type);
        registry.placeConduit(new BlockPos(1, 64, 0), type);
        registry.placeConduit(new BlockPos(2, 64, 0), type);
        registry.placeConduit(new BlockPos(1, 64, 1), type);

        // Break the junction
        registry.breakConduit(new BlockPos(1, 64, 0));

        // Three branches → three networks
        assertEquals(3, registry.networkCount());
    }
}
