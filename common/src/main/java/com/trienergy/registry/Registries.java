package com.trienergy.registry;

import com.trienergy.TriEnergy;
import com.trienergy.content.BasicBatteryBlock;
import com.trienergy.content.BasicBatteryBlockEntity;
import com.trienergy.content.EnergyConduitBlock;
import com.trienergy.content.EnergyConduitBlockEntity;
import com.trienergy.content.SimpleGeneratorBlock;
import com.trienergy.content.SimpleGeneratorBlockEntity;
import com.trienergy.content.TestBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(TriEnergy.MOD_ID, net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE);

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

    public static final RegistrySupplier<Block> ENERGY_CONDUIT_BLOCK =
            BLOCKS.register("energy_conduit", () -> new EnergyConduitBlock(
                    EnergyConduitBlock.defaultProperties()
                            .setId(blockKey("energy_conduit"))));

    public static final RegistrySupplier<Item> ENERGY_CONDUIT_ITEM =
            ITEMS.register("energy_conduit", () -> new BlockItem(
                    ENERGY_CONDUIT_BLOCK.get(),
                    new Item.Properties().setId(itemKey("energy_conduit"))));

    public static final RegistrySupplier<BlockEntityType<EnergyConduitBlockEntity>> ENERGY_CONDUIT_BE =
            BLOCK_ENTITY_TYPES.register("energy_conduit", () ->
                    new BlockEntityType<>(
                            EnergyConduitBlockEntity::new,
                            java.util.Set.of(ENERGY_CONDUIT_BLOCK.get())));

    public static final RegistrySupplier<Block> SIMPLE_GENERATOR_BLOCK =
            BLOCKS.register("simple_generator", () -> new SimpleGeneratorBlock(
                    SimpleGeneratorBlock.defaultProperties()
                            .setId(blockKey("simple_generator"))));

    public static final RegistrySupplier<Item> SIMPLE_GENERATOR_ITEM =
            ITEMS.register("simple_generator", () -> new BlockItem(
                    SIMPLE_GENERATOR_BLOCK.get(),
                    new Item.Properties().setId(itemKey("simple_generator"))));

    public static final RegistrySupplier<BlockEntityType<SimpleGeneratorBlockEntity>> SIMPLE_GENERATOR_BE =
            BLOCK_ENTITY_TYPES.register("simple_generator", () ->
                    new BlockEntityType<>(
                            SimpleGeneratorBlockEntity::new,
                            java.util.Set.of(SIMPLE_GENERATOR_BLOCK.get())));

    public static final RegistrySupplier<Block> BASIC_BATTERY_BLOCK =
            BLOCKS.register("basic_battery", () -> new BasicBatteryBlock(
                    BasicBatteryBlock.defaultProperties()
                            .setId(blockKey("basic_battery"))));

    public static final RegistrySupplier<Item> BASIC_BATTERY_ITEM =
            ITEMS.register("basic_battery", () -> new BlockItem(
                    BASIC_BATTERY_BLOCK.get(),
                    new Item.Properties().setId(itemKey("basic_battery"))));

    public static final RegistrySupplier<BlockEntityType<BasicBatteryBlockEntity>> BASIC_BATTERY_BE =
            BLOCK_ENTITY_TYPES.register("basic_battery", () ->
                    new BlockEntityType<>(
                            BasicBatteryBlockEntity::new,
                            java.util.Set.of(BASIC_BATTERY_BLOCK.get())));

    private Registries() {}

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
    }
}
