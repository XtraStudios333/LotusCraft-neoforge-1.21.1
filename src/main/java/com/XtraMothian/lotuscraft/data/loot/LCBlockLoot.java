package com.XtraMothian.lotuscraft.data.loot;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.data.loot.BlockLootSubProvider;

import java.util.Collections;
import java.util.Set;

public class LCBlockLoot extends BlockLootSubProvider {

    public LCBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {

        // Bare soils
        dropSelf(ModBlocks.MOLLISOL.get());
        dropSelf(ModBlocks.ARIDISOL.get());
        dropSelf(ModBlocks.ALFISOL.get());
        dropSelf(ModBlocks.HISTOSOL.get());

        // Grass soils
        dropSelf(ModBlocks.MOLLISOL_GRASS.get());
        dropSelf(ModBlocks.ARIDISOL_GRASS.get());
        dropSelf(ModBlocks.ALFISOL_GRASS.get());
        dropSelf(ModBlocks.HISTOSOL_GRASS.get());

        // Add future blocks here...
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries()
                .stream()
                .map(holder -> (Block) holder.value())
                .toList();
    }
}