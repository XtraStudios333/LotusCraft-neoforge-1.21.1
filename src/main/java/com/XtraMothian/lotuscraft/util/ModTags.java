package com.XtraMothian.lotuscraft.util;

import com.XtraMothian.lotuscraft.LotusCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> NEEDS_FLINT_TOOL = create("needs_flint_tool");
        public static final TagKey<Block> INCORRECT_FOR_FLINT_TOOL = create("incorrect_for_flint_tool");

        private static TagKey<Block> create(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(LotusCraft.MOD_ID, name));
        }
    }

    public static class Items {

        private static TagKey<Item> create(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(LotusCraft.MOD_ID, name));
        }
    }
}