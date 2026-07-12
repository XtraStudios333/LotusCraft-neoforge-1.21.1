package com.XtraMothian.lotuscraft.block.custom;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class ModBlockColors {

    public static void registerBlock(RegisterColorHandlersEvent.Block event) {

        event.register(
                (state, level, pos, tintIndex) -> {

                    if (level != null && pos != null) {
                        return BiomeColors.getAverageGrassColor(level, pos);
                    }

                    return FoliageColor.getDefaultColor();
                },

                ModBlocks.IVY.get()
        );
    }


    public static void registerItem(RegisterColorHandlersEvent.Item event) {

        event.register(
                (stack, tintIndex) -> FoliageColor.getDefaultColor(),
                ModBlocks.IVY.get().asItem()
        );
    }
}