package com.trienergy.api.events;

import com.trienergy.api.NetworkSnapshot;

import java.util.List;

/**
 * Fired when a network's topology changes (place, break, merge, split, chunk load/unload).
 * Carries before-and-after snapshots so subscribers can diff.
 */
public record NetworkChangedEvent(
        List<NetworkSnapshot> before,
        List<NetworkSnapshot> after
) {}
