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

public class SimpleGeneratorBlockEntity extends BlockEntity implements EnergyPeripheral {
    private static final long INITIAL_FUEL = 100_000L;
    private static final long OUTPUT_RATE = 100L;
    private static final String KEY_FUEL = "fuel";

    private long fuel = INITIAL_FUEL;
    private NetworkRegistry registryRef;

    public SimpleGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Registries.SIMPLE_GENERATOR_BE.get(), pos, state);
    }

    public void setRegistry(NetworkRegistry r) {
        this.registryRef = r;
    }

    @Override
    public BlockPos position() { return getBlockPos(); }

    @Override
    public ResourceLocation conduitTypeId() { return EnergyConduitType.ID; }

    @Override
    public int roles() { return MachinePeripheral.Role.SOURCE.mask; }

    @Override
    public void notifyStateChange() {
        if (registryRef == null) return;
        registryRef.eventBus().publish(new MachineStateChangeEvent(this));
    }

    @Override
    public long maxOutputThisTick() { return fuel > 0 ? OUTPUT_RATE : 0L; }

    @Override
    public long maxIntakeThisTick() { return 0L; }

    @Override
    public void onOutput(long amount) {
        boolean wasActive = fuel > 0;
        fuel = Math.max(0L, fuel - amount);
        setChanged();
        if (wasActive && fuel == 0L) notifyStateChange();
    }

    @Override
    public void onIntake(long amount) { /* generator does not intake */ }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putLong(KEY_FUEL, fuel);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        if (tag.contains(KEY_FUEL)) {
            fuel = tag.getLong(KEY_FUEL);
        }
    }

    public long fuel() { return fuel; }
}
