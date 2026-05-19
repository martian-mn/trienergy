package com.trienergy.api.events;

import com.trienergy.api.NetworkSnapshot;

/**
 * Fired once per ACTIVE energy network per game tick, after distribution.
 * Carries totals — addon authors can compute aggregates for in-game HUDs.
 */
public record EnergyFlowEvent(
        NetworkSnapshot network,
        long sourcesEmitted,
        long consumersReceived,
        long storageChargedNet
) {}
