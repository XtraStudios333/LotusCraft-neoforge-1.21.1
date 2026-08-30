package com.XtraMothian.lotuscraft.event;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import com.XtraMothian.lotuscraft.block.custom.FlowerClusterBlock;
import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import com.XtraMothian.lotuscraft.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class FlowerStackingEvent {

    /*
     * ============================================================
     * FLOWER STACKING
     * ============================================================
     */

    @SubscribeEvent
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack heldItem = event.getItemStack();

        // We only care about block items.
        if (!(heldItem.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Block heldBlock = blockItem.getBlock();

        /*
         * ------------------------------------------------------------
         * EXISTING FLOWER → CREATE CLUSTER
         * ------------------------------------------------------------
         */

        if (state.is(ModTags.Blocks.SMALL_FLOWER)
                && !state.is(ModTags.Blocks.FLOWER_STACKING_EXEMPT)) {

            // The flower being held must be the same flower.
            if (heldBlock != state.getBlock()) {
                return;
            }

            if (level.isClientSide()) {
                return;
            }

            // Create a 2-flower cluster.
            BlockState clusterState =
                    ModBlocks.FLOWER_CLUSTER.get()
                            .defaultBlockState()
                            .setValue(
                                    FlowerClusterBlock.AMOUNT,
                                    2
                            );

            level.setBlock(
                    pos,
                    clusterState,
                    Block.UPDATE_ALL
            );

            // Remember which flower this cluster contains.
            if (level.getBlockEntity(pos)
                    instanceof FlowerClusterBlockEntity cluster) {

                cluster.setFlower(state.getBlock());
            }

            // Consume the flower used for stacking.
            if (!event.getEntity().isCreative()) {
                heldItem.shrink(1);
            }

            event.setCanceled(true);
            return;
        }

        /*
         * ------------------------------------------------------------
         * EXISTING CLUSTER → ADD ANOTHER FLOWER
         * ------------------------------------------------------------
         */

        if (state.is(ModBlocks.FLOWER_CLUSTER.get())) {

            if (level.isClientSide()) {
                return;
            }

            if (!(level.getBlockEntity(pos)
                    instanceof FlowerClusterBlockEntity cluster)) {
                return;
            }

            Block clusterFlower =
                    cluster.getFlower();

            // Safety check: a cluster without a flower
            // cannot be stacked.
            if (clusterFlower == null) {
                return;
            }

            // The held flower must be the same type.
            if (heldBlock != clusterFlower) {
                return;
            }

            int amount =
                    state.getValue(
                            FlowerClusterBlock.AMOUNT
                    );

            // Maximum of four flowers.
            if (amount >= 4) {
                return;
            }

            // Increase the cluster size.
            level.setBlock(
                    pos,
                    state.setValue(
                            FlowerClusterBlock.AMOUNT,
                            amount + 1
                    ),
                    Block.UPDATE_ALL
            );

            // Consume the additional flower.
            if (!event.getEntity().isCreative()) {
                heldItem.shrink(1);
            }

            event.setCanceled(true);
        }
    }


    /*
     * ============================================================
     * PLAYER BREAK EVENT
     * ============================================================
     *
     * This event fires while the cluster's BlockEntity still
     * exists.
     *
     * We are NOT dropping anything here.
     *
     * FlowerClusterBlock.playerDestroy() already handles the
     * actual flower drops.
     *
     * This event is intentionally here as a reliable point at
     * which we can later add special destruction handling if
     * needed.
     */

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {

        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (!state.is(ModBlocks.FLOWER_CLUSTER.get())) {
            return;
        }

        BlockEntity blockEntity =
                event.getLevel().getBlockEntity(pos);

        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return;
        }

        Block flower = cluster.getFlower();

        if (flower == null) {
            return;
        }

        /*
         * Nothing is dropped here.
         *
         * FlowerClusterBlock.playerDestroy() handles the actual
         * flower drops.
         */
    }

    @SubscribeEvent
    public static void onExplosionDetonate(
            ExplosionEvent.Detonate event
    ) {
        Level level = event.getLevel();

        if (level.isClientSide()) {
            return;
        }

        /*
         * Detonate gives us the blocks that the explosion is
         * going to destroy while their BlockEntities still
         * exist.
         */
        for (BlockPos pos : event.getAffectedBlocks()) {

            BlockState state =
                    level.getBlockState(pos);

            if (!state.is(ModBlocks.FLOWER_CLUSTER.get())) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(pos);

            if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
                continue;
            }

            Block flower =
                    cluster.getFlower();

            if (flower == null) {
                continue;
            }

            int amount =
                    state.getValue(
                            FlowerClusterBlock.AMOUNT
                    );

            /*
             * Explosions return at least half of the cluster.
             *
             * 2 -> 1
             * 3 -> 1
             * 4 -> 2
             */
            int dropAmount =
                    Math.max(
                            1,
                            amount / 2
                    );

            ItemStack flowerStack =
                    new ItemStack(
                            flower.asItem(),
                            dropAmount
                    );

            Block.popResource(
                    level,
                    pos,
                    flowerStack
            );
        }
    }
}