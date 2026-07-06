package com.XtraMothian.lotuscraft.item;

import com.XtraMothian.lotuscraft.LotusCraft;
import com.XtraMothian.lotuscraft.util.ModTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {

    public static final Tier FLINT = new SimpleTier(
            ModTags.Blocks.INCORRECT_FOR_FLINT_TOOL,
            80,
            2.5F,
            0.0F,
            5,
            () -> Ingredient.of(ModItems.KNAPPED_FLINT.get())
    );
}