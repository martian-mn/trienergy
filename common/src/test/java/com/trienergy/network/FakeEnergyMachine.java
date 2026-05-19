package com.trienergy.network;

import com.trienergy.api.EnergyPeripheral;
import com.trienergy.network.types.EnergyConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

class FakeEnergyMachine implements EnergyPeripheral {
    private final BlockPos pos;
    private final long maxOut;
    private final long maxIn;
    int roles;
    long outputDelivered = 0;
    long intakeDelivered = 0;

    FakeEnergyMachine(BlockPos pos, long maxOut, long maxIn) {
        this.pos = pos;
        this.maxOut = maxOut;
        this.maxIn = maxIn;
        this.roles = (maxOut > 0 ? Role.SOURCE.mask : 0)
                   | (maxIn  > 0 ? Role.CONSUMER.mask : 0);
    }

    @Override public BlockPos position() { return pos; }
    @Override public ResourceLocation conduitTypeId() { return EnergyConduitType.ID; }
    @Override public int roles() { return roles; }
    @Override public void notifyStateChange() { /* unused in tests */ }
    @Override public long maxOutputThisTick() { return maxOut; }
    @Override public long maxIntakeThisTick() { return maxIn; }
    @Override public void onOutput(long amount) { outputDelivered += amount; }
    @Override public void onIntake(long amount) { intakeDelivered += amount; }
}
