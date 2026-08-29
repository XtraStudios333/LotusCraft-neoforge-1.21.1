package com.XtraMothian.lotuscraft.util;

import com.XtraMothian.lotuscraft.LotusCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> NEEDS_FLINT_TOOL =
                create("needs_flint_tool");

        public static final TagKey<Block> INCORRECT_FOR_FLINT_TOOL =
                create("incorrect_for_flint_tool");

        public static final TagKey<Block> SMALL_FLOWER =
                create("small_flower");

        public static final TagKey<Block> FLOWER_STACKING_EXEMPT =
                create("flower_stacking_exempt");

        private static TagKey<Block> create(String name) {
            return TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(
                            LotusCraft.MOD_ID,
                            name
                    )
            );
        }
    }
}