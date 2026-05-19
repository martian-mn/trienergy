package com.trienergy.network.bench;

import com.trienergy.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class BenchmarkWorldGen {
    private BenchmarkWorldGen() {}

    /**
     * Lay out a benchmark scenario in-world.
     *
     * <p>Places one SimpleGenerator at {@code anchor}, a line of {@code conduits}
     * EnergyConduit blocks along the +X axis from {@code anchor + (1,0,0)}, and
     * {@code machines} SimpleConsumer blocks on the +Z neighbour of
     * evenly-spaced conduit positions.</p>
     *
     * @param level    server level to place blocks in
     * @param anchor   position of the generator
     * @param conduits number of EnergyConduit blocks to place
     * @param machines number of SimpleConsumer blocks to place
     * @return descriptor of the placed layout
     */
    public static BenchmarkLayout placeBenchmark(
            ServerLevel level, BlockPos anchor, int conduits, int machines) {

        // Generator at anchor
        level.setBlock(anchor, Registries.SIMPLE_GENERATOR_BLOCK.get().defaultBlockState(), 3);

        // Conduit line along +X axis
        BlockPos firstConduit = anchor.offset(1, 0, 0);
        for (int i = 0; i < conduits; i++) {
            BlockPos p = firstConduit.offset(i, 0, 0);
            level.setBlock(p, Registries.ENERGY_CONDUIT_BLOCK.get().defaultBlockState(), 3);
        }

        // Consumers distributed evenly along the conduit line (on +Z neighbour)
        if (machines > 0 && conduits > 0) {
            for (int i = 0; i < machines; i++) {
                int conduitIdx = (int) Math.floor((double) i * conduits / machines);
                BlockPos consumerPos = firstConduit.offset(conduitIdx, 0, 1);
                level.setBlock(consumerPos, Registries.SIMPLE_CONSUMER_BLOCK.get().defaultBlockState(), 3);
            }
        }

        BlockPos end = firstConduit.offset(Math.max(0, conduits - 1), 0, 0);
        return new BenchmarkLayout(anchor, end, conduits, machines);
    }

    /** Describes the block extents of a placed benchmark layout. */
    public record BenchmarkLayout(BlockPos start, BlockPos end, int conduits, int machines) {}
}
