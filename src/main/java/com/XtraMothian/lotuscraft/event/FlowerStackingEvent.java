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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class FlowerStackingEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

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
            BlockState clusterState = ModBlocks.FLOWER_CLUSTER.get()
                    .defaultBlockState()
                    .setValue(FlowerClusterBlock.AMOUNT, 2);

            level.setBlock(
                    pos,
                    clusterState,
                    Block.UPDATE_ALL
            );

            // Remember which flower this cluster contains.
            if (level.getBlockEntity(pos) instanceof FlowerClusterBlockEntity cluster) {
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

            if (!(level.getBlockEntity(pos) instanceof FlowerClusterBlockEntity cluster)) {
                return;
            }

            Block clusterFlower = cluster.getFlower();

            // Safety check: a cluster without a flower cannot be stacked.
            if (clusterFlower == null) {
                return;
            }

            // The held flower must be the same type as the cluster.
            if (heldBlock != clusterFlower) {
                return;
            }

            int amount = state.getValue(FlowerClusterBlock.AMOUNT);

            // A cluster can contain a maximum of four flowers.
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
}