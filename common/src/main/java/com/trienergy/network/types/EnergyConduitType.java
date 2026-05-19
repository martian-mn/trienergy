package com.trienergy.network.types;

import com.trienergy.TriEnergy;
import com.trienergy.api.ConduitType;
import com.trienergy.api.EnergyPeripheral;
import com.trienergy.api.Network;
import com.trienergy.network.NetworkImpl;
import com.trienergy.network.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public final class EnergyConduitType implements ConduitType {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(TriEnergy.MOD_ID, "energy");
    public static final EnergyConduitType INSTANCE = new EnergyConduitType();

    private EnergyConduitType() {}

    @Override public ResourceLocation id() { return ID; }

    @Override
    public void tick(Network network) {
        if (!(network instanceof NetworkImpl impl)) return;
        Map<BlockPos, Node> nodes = impl.nodesMap();

        long directSupply = 0L;
        long storageSupply = 0L;
        long demand = 0L;

        for (BlockPos pos : impl.sources()) {
            EnergyPeripheral ep = energyAt(nodes, pos);
            if (ep != null) directSupply += ep.maxOutputThisTick();
        }
        for (BlockPos pos : impl.storage()) {
            EnergyPeripheral ep = energyAt(nodes, pos);
            if (ep != null) storageSupply += ep.maxOutputThisTick();
        }
        for (BlockPos pos : impl.consumers()) {
            EnergyPeripheral ep = energyAt(nodes, pos);
            if (ep != null) demand += ep.maxIntakeThisTick();
        }

        // Phase A — satisfy demand from direct supply first, then storage.
        long suppliedFromDirect = Math.min(directSupply, demand);
        long suppliedFromStorage = Math.max(0L, Math.min(demand - directSupply, storageSupply));

        drainProRata(impl.sources(), nodes, directSupply, suppliedFromDirect);
        drainProRata(impl.storage(), nodes, storageSupply, suppliedFromStorage);
        fillProRata(impl.consumers(), nodes, demand, suppliedFromDirect + suppliedFromStorage);

        // Phase B — store surplus from direct supply (not storage discharge).
        long surplus = Math.max(0L, directSupply - demand);
        if (surplus > 0) {
            long storageCapacity = 0L;
            for (BlockPos pos : impl.storage()) {
                EnergyPeripheral ep = energyAt(nodes, pos);
                if (ep != null) storageCapacity += ep.maxIntakeThisTick();
            }
            if (storageCapacity > 0) {
                long stored = Math.min(surplus, storageCapacity);
                for (BlockPos pos : impl.storage()) {
                    EnergyPeripheral ep = energyAt(nodes, pos);
                    if (ep == null) continue;
                    long share = (long) ((double) stored * ep.maxIntakeThisTick() / storageCapacity);
                    if (share > 0) ep.onIntake(share);
                }
            }
        }
    }

    private static EnergyPeripheral energyAt(Map<BlockPos, Node> nodes, BlockPos pos) {
        Node node = nodes.get(pos);
        if (node == null) return null;
        Object peripheral = node.peripheral();
        return peripheral instanceof EnergyPeripheral ep ? ep : null;
    }

    private static void drainProRata(Set<BlockPos> positions, Map<BlockPos, Node> nodes,
                                     long totalAvailable, long totalTaken) {
        if (totalAvailable == 0L || totalTaken == 0L) return;
        for (BlockPos pos : positions) {
            EnergyPeripheral ep = energyAt(nodes, pos);
            if (ep == null) continue;
            long share = (long) ((double) totalTaken * ep.maxOutputThisTick() / totalAvailable);
            if (share > 0) ep.onOutput(share);
        }
    }

    private static void fillProRata(Set<BlockPos> positions, Map<BlockPos, Node> nodes,
                                    long totalDemand, long totalGiven) {
        if (totalDemand == 0L || totalGiven == 0L) return;
        for (BlockPos pos : positions) {
            EnergyPeripheral ep = energyAt(nodes, pos);
            if (ep == null) continue;
            long share = (long) ((double) totalGiven * ep.maxIntakeThisTick() / totalDemand);
            if (share > 0) ep.onIntake(share);
        }
    }
}
