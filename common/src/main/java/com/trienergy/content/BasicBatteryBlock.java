package com.trienergy.content;

import com.trienergy.network.NetworkRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class BasicBatteryBlock extends Block implements EntityBlock {

    public BasicBatteryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties defaultProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(1.5f, 6.0f)
                .requiresCorrectToolForDrops();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicBatteryBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            NetworkRegistry registry = TriEnergyWorldState.get(serverLevel).networkRegistry();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BasicBatteryBlockEntity battery) {
                battery.setRegistry(registry);
                try {
                    registry.placeMachine(battery);
                } catch (IllegalStateException e) {
                    // chunk-reload restore — node already present
                }
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
