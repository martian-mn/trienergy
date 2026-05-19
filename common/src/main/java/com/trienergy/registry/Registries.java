package com.trienergy.registry;

import com.trienergy.TriEnergy;
import com.trienergy.content.TestBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// The local class is intentionally named `Registries` and does NOT import
// net.minecraft.core.registries.Registries — that vanilla class is referenced
// via its fully-qualified name below to avoid the name collision.
public final class Registries {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(TriEnergy.MOD_ID, net.minecraft.core.registries.Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(TriEnergy.MOD_ID, net.minecraft.core.registries.Registries.ITEM);

    public static final RegistrySupplier<Block> TEST_BLOCK =
            BLOCKS.register("test_block", TestBlock::new);
    public static final RegistrySupplier<Item> TEST_BLOCK_ITEM =
            ITEMS.register("test_block", () -> new BlockItem(TEST_BLOCK.get(), new Item.Properties()));

    private Registries() {}

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
