package com.XtraMothian.lotuscraft.client;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.BiomeColors;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class ClientEvents {

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {

        event.register(
                (BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {

                    if (level == null || pos == null) {
                        return 0x00FF00; // fallback grass green
                    }

                    return BiomeColors.getAverageGrassColor(level, pos);
                },

                ModBlocks.MOLLISOL_GRASS.get(),
                ModBlocks.ARIDISOL_GRASS.get(),
                ModBlocks.ALFISOL_GRASS.get(),
                ModBlocks.HISTOSOL_GRASS.get()
        );
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {

        event.register(
                (ItemStack stack, int tintIndex) -> 0x00FF00,

                ModBlocks.MOLLISOL_GRASS.get(),
                ModBlocks.ARIDISOL_GRASS.get(),
                ModBlocks.ALFISOL_GRASS.get(),
                ModBlocks.HISTOSOL_GRASS.get()
        );
    }
}