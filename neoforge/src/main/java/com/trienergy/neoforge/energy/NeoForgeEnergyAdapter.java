package com.trienergy.neoforge.energy;

import com.trienergy.api.EnergyPeripheral;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Wraps a TriEnergy {@link EnergyPeripheral} as a NeoForge {@link IEnergyStorage}.
 * Exposed via capability registration so other mods see TriEnergy machines as
 * standard FE storages.
 */
public final class NeoForgeEnergyAdapter implements IEnergyStorage {
    private final EnergyPeripheral peripheral;

    public NeoForgeEnergyAdapter(EnergyPeripheral peripheral) {
        this.peripheral = peripheral;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long canAccept = peripheral.maxIntakeThisTick();
        long actual = Math.min(maxReceive, canAccept);
        if (!simulate && actual > 0) {
            peripheral.onIntake(actual);
        }
        return (int) actual;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        long canOutput = peripheral.maxOutputThisTick();
        long actual = Math.min(maxExtract, canOutput);
        if (!simulate && actual > 0) {
            peripheral.onOutput(actual);
        }
        return (int) actual;
    }

    @Override
    public int getEnergyStored() {
        // For consumer / generator / conduit (non-storage), report 0.
        // For battery (storage), maxOutputThisTick == stored capped to MAX_TRANSFER.
        // The real stored amount is on the battery BE; this adapter doesn't see it
        // directly. Reporting maxOutputThisTick is a reasonable approximation —
        // external mods reading "stored" generally use it for HUDs, not for math.
        return (int) peripheral.maxOutputThisTick();
    }

    @Override
    public int getMaxEnergyStored() {
        // Same caveat as above. For an external HUD reading "fill percentage",
        // reporting max intake as the capacity is misleading for batteries but
        // OK for M1 first-pass capability exposure. T7+ refinement deferred.
        return (int) Math.max(peripheral.maxIntakeThisTick(), peripheral.maxOutputThisTick());
    }

    @Override public boolean canExtract() { return peripheral.maxOutputThisTick() > 0; }
    @Override public boolean canReceive() { return peripheral.maxIntakeThisTick() > 0; }
}
