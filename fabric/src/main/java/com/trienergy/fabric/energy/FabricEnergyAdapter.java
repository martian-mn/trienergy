package com.trienergy.fabric.energy;

import com.trienergy.api.EnergyPeripheral;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/**
 * Wraps a TriEnergy {@link EnergyPeripheral} as a Team Reborn Energy
 * {@link EnergyStorage}.
 *
 * <p>Insertion and extraction are deferred to {@link TransactionContext}
 * close callbacks so the actual state change only applies when the
 * transaction commits. This matches the Fabric Transfer API contract.
 */
public final class FabricEnergyAdapter implements EnergyStorage {
    private final EnergyPeripheral peripheral;

    public FabricEnergyAdapter(EnergyPeripheral peripheral) {
        this.peripheral = peripheral;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long canAccept = peripheral.maxIntakeThisTick();
        long actual = Math.min(maxAmount, canAccept);
        if (actual > 0) {
            transaction.addCloseCallback((tx, result) -> {
                if (result.wasCommitted()) {
                    peripheral.onIntake(actual);
                }
            });
        }
        return actual;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long canOutput = peripheral.maxOutputThisTick();
        long actual = Math.min(maxAmount, canOutput);
        if (actual > 0) {
            transaction.addCloseCallback((tx, result) -> {
                if (result.wasCommitted()) {
                    peripheral.onOutput(actual);
                }
            });
        }
        return actual;
    }

    @Override
    public long getAmount() {
        // M1 approximation — EnergyPeripheral exposes per-tick I/O, not stored
        // amounts directly. For consumers/generators this reports 0; for batteries
        // maxOutputThisTick() caps at MAX_TRANSFER, so it's a lower bound on stored
        // energy. External HUDs reading this will see a conservative but valid value.
        return peripheral.maxOutputThisTick();
    }

    @Override
    public long getCapacity() {
        // M1 approximation — same caveat as getAmount(). Reports the larger of the
        // two per-tick limits as a proxy for capacity; refined in a later milestone
        // when the battery BE exposes stored/max directly.
        return Math.max(peripheral.maxIntakeThisTick(), peripheral.maxOutputThisTick());
    }

    @Override
    public boolean supportsInsertion() {
        return peripheral.maxIntakeThisTick() > 0;
    }

    @Override
    public boolean supportsExtraction() {
        return peripheral.maxOutputThisTick() > 0;
    }
}
