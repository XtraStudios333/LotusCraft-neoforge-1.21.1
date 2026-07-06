package com.XtraMothian.lotuscraft.block;

import com.XtraMothian.lotuscraft.LotusCraft;
import com.XtraMothian.lotuscraft.block.custom.GrassSoilBlock;
import com.XtraMothian.lotuscraft.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LotusCraft.MOD_ID);

    //==================================================
    // Soil Blocks
    //==================================================

    public static final DeferredBlock<Block> MOLLISOL =
            registerBlock("mollisol",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));

    public static final DeferredBlock<Block> ARIDISOL =
            registerBlock("aridisol",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));

    public static final DeferredBlock<Block> ALFISOL =
            registerBlock("alfisol",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));

    public static final DeferredBlock<Block> HISTOSOL =
            registerBlock("histosol",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));

    //==================================================
    // Grass Variants
    //==================================================

    public static final DeferredBlock<Block> MOLLISOL_GRASS =
            registerBlock("mollisol_grass",
                    () -> new GrassSoilBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));

    public static final DeferredBlock<Block> ARIDISOL_GRASS =
            registerBlock("aridisol_grass",
                    () -> new GrassSoilBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));

    public static final DeferredBlock<Block> ALFISOL_GRASS =
            registerBlock("alfisol_grass",
                    () -> new GrassSoilBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));

    public static final DeferredBlock<Block> HISTOSOL_GRASS =
            registerBlock("histosol_grass",
                    () -> new GrassSoilBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));

    //==================================================
    // Lookup Helpers
    //==================================================

    public static BlockState getGrassVariant(BlockState soil) {

        if (soil.is(MOLLISOL.get())) {
            return MOLLISOL_GRASS.get().defaultBlockState();
        }

        if (soil.is(ARIDISOL.get())) {
            return ARIDISOL_GRASS.get().defaultBlockState();
        }

        if (soil.is(ALFISOL.get())) {
            return ALFISOL_GRASS.get().defaultBlockState();
        }

        if (soil.is(HISTOSOL.get())) {
            return HISTOSOL_GRASS.get().defaultBlockState();
        }

        return null;
    }

    public static BlockState getSoilVariant(BlockState grass) {

        if (grass.is(MOLLISOL_GRASS.get())) {
            return MOLLISOL.get().defaultBlockState();
        }

        if (grass.is(ARIDISOL_GRASS.get())) {
            return ARIDISOL.get().defaultBlockState();
        }

        if (grass.is(ALFISOL_GRASS.get())) {
            return ALFISOL.get().defaultBlockState();
        }

        if (grass.is(HISTOSOL_GRASS.get())) {
            return HISTOSOL.get().defaultBlockState();
        }

        return null;
    }

    //==================================================
    // Registration
    //==================================================

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {

        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {

        ModItems.ITEMS.register(name,
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}