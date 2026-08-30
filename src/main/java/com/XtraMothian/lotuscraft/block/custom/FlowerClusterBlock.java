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


/**
 * FlowerClusterBlock
 *
 * Represents a cluster containing 2-4 copies of the same flower.
 *
 * The actual flower type is stored inside FlowerClusterBlockEntity.
 *
 * This block is intentionally NOT an event subscriber.
 *
 * Direct interaction with an existing cluster is handled by
 * useItemOn().
 */
public class FlowerClusterBlock extends Block implements EntityBlock {

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
        /*
         * Make absolutely sure this is our block.
         */
        if (!state.is(this)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }


        /*
         * Find the cluster BlockEntity.
         */
        BlockEntity blockEntity =
                level.getBlockEntity(pos);


        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }


        /*
         * Determine which flower this cluster represents.
         */
        Block storedFlower =
                cluster.getFlower();


        if (storedFlower == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }


        /*
         * The held item must be the exact flower item represented
         * by this cluster.
         */
        if (!stack.is(storedFlower.asItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }


        /*
         * Current cluster size.
         */
        int amount =
                state.getValue(AMOUNT);


        /*
         * Four is the maximum.
         */
        if (amount >= 4) {
            return ItemInteractionResult.FAIL;
        }


        /*
         * Client:
         *
         * Report success but do not modify the world or consume
         * the item.
         */
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }


        /*
         * Server:
         *
         * Increase the cluster by exactly one.
         */
        level.setBlock(
                pos,
                state.setValue(
                        AMOUNT,
                        amount + 1
                ),
                Block.UPDATE_ALL
        );


        /*
         * Consume one flower unless the player is in Creative.
         */
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }


        return ItemInteractionResult.SUCCESS;
    }


    /*
     * ============================================================
     * PICK BLOCK / MIDDLE CLICK
     * ============================================================
     *
     * When the player middle-clicks this cluster, Minecraft asks
     * the block which ItemStack should be picked.
     *
     * Normally this would return the FlowerClusterBlock item.
     *
     * Instead, return the actual flower represented by the
     * FlowerClusterBlockEntity.
     *
     * This makes middle-clicking:
     *
     *     Flower Cluster
     *            |
     *            v
     *     Corresponding Flower
     *
     * rather than:
     *
     *     Flower Cluster
     *            |
     *            v
     *     Flower Cluster Item
     *
     * The normal Minecraft pick-block handling can then use that
     * flower item and select an existing copy from the player's
     * inventory/hotbar when appropriate.
     */

    @Override
    public ItemStack getCloneItemStack(
            BlockState state,
            HitResult target,
            LevelReader level,
            BlockPos pos,
            Player player
    ) {
        /*
         * Find the BlockEntity containing the actual flower type.
         */
        BlockEntity blockEntity =
                level.getBlockEntity(pos);


        /*
         * If the BlockEntity is missing or invalid, there is no
         * flower to pick.
         */
        if (!(blockEntity instanceof FlowerClusterBlockEntity cluster)) {
            return ItemStack.EMPTY;
        }


        /*
         * Get the flower represented by this cluster.
         */
        Block flower =
                cluster.getFlower();


        /*
         * A cluster without a stored flower cannot provide a
         * meaningful pick-block item.
         */
        if (flower == null) {
            return ItemStack.EMPTY;
        }


        /*
         * Return the normal flower item.
         */
        return new ItemStack(
                flower.asItem()
        );
    }


    /*
     * ============================================================
     * REPLACEMENT PROTECTION
     * ============================================================
     *
     * A FlowerClusterBlock must not be replaced by an ordinary
     * flower BlockItem through Minecraft's normal replacement
     * mechanics.
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
     *
     * A newly created cluster always begins at two flowers.
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


        /*
         * A normal flower is represented by a BlockItem.
         */
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


        /*
         * Deterministic random source.
         *
         * This ensures the same block position produces the same
         * offsets every time.
         */
        RandomSource random =
                RandomSource.create(pos.asLong());


        /*
         * Deterministic rotation.
         */
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
        /*
         * Random offset.
         */
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