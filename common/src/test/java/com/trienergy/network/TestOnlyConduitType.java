package com.trienergy.network;

import com.trienergy.api.ConduitType;
import com.trienergy.api.Network;
import net.minecraft.resources.ResourceLocation;

public final class TestOnlyConduitType implements ConduitType {
    public static final TestOnlyConduitType INSTANCE = new TestOnlyConduitType();

    private TestOnlyConduitType() {}

    @Override public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("trienergy_test", "test");
    }

    @Override public void tick(Network network) {
        // No-op for tests
    }
}
