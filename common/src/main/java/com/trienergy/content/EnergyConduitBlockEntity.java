package com.trienergy.content;

import com.trienergy.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyConduitBlockEntity extends BlockEntity {
    public EnergyConduitBlockEntity(BlockPos pos, BlockState state) {
        super(Registries.ENERGY_CONDUIT_BE.get(), pos, state);
    }
}
