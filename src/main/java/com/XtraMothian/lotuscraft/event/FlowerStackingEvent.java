package com.XtraMothian.lotuscraft.event;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import com.XtraMothian.lotuscraft.block.custom.FlowerClusterBlock;
import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import com.XtraMothian.lotuscraft.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

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

        Level level =
                event.getLevel();

        BlockPos pos =
                event.getPos();

        BlockState state =
                level.getBlockState(pos);

        ItemStack heldItem =
                event.getItemStack();

        /*
         * We only care about block items.
         */
        if (!(heldItem.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Block heldBlock =
                blockItem.getBlock();

        /*
         * ========================================================
         * EXISTING FLOWER → CREATE CLUSTER
         * ========================================================
         */

        if (state.is(ModTags.Blocks.SMALL_FLOWER)
                && !state.is(ModTags.Blocks.FLOWER_STACKING_EXEMPT)) {

            /*
             * The held flower must be the same flower.
             */
            if (heldBlock != state.getBlock()) {
                return;
            }

            /*
             * ----------------------------------------------------
             * CLIENT SIDE
             * ----------------------------------------------------
             *
             * The client is responsible for the visible hand
             * animation.
             *
             * We do NOT modify the world or consume the item here.
             */
            if (level.isClientSide()) {

                event.getEntity().swing(
                        InteractionHand.MAIN_HAND
                );

                event.setCanceled(true);

                return;
            }

            /*
             * ----------------------------------------------------
             * SERVER SIDE
             * ----------------------------------------------------
             */

            /*
             * Remember the original flower before replacing it.
             */
            Block originalFlower =
                    state.getBlock();

            /*
             * Create a 2-flower cluster.
             */
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

            /*
             * Store the original flower in the BlockEntity.
             */
            if (level.getBlockEntity(pos)
                    instanceof FlowerClusterBlockEntity cluster) {

                cluster.setFlower(originalFlower);
            }

            /*
             * Consume the flower.
             */
            if (!event.getEntity().isCreative()) {
                heldItem.shrink(1);
            }

            /*
             * Play the original flower's placement sound.
             */
            playFlowerPlaceSound(
                    level,
                    pos,
                    originalFlower
            );

            /*
             * Tell the server that we handled the interaction.
             */
            event.setCanceled(true);

            return;
        }

        /*
         * ========================================================
         * EXISTING CLUSTER → ADD ANOTHER FLOWER
         * ========================================================
         */

        if (state.is(ModBlocks.FLOWER_CLUSTER.get())) {

            /*
             * We need the BlockEntity on both sides to determine
             * which flower this cluster contains.
             */
            if (!(level.getBlockEntity(pos)
                    instanceof FlowerClusterBlockEntity cluster)) {

                return;
            }

            Block clusterFlower =
                    cluster.getFlower();

            /*
             * A cluster without a stored flower cannot accept
             * additional flowers.
             */
            if (clusterFlower == null) {
                return;
            }

            /*
             * The held flower must match the cluster flower.
             */
            if (heldBlock != clusterFlower) {
                return;
            }

            int amount =
                    state.getValue(
                            FlowerClusterBlock.AMOUNT
                    );

            /*
             * Maximum of four flowers.
             */
            if (amount >= 4) {
                return;
            }

            /*
             * ----------------------------------------------------
             * CLIENT SIDE
             * ----------------------------------------------------
             *
             * Play the visible hand animation.
             */
            if (level.isClientSide()) {

                event.getEntity().swing(
                        InteractionHand.MAIN_HAND
                );

                event.setCanceled(true);

                return;
            }

            /*
             * ----------------------------------------------------
             * SERVER SIDE
             * ----------------------------------------------------
             */

            /*
             * Increase the cluster size.
             */
            level.setBlock(
                    pos,
                    state.setValue(
                            FlowerClusterBlock.AMOUNT,
                            amount + 1
                    ),
                    Block.UPDATE_ALL
            );

            /*
             * Consume the flower.
             */
            if (!event.getEntity().isCreative()) {
                heldItem.shrink(1);
            }

            /*
             * Play the base flower's placement sound.
             */
            playFlowerPlaceSound(
                    level,
                    pos,
                    clusterFlower
            );

            /*
             * Tell the server that we handled the interaction.
             */
            event.setCanceled(true);
        }
    }

    /*
     * ============================================================
     * FLOWER PLACEMENT SOUND
     * ============================================================
     */

    private static void playFlowerPlaceSound(
            Level level,
            BlockPos pos,
            Block flower
    ) {

        BlockState flowerState =
                flower.defaultBlockState();

        SoundType soundType =
                flowerState.getSoundType();

        /*
         * Placement volume.
         */
        float volume =
                (soundType.getVolume() + 1.0F) / 2.0F;

        /*
         * Placement pitch with vanilla-style variation.
         */
        float pitch =
                soundType.getPitch()
                        * 0.7F
                        + level.getRandom().nextFloat()
                        * 0.2F;

        level.playSound(
                null,
                pos,
                soundType.getPlaceSound(),
                SoundSource.BLOCKS,
                volume,
                pitch
        );
    }

    /*
     * ============================================================
     * PLAYER BREAK EVENT
     * ============================================================
     */

    @SubscribeEvent
    public static void onBlockBreak(
            BlockEvent.BreakEvent event
    ) {

        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos =
                event.getPos();

        BlockState state =
                event.getState();

        if (!state.is(ModBlocks.FLOWER_CLUSTER.get())) {
            return;
        }

        /*
         * FlowerClusterBlock.playerDestroy() handles the
         * actual flower drops.
         *
         * Nothing is dropped here.
         */
    }

    /*
     * ============================================================
     * EXPLOSION
     * ============================================================
     */

    @SubscribeEvent
    public static void onExplosionDetonate(
            ExplosionEvent.Detonate event
    ) {

        Level level =
                event.getLevel();

        if (level.isClientSide()) {
            return;
        }

        /*
         * Detonate gives us the blocks the explosion is going
         * to destroy while their BlockEntities still exist.
         */
        for (BlockPos pos :
                event.getAffectedBlocks()) {

            BlockState state =
                    level.getBlockState(pos);

            if (!state.is(ModBlocks.FLOWER_CLUSTER.get())) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(pos);

            if (!(blockEntity
                    instanceof FlowerClusterBlockEntity cluster)) {

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

            Block.popResource(
                    level,
                    pos,
                    new ItemStack(
                            flower.asItem(),
                            dropAmount
                    )
            );
        }
    }
}