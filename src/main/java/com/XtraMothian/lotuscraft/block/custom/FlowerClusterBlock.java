package com.XtraMothian.lotuscraft.block.custom;

import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;

public class FlowerClusterBlock extends Block implements EntityBlock {

    public static final IntegerProperty AMOUNT =
            IntegerProperty.create("amount", 2, 4);

    public FlowerClusterBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AMOUNT, 2)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(AMOUNT);
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new FlowerClusterBlockEntity(pos, state);
    }

    /*
     * ============================================================
     * STORED FLOWER
     * ============================================================
     */

    public Block getStoredFlower(
            Level level,
            BlockPos pos
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return null;
        }

        return cluster.getFlower();
    }

    public SoundType getStoredFlowerSound(
            Level level,
            BlockPos pos
    ) {
        Block flower =
                getStoredFlower(level, pos);

        if (flower == null) {
            return this.defaultBlockState().getSoundType();
        }

        return flower.defaultBlockState().getSoundType();
    }

    /*
     * ============================================================
     * NORMAL PLAYER BREAKING
     * ============================================================
     */

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity,
            ItemStack tool
    ) {
        if (!level.isClientSide()) {

            if (!player.isCreative()) {

                dropClusterFlowers(
                        level,
                        pos,
                        state,
                        blockEntity,
                        1.0F
                );
            }
        }

        player.awardStat(
                net.minecraft.stats.Stats.BLOCK_MINED.get(this)
        );

        if (!player.isCreative()) {
            player.causeFoodExhaustion(0.005F);
        }
    }

    /*
     * ============================================================
     * EXPLOSIONS
     * ============================================================
     */

    @Override
    public void wasExploded(
            Level level,
            BlockPos pos,
            Explosion explosion
    ) {
        if (level.isClientSide()) {
            return;
        }

        BlockState state =
                level.getBlockState(pos);

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        dropClusterFlowers(
                level,
                pos,
                state,
                blockEntity,
                0.5F
        );
    }

    /*
     * ============================================================
     * NEIGHBOR UPDATE
     * ============================================================
     */

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (level instanceof Level actualLevel) {

            actualLevel.scheduleTick(
                    pos,
                    this,
                    1
            );
        }

        return super.updateShape(
                state,
                direction,
                neighborState,
                level,
                pos,
                neighborPos
        );
    }

    /*
     * ============================================================
     * NEIGHBOR CHANGED
     * ============================================================
     */

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        if (!level.isClientSide()) {

            level.scheduleTick(
                    pos,
                    this,
                    1
            );
        }

        super.neighborChanged(
                state,
                level,
                pos,
                neighborBlock,
                neighborPos,
                movedByPiston
        );
    }

    /*
     * ============================================================
     * BLOCK REMOVAL
     * ============================================================
     */

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean isMoving
    ) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide()) {

            boolean isFluidRemoval =
                    !level.getFluidState(pos).isEmpty();

            boolean isPistonRemoval =
                    isMoving;

            if (isFluidRemoval || isPistonRemoval) {

                BlockEntity blockEntity =
                        level.getBlockEntity(pos);

                if (blockEntity instanceof FlowerClusterBlockEntity cluster) {

                    Block flower =
                            cluster.getFlower();

                    if (flower != null) {

                        int amount =
                                state.getValue(AMOUNT);

                        popResource(
                                level,
                                pos,
                                new ItemStack(
                                        flower.asItem(),
                                        amount
                                )
                        );
                    }
                }
            }
        }

        super.onRemove(
                state,
                level,
                pos,
                newState,
                isMoving
        );
    }

    /*
     * ============================================================
     * PISTON PUSH
     * ============================================================
     */

    @Override
    public void onDestroyedByPushReaction(
            BlockState state,
            Level level,
            BlockPos pos,
            Direction pushDirection,
            FluidState fluid
    ) {
        if (level.isClientSide()) {
            return;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (blockEntity instanceof FlowerClusterBlockEntity cluster) {

            Block flower =
                    cluster.getFlower();

            if (flower != null) {

                int amount =
                        state.getValue(AMOUNT);

                popResource(
                        level,
                        pos,
                        new ItemStack(
                                flower.asItem(),
                                amount
                        )
                );
            }
        }

        level.removeBlock(
                pos,
                false
        );
    }

    /*
     * ============================================================
     * SURVIVAL CHECK
     * ============================================================
     */

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return;
        }

        Block flower =
                cluster.getFlower();

        if (flower == null) {
            return;
        }

        /*
         * WATER / FLUID
         */

        if (!level.getFluidState(pos).isEmpty()) {

            int amount =
                    state.getValue(AMOUNT);

            level.removeBlock(
                    pos,
                    false
            );

            popResource(
                    level,
                    pos,
                    new ItemStack(
                            flower.asItem(),
                            amount
                    )
            );

            return;
        }

        /*
         * ORIGINAL FLOWER SURVIVAL
         */

        BlockState flowerState =
                flower.defaultBlockState();

        if (!flowerState.canSurvive(level, pos)) {

            int amount =
                    state.getValue(AMOUNT);

            level.removeBlock(
                    pos,
                    false
            );

            popResource(
                    level,
                    pos,
                    new ItemStack(
                            flower.asItem(),
                            amount
                    )
            );
        }
    }

    /*
     * ============================================================
     * FLOWER DROP HELPER
     * ============================================================
     */

    private void dropClusterFlowers(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity,
            float multiplier
    ) {
        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return;
        }

        Block flower =
                cluster.getFlower();

        if (flower == null) {
            return;
        }

        int amount =
                state.getValue(AMOUNT);

        int dropAmount =
                Math.max(
                        1,
                        (int) Math.floor(
                                amount * multiplier
                        )
                );

        popResource(
                level,
                pos,
                new ItemStack(
                        flower.asItem(),
                        dropAmount
                )
        );
    }
}