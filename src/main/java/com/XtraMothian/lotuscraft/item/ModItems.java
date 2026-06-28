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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
