package com.XtraMothian.lotuscraft.item;

import com.XtraMothian.lotuscraft.LotusCraft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LotusCraft.MOD_ID);

    // Crafting Items - Basic
    public static final DeferredItem<Item> KNAPPED_FLINT = ITEMS.register("knapped_flint",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TWIG = ITEMS.register("twig",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLINT_KNIFE_HEAD = ITEMS.register("flint_knife_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLINT_HATCHET_HEAD = ITEMS.register("flint_hatchet_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.register("flint_knife",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLINT_HATCHET = ITEMS.register("flint_hatchet",
            () -> new Item(new Item.Properties()));


    // Edited Vanilla Items

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
