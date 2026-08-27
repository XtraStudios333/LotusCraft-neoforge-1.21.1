package com.XtraMothian.lotuscraft.block.custom;

import com.XtraMothian.lotuscraft.util.ShapeUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


public class AquaticClusterBlock extends WaterlilyBlock {

    public static final IntegerProperty AMOUNT =
            IntegerProperty.create("amount", 1, 4);

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;


    private static final VoxelShape[] SHAPES = {

            // amount=1
            Block.box(
                    1, 0,
                    1,
                    8, 1.5,
                    8
            ),

            // amount=2
            Block.box(
                    1, 0,
                    1,
                    8, 1.5,
                    15
            ),

            // amount=3
            Block.box(
                    1, 0,
                    1,
                    15, 1.5,
                    15
            ),

            // amount=4
            Block.box(
                    1, 0,
                    1,
                    15, 1.5,
                    15
            )
    };


    public AquaticClusterBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AMOUNT, 1)
                        .setValue(FACING, Direction.NORTH)
        );
    }


    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(
                AMOUNT,
                FACING
        );
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(
            net.minecraft.world.item.ItemStack stack,
            BlockState state,
            net.minecraft.world.level.Level level,
            BlockPos pos,
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit
    ) {

        if (stack.is(this.asItem())
                && state.getValue(AMOUNT) < 4) {

            if (!level.isClientSide) {

                BlockState newState = state.setValue(
                        AMOUNT,
                        state.getValue(AMOUNT) + 1
                );

                level.setBlock(
                        pos,
                        newState,
                        3
                );

                level.playSound(
                        null,
                        pos,
                        newState.getSoundType().getPlaceSound(),
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        (newState.getSoundType().getVolume() + 1.0F) / 2.0F,
                        newState.getSoundType().getPitch() * 0.8F
                );
            }

            return net.minecraft.world.ItemInteractionResult.sidedSuccess(
                    level.isClientSide
            );
        }

        return super.useItemOn(
                stack,
                state,
                level,
                pos,
                player,
                hand,
                hit
        );
    }


    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {

        BlockState existing =
                context.getLevel()
                        .getBlockState(context.getClickedPos());


        if (existing.is(this)) {

            return existing.setValue(
                    AMOUNT,
                    Math.min(
                            4,
                            existing.getValue(AMOUNT) + 1
                    )
            );
        }


        return this.defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection()
                );
    }


    @Override
    protected boolean canBeReplaced(
            BlockState state,
            BlockPlaceContext context
    ) {
        return context.getItemInHand().is(this.asItem())
                && state.getValue(AMOUNT) < 4;
    }


    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {

        VoxelShape shape =
                SHAPES[state.getValue(AMOUNT) - 1];


        return switch(state.getValue(FACING)) {

            case EAST ->
                    ShapeUtil.rotate(shape, 90);

            case SOUTH ->
                    ShapeUtil.rotate(shape, 180);

            case WEST ->
                    ShapeUtil.rotate(shape, 270);

            default ->
                    shape;
        };
    }
}