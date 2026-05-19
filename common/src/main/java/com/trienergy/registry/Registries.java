package com.trienergy.registry;

import com.trienergy.TriEnergy;
import com.trienergy.content.TestBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

// The local class is intentionally named `Registries` and does NOT import
// net.minecraft.core.registries.Registries — the vanilla class is referenced
// via its fully-qualified name below to avoid the name collision.
public final class Registries {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(TriEnergy.MOD_ID, net.minecraft.core.registries.Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(TriEnergy.MOD_ID, net.minecraft.core.registries.Registries.ITEM);

    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TriEnergy.MOD_ID, name));
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(TriEnergy.MOD_ID, name));
    }

    public static final RegistrySupplier<Block> TEST_BLOCK =
            BLOCKS.register("test_block", () -> new TestBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(1.5f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .setId(blockKey("test_block"))));

    public static final RegistrySupplier<Item> TEST_BLOCK_ITEM =
            ITEMS.register("test_block", () -> new BlockItem(TEST_BLOCK.get(),
                    new Item.Properties().setId(itemKey("test_block"))));

    private Registries() {}

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
