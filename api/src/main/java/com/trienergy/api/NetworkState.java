package com.trienergy.api;

/**
 * Network operational state.
 */
public enum NetworkState {
    /** Network has at least one source with output AND at least one consumer or storage with demand. Ticks every game tick. */
    ACTIVE,
    /** Network has no current supply or no current demand. Does not tick. Resumes on event or failsafe poll. */
    IDLE,
    /** Network's chunks are all unloaded. State persisted to NBT. */
    SUSPENDED
}
