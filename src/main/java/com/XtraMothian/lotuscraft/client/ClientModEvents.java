package com.XtraMothian.lotuscraft.client;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class ClientModEvents {

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {

        event.register(
                (BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {

                    if (level == null || pos == null) {
                        return FoliageColor.getDefaultColor();
                    }

                    return BiomeColors.getAverageGrassColor(level, pos);
                },

                ModBlocks.MOLLISOL_GRASS.get(),
                ModBlocks.ARIDISOL_GRASS.get(),
                ModBlocks.ALFISOL_GRASS.get(),
                ModBlocks.HISTOSOL_GRASS.get(),

                ModBlocks.IVY.get()
        );
    }


    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {

        event.register(
                (ItemStack stack, int tintIndex) -> FoliageColor.getDefaultColor(),

                ModBlocks.MOLLISOL_GRASS.get(),
                ModBlocks.ARIDISOL_GRASS.get(),
                ModBlocks.ALFISOL_GRASS.get(),
                ModBlocks.HISTOSOL_GRASS.get()

        );
    }
}