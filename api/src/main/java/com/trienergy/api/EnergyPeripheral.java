package com.trienergy.api;

/**
 * Energy-specific {@link MachinePeripheral}. Provides the per-tick I/O methods
 * the energy {@code ConduitType} uses during distribution.
 */
public interface EnergyPeripheral extends MachinePeripheral {
    /**
     * Maximum FE this peripheral can emit this tick (sources, batteries that can discharge).
     */
    long maxOutputThisTick();

    /**
     * Maximum FE this peripheral wants this tick (consumers, batteries that can charge).
     */
    long maxIntakeThisTick();

    /**
     * Apply the actual delivered output. Called by the engine after distribution.
     */
    void onOutput(long amount);

    /**
     * Apply the actual delivered intake.
     */
    void onIntake(long amount);
}
