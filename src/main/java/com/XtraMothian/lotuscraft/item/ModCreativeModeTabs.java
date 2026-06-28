package com.XtraMothian.lotuscraft.item;

import com.XtraMothian.lotuscraft.LotusCraft;
import com.XtraMothian.lotuscraft.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LotusCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> LOTUSCRAFT_NATURE_TAB = CREATIVE_MODE_TAB.register("lotuscraft_nature_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MOLLISOL.get()))
                    .title(Component.translatable("creativetab.lotuscraft.lotuscraft_nature_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.MOLLISOL);
                        output.accept(ModBlocks.ARIDISOL);
                        output.accept(ModBlocks.ALFISOL);
                        output.accept(ModBlocks.HISTOSOL);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> LOTUSCRAFT_MATERIALS_TAB = CREATIVE_MODE_TAB.register("lotuscraft_materials_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.KNAPPED_FLINT.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LotusCraft.MOD_ID, "lotuscraft_nature_tab"))
                    .title(Component.translatable("creativetab.lotuscraft.lotuscraft_materials_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.TWIG);
                        output.accept(ModItems.KNAPPED_FLINT);
                        output.accept(ModItems.FLINT_KNIFE_HEAD);
                        output.accept(ModItems.FLINT_HATCHET_HEAD);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> LOTUSCRAFT_UTILITY_TAB = CREATIVE_MODE_TAB.register("lotuscraft_utility_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.FLINT_KNIFE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LotusCraft.MOD_ID, "lotuscraft_materials_tab"))
                    .title(Component.translatable("creativetab.lotuscraft.lotuscraft_utility_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.FLINT_KNIFE);
                        output.accept(ModItems.FLINT_HATCHET);
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
