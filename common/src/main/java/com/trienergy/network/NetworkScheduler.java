package com.trienergy.network;

import com.trienergy.api.EnergyPeripheral;
import com.trienergy.api.NetworkState;
import net.minecraft.core.BlockPos;

public final class NetworkScheduler {
    public static final long POLL_INTERVAL_TICKS = 100L;

    private final NetworkRegistry registry;
    private long failsafePollNextTick = POLL_INTERVAL_TICKS;

    public NetworkScheduler(NetworkRegistry registry) {
        this.registry = registry;
    }

    /** Drive one game tick. {@code gameTime} is the world's game time in ticks. */
    public void tick(long gameTime) {
        // Failsafe poll: every POLL_INTERVAL_TICKS, re-evaluate every IDLE network's state.
        if (gameTime >= failsafePollNextTick) {
            for (NetworkImpl net : registry.allNetworks()) {
                if (net.state() == NetworkState.IDLE) {
                    reevaluateState(net);
                }
            }
            failsafePollNextTick = gameTime + POLL_INTERVAL_TICKS;
        }

        // Per-tick: re-evaluate state, then tick ACTIVE networks, then re-evaluate.
        // Snapshot the network list because conduit-type ticks may trigger merges/splits
        // (not in M1 energy, but a future ConduitType might).
        for (NetworkImpl net : registry.allNetworks().toArray(new NetworkImpl[0])) {
            if (net.state() == NetworkState.SUSPENDED) continue;
            reevaluateState(net);
            if (net.state() == NetworkState.ACTIVE) {
                net.conduitType().tick(net);
                net.setLastTickGameTime(gameTime);
                reevaluateState(net);
            }
        }
    }

    private void reevaluateState(NetworkImpl net) {
        if (net.state() == NetworkState.SUSPENDED) return;

        boolean hasSupply = false;
        boolean hasDemand = false;

        for (BlockPos pos : net.sources()) {
            EnergyPeripheral ep = energyAt(net, pos);
            if (ep != null && ep.maxOutputThisTick() > 0) {
                hasSupply = true;
                break;
            }
        }
        if (!hasSupply) {
            for (BlockPos pos : net.storage()) {
                EnergyPeripheral ep = energyAt(net, pos);
                if (ep != null && ep.maxOutputThisTick() > 0) {
                    hasSupply = true;
                    break;
                }
            }
        }
        for (BlockPos pos : net.consumers()) {
            EnergyPeripheral ep = energyAt(net, pos);
            if (ep != null && ep.maxIntakeThisTick() > 0) {
                hasDemand = true;
                break;
            }
        }
        if (!hasDemand) {
            for (BlockPos pos : net.storage()) {
                EnergyPeripheral ep = energyAt(net, pos);
                if (ep != null && ep.maxIntakeThisTick() > 0) {
                    hasDemand = true;
                    break;
                }
            }
        }

        net.setState(hasSupply && hasDemand ? NetworkState.ACTIVE : NetworkState.IDLE);
    }

    private static EnergyPeripheral energyAt(NetworkImpl net, BlockPos pos) {
        Node node = net.nodesMap().get(pos);
        if (node == null) return null;
        Object peripheral = node.peripheral();
        return peripheral instanceof EnergyPeripheral ep ? ep : null;
    }
}
