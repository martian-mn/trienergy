package com.trienergy.content;

import com.trienergy.api.EnergyPeripheral;
import com.trienergy.api.MachinePeripheral;
import com.trienergy.api.events.MachineStateChangeEvent;
import com.trienergy.network.NetworkRegistry;
import com.trienergy.network.types.EnergyConduitType;
import com.trienergy.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBatteryBlockEntity extends BlockEntity implements EnergyPeripheral {
    public static final long CAPACITY = 10_000L;
    public static final long MAX_TRANSFER_PER_TICK = 100L;
    private static final String KEY_STORED = "stored";

    private long stored = 0L;
    private NetworkRegistry registryRef;

    public BasicBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(Registries.BASIC_BATTERY_BE.get(), pos, state);
    }

    public void setRegistry(NetworkRegistry r) {
        this.registryRef = r;
    }

    @Override
    public BlockPos position() { return getBlockPos(); }

    @Override
    public ResourceLocation conduitTypeId() { return EnergyConduitType.ID; }

    @Override
    public int roles() { return MachinePeripheral.Role.STORAGE.mask; }

    @Override
    public void notifyStateChange() {
        if (registryRef == null) return;
        registryRef.eventBus().publish(new MachineStateChangeEvent(this));
    }

    @Override
    public long maxOutputThisTick() { return Math.min(stored, MAX_TRANSFER_PER_TICK); }

    @Override
    public long maxIntakeThisTick() { return Math.min(CAPACITY - stored, MAX_TRANSFER_PER_TICK); }

    @Override
    public void onOutput(long amount) {
        long before = stored;
        stored = Math.max(0L, stored - amount);
        setChanged();
        if (before > 0 && stored == 0) notifyStateChange();
    }

    @Override
    public void onIntake(long amount) {
        long before = stored;
        stored = Math.min(CAPACITY, stored + amount);
        setChanged();
        if ((before == 0 && stored > 0) || (before < CAPACITY && stored == CAPACITY)) {
            notifyStateChange();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putLong(KEY_STORED, stored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        if (tag.contains(KEY_STORED)) {
            stored = tag.getLong(KEY_STORED);
        }
    }

    public long stored() { return stored; }
}
