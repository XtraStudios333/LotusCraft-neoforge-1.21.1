package com.XtraMothian.lotuscraft.block.entity;

import com.XtraMothian.lotuscraft.LotusCraft;
import com.XtraMothian.lotuscraft.block.ModBlocks;
import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    LotusCraft.MOD_ID
            );

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<FlowerClusterBlockEntity>
            > FLOWER_CLUSTER =
            BLOCK_ENTITIES.register(
                    "flower_cluster",
                    () -> BlockEntityType.Builder.of(
                            FlowerClusterBlockEntity::new,
                            ModBlocks.FLOWER_CLUSTER.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}