package com.XtraMothian.lotuscraft.block.custom;

import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.extensions.IBlockExtension;


/**
 * FlowerClusterBlock
 *
 * Represents a cluster containing 2-4 copies of the same flower.
 *
 * The actual flower type is stored inside FlowerClusterBlockEntity.
 *
 * Explosion handling is deliberately done through NeoForge's
 * IBlockExtension explosion hooks.
 */
public class FlowerClusterBlock extends Block
        implements EntityBlock, IBlockExtension {

    /*
     * ============================================================
     * BLOCK STATE
     * ============================================================
     */

    public static final IntegerProperty AMOUNT =
            IntegerProperty.create("amount", 2, 4);


    /*
     * ============================================================
     * CONSTRUCTOR
     * ============================================================
     */

    public FlowerClusterBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AMOUNT, 2)
        );
    }


    /*
     * ============================================================
     * BLOCK STATE DEFINITION
     * ============================================================
     */

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(AMOUNT);
    }


    /*
     * ============================================================
     * BLOCK ENTITY
     * ============================================================
     */

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new FlowerClusterBlockEntity(pos, state);
    }


    /*
     * ============================================================
     * DIRECT CLUSTER INTERACTION
     * ============================================================
     *
     * Right-clicking an existing cluster with the flower that
     * belongs to that cluster:
     *
     *     2 -> 3
     *     3 -> 4
     *
     * A full cluster cannot be increased.
     */

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!state.is(this)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Block storedFlower =
                cluster.getFlower();

        if (storedFlower == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.is(storedFlower.asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int amount =
                state.getValue(AMOUNT);

        if (amount >= 4) {
            return ItemInteractionResult.FAIL;
        }

        /*
         * Client only reports success.
         *
         * The server performs the actual state change and
         * item consumption.
         */
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        level.setBlock(
                pos,
                state.setValue(
                        AMOUNT,
                        amount + 1
                ),
                Block.UPDATE_ALL
        );

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return ItemInteractionResult.SUCCESS;
    }


    /*
     * ============================================================
     * PICK BLOCK / MIDDLE CLICK
     * ============================================================
     */

    @Override
    public ItemStack getCloneItemStack(
            BlockState state,
            HitResult target,
            LevelReader level,
            BlockPos pos,
            Player player
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return ItemStack.EMPTY;
        }

        Block flower =
                cluster.getFlower();

        if (flower == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(
                flower.asItem()
        );
    }


    /*
     * ============================================================
     * REPLACEMENT PROTECTION
     * ============================================================
     */

    @Override
    protected boolean canBeReplaced(
            BlockState state,
            BlockPlaceContext context
    ) {
        if (state.is(this)) {
            return false;
        }

        return super.canBeReplaced(
                state,
                context
        );
    }


    /*
     * ============================================================
     * PLACEMENT STATE
     * ============================================================
     */

    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return this.defaultBlockState()
                .setValue(AMOUNT, 2);
    }


    /*
     * ============================================================
     * STORE FLOWER ON PLACEMENT
     * ============================================================
     */

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(
                level,
                pos,
                state,
                placer,
                stack
        );

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return;
        }

        if (stack.getItem() instanceof BlockItem blockItem) {
            cluster.setFlower(
                    blockItem.getBlock()
            );

            cluster.setChanged();
        }
    }


    /*
     * ============================================================
     * STORED FLOWER ACCESS
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


    /*
     * ============================================================
     * HITBOX
     * ============================================================
     */

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        int amount =
                state.getValue(AMOUNT);

        RandomSource random =
                RandomSource.create(pos.asLong());

        int rotation =
                random.nextInt(4);

        VoxelShape result =
                Shapes.empty();

        switch (amount) {

            case 2 -> {
                result = Shapes.or(
                        result,
                        createFlowerShape(
                                -0.24F,
                                -0.24F,
                                rotation,
                                random
                        )
                );

                result = Shapes.or(
                        result,
                        createFlowerShape(
                                0.24F,
                                0.24F,
                                rotation,
                                random
                        )
                );
            }

            case 3 -> {
                result = Shapes.or(
                        result,
                        createFlowerShape(
                                0.0F,
                                -0.24F,
                                rotation,
                                random
                        )
                );

                result = Shapes.or(
                        result,
                        createFlowerShape(
                                -0.24F,
                                0.24F,
                                rotation,
                                random
                        )
                );

                result = Shapes.or(
                        result,
                        createFlowerShape(
                                0.24F,
                                0.24F,
                                rotation,
                                random
                        )
                );
            }

            case 4 -> {
                result = Shapes.or(
                        result,
                        createFlowerShape(
                                -0.24F,
                                -0.24F,
                                rotation,
                                random
                        )
                );

                result = Shapes.or(
                        result,
                        createFlowerShape(
                                0.24F,
                                -0.24F,
                                rotation,
                                random
                        )
                );

                result = Shapes.or(
                        result,
                        createFlowerShape(
                                -0.24F,
                                0.24F,
                                rotation,
                                random
                        )
                );

                result = Shapes.or(
                        result,
                        createFlowerShape(
                                0.24F,
                                0.24F,
                                rotation,
                                random
                        )
                );
            }
        }

        return result;
    }


    /*
     * ============================================================
     * CREATE INDIVIDUAL FLOWER HITBOX
     * ============================================================
     */

    private VoxelShape createFlowerShape(
            float baseX,
            float baseZ,
            int rotation,
            RandomSource random
    ) {
        float randomX =
                (random.nextFloat() * 0.30F) - 0.15F;

        float randomZ =
                (random.nextFloat() * 0.30F) - 0.15F;

        float x =
                baseX + randomX;

        float z =
                baseZ + randomZ;

        float rotatedX;
        float rotatedZ;

        switch (rotation) {

            case 1 -> {
                rotatedX = z;
                rotatedZ = -x;
            }

            case 2 -> {
                rotatedX = -x;
                rotatedZ = -z;
            }

            case 3 -> {
                rotatedX = -z;
                rotatedZ = x;
            }

            default -> {
                rotatedX = x;
                rotatedZ = z;
            }
        }

        double centerX =
                0.5D + rotatedX;

        double centerZ =
                0.5D + rotatedZ;

        double halfSize =
                3.0D / 16.0D;

        return Block.box(
                (centerX - halfSize) * 16.0D,
                0.0D,
                (centerZ - halfSize) * 16.0D,
                (centerX + halfSize) * 16.0D,
                10.0D,
                (centerZ + halfSize) * 16.0D
        );
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
        if (!level.isClientSide() && !player.isCreative()) {

            dropClusterFlowers(
                    level,
                    pos,
                    state,
                    blockEntity,
                    1.0F
            );
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
     * EXPLOSION DROP CONTROL
     * ============================================================
     *
     * THIS IS THE IMPORTANT PART.
     *
     * NeoForge's explosion system has two separate concepts:
     *
     * 1. Should this block's normal explosion loot be dropped?
     * 2. What should happen when the explosion actually destroys
     *    the block?
     *
     * If we leave canDropFromExplosion() alone, the default
     * explosion drop mechanism can create an item independently
     * of our custom BlockEntity-aware drop code.
     *
     * That is exactly what causes the "item drops but the block
     * remains" behaviour with Wind Charges / TRIGGER_BLOCK.
     *
     * We therefore disable the generic explosion loot path.
     *
     * Our onBlockExploded() method below is the ONLY place where
     * destructive explosions create cluster drops.
     */

    @Override
    public boolean canDropFromExplosion(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Explosion explosion
    ) {
        return false;
    }


    /*
     * ============================================================
     * EXPLOSION HANDLING
     * ============================================================
     *
     * NeoForge 21.1.x provides this through IBlockExtension.
     *
     * The important property of this callback is that the
     * BlockEntity can be queried BEFORE we remove the block.
     *
     * Wind Charges use TRIGGER_BLOCK.
     *
     * TRIGGER_BLOCK:
     *
     *     -> do NOT remove the cluster
     *     -> do NOT drop the cluster
     *
     * DESTROY:
     *
     *     -> capture BlockEntity data
     *     -> remove the block
     *     -> manually drop the desired amount
     *
     * DESTROY_WITH_DECAY:
     *
     *     -> same handling here
     *
     * KEEP:
     *
     *     -> nothing happens
     */

    @Override
    public void onBlockExploded(
            BlockState state,
            Level level,
            BlockPos pos,
            Explosion explosion
    ) {
        Explosion.BlockInteraction interaction =
                explosion.getBlockInteraction();

        // Only handle explosions that actually destroy blocks.
        if (interaction != Explosion.BlockInteraction.DESTROY
                && interaction != Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        Block flower = null;

        if (blockEntity instanceof FlowerClusterBlockEntity cluster) {
            flower = cluster.getFlower();
        }

        int amount = state.getValue(AMOUNT);

        // Remove the cluster.
        level.setBlock(
                pos,
                Blocks.AIR.defaultBlockState(),
                Block.UPDATE_ALL
        );

        if (flower == null) {
            return;
        }

        int dropAmount = Math.max(
                1,
                (int) Math.floor(amount * 0.5F)
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
     *
     * This is intentionally NOT used for explosions.
     *
     * Explosion drops are handled exclusively by
     * onBlockExploded().
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


            /*
             * Only handle removals that are genuinely caused by
             * fluid replacement or piston destruction.
             *
             * Explosion removal is deliberately NOT handled here,
             * preventing a second drop.
             */
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
         * --------------------------------------------------------
         * FLUID CHECK
         * --------------------------------------------------------
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
         * --------------------------------------------------------
         * ORIGINAL FLOWER SURVIVAL
         * --------------------------------------------------------
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