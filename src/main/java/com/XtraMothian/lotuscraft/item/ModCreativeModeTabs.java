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
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.MOLLISOL_GRASS.get().asItem()))
                    .title(Component.translatable("creativetab.lotuscraft.lotuscraft_nature_tab"))
                    .displayItems((params, output) -> {

                        output.accept(new ItemStack(ModBlocks.MOLLISOL_GRASS.get()));
                        output.accept(new ItemStack(ModBlocks.ARIDISOL_GRASS.get()));
                        output.accept(new ItemStack(ModBlocks.ALFISOL_GRASS.get()));
                        output.accept(new ItemStack(ModBlocks.HISTOSOL_GRASS.get()));

                        output.accept(new ItemStack(ModBlocks.MOLLISOL.get()));
                        output.accept(new ItemStack(ModBlocks.ARIDISOL.get()));
                        output.accept(new ItemStack(ModBlocks.ALFISOL.get()));
                        output.accept(new ItemStack(ModBlocks.HISTOSOL.get()));

                        output.accept(new ItemStack(ModBlocks.DACITE.get()));
                        output.accept(new ItemStack(ModBlocks.RHYOLITE.get()));
                        output.accept(new ItemStack(ModBlocks.ANDESITE.get()));
                        output.accept(new ItemStack(ModBlocks.BASALT.get()));
                        output.accept(new ItemStack(ModBlocks.GRANITE.get()));
                        output.accept(new ItemStack(ModBlocks.DIORITE.get()));
                        output.accept(new ItemStack(ModBlocks.GABBRO.get()));
                        output.accept(new ItemStack(ModBlocks.PERIDOTITE.get()));
                        output.accept(new ItemStack(ModBlocks.GNEISS.get()));
                        output.accept(new ItemStack(ModBlocks.MARBLE.get()));
                        output.accept(new ItemStack(ModBlocks.SCHIST.get()));
                        output.accept(new ItemStack(ModBlocks.SLATE.get()));
                        output.accept(new ItemStack(ModBlocks.CHALK.get()));
                        output.accept(new ItemStack(ModBlocks.LIMESTONE.get()));
                        output.accept(new ItemStack(ModBlocks.DOLOMITE.get()));
                        output.accept(new ItemStack(ModBlocks.CHERT.get()));

                        output.accept(new ItemStack(ModBlocks.CLOVERS.get()));
                    })
                    .build());

    public static final Supplier<CreativeModeTab> LOTUSCRAFT_MATERIALS_TAB = CREATIVE_MODE_TAB.register("lotuscraft_materials_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.KNAPPED_FLINT.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LotusCraft.MOD_ID, "lotuscraft_nature_tab"))
                    .title(Component.translatable("creativetab.lotuscraft.lotuscraft_materials_tab"))
                    .displayItems((params, output) -> {

                        output.accept(new ItemStack(ModItems.TWIG.get()));
                        output.accept(new ItemStack(ModItems.KNAPPED_FLINT.get()));
                        output.accept(new ItemStack(ModItems.FLINT_KNIFE_HEAD.get()));
                        output.accept(new ItemStack(ModItems.FLINT_HATCHET_HEAD.get()));
                    })
                    .build());

    public static final Supplier<CreativeModeTab> LOTUSCRAFT_UTILITY_TAB = CREATIVE_MODE_TAB.register("lotuscraft_utility_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.FLINT_KNIFE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LotusCraft.MOD_ID, "lotuscraft_materials_tab"))
                    .title(Component.translatable("creativetab.lotuscraft.lotuscraft_utility_tab"))
                    .displayItems((params, output) -> {

                        output.accept(new ItemStack(ModItems.FLINT_KNIFE.get()));
                        output.accept(new ItemStack(ModItems.FLINT_HATCHET.get()));
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}