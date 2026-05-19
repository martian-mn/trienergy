package com.trienergy.content;

import com.trienergy.api.EnergyPeripheral;
import com.trienergy.api.MachinePeripheral;
import com.trienergy.api.events.MachineStateChangeEvent;
import com.trienergy.network.NetworkRegistry;
import com.trienergy.network.types.EnergyConduitType;
import com.trienergy.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleConsumerBlockEntity extends BlockEntity implements EnergyPeripheral {
    public static final long DRAIN_RATE = 50L;

    private NetworkRegistry registryRef;

    public SimpleConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(Registries.SIMPLE_CONSUMER_BE.get(), pos, state);
    }

    public void setRegistry(NetworkRegistry r) {
        this.registryRef = r;
    }

    @Override
    public BlockPos position() {
        return getBlockPos();
    }

    @Override
    public ResourceLocation conduitTypeId() {
        return EnergyConduitType.ID;
    }

    @Override
    public int roles() {
        return MachinePeripheral.Role.CONSUMER.mask;
    }

    @Override
    public void notifyStateChange() {
        if (registryRef == null) return;
        registryRef.eventBus().publish(new MachineStateChangeEvent(this));
    }

    @Override
    public long maxOutputThisTick() {
        return 0L;
    }

    @Override
    public long maxIntakeThisTick() {
        return DRAIN_RATE;
    }

    @Override
    public void onOutput(long amount) {
        /* consumer doesn't output */
    }

    @Override
    public void onIntake(long amount) {
        /* drained to nothing */
    }
}
