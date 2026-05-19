package com.trienergy.content;

import com.trienergy.network.NetworkRegistry;
import com.trienergy.network.types.EnergyConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class EnergyConduitBlock extends Block implements EntityBlock {

    public EnergyConduitBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties defaultProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(0.5f, 1.0f);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyConduitBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            NetworkRegistry registry = TriEnergyWorldState.get(serverLevel).networkRegistry();
            try {
                registry.placeConduit(pos, EnergyConduitType.INSTANCE);
            } catch (IllegalStateException e) {
                // Position already has a node — this happens on chunk reload when the
                // engine restored the node before the block re-attached. Safe to ignore.
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()
                && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            NetworkRegistry registry = TriEnergyWorldState.get(serverLevel).networkRegistry();
            registry.breakConduit(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
