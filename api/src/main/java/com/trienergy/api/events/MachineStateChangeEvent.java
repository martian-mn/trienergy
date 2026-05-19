package com.trienergy.api.events;

import com.trienergy.api.MachinePeripheral;

/**
 * Fired by a {@link MachinePeripheral} when its supply / demand state changes.
 * Drives the network's ACTIVE / IDLE transitions.
 */
public record MachineStateChangeEvent(MachinePeripheral peripheral) {}
