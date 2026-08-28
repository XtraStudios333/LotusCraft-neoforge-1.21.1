package com.XtraMothian.lotuscraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HornwortBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<HornwortBlock> CODEC =
            simpleCodec(HornwortBlock::new);

    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 4;

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public static final IntegerProperty AMOUNT =
            BlockStateProperties.FLOWER_AMOUNT;

    public static final BooleanProperty WATERLOGGED =
            BlockStateProperties.WATERLOGGED;


    private static final VoxelShape SHAPE_1 = Block.box(
            4.0, 0.0, 4.0,
            12.0, 4.0, 12.0
    );

    private static final VoxelShape SHAPE_2 = Block.box(
            3.0, 0.0, 3.0,
            13.0, 6.0, 13.0
    );

    private static final VoxelShape SHAPE_3 = Block.box(
            2.0, 0.0, 2.0,
            14.0, 8.0, 14.0
    );

    private static final VoxelShape SHAPE_4 = Block.box(
            1.0, 0.0, 1.0,
            15.0, 10.0, 15.0
    );


    public HornwortBlock(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(AMOUNT, MIN_AMOUNT)
                        .setValue(WATERLOGGED, true)
        );
    }


    @Override
    public MapCodec<HornwortBlock> codec() {
        return CODEC;
    }


    // -------------------------------------------------------------------------
    // PLACEMENT
    // -------------------------------------------------------------------------

    @Override
    protected boolean mayPlaceOn(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return state.isFaceSturdy(
                level,
                pos,
                Direction.UP
        );
    }


    @Override
    public boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        BlockPos below = pos.below();

        BlockState belowState = level.getBlockState(below);

        return belowState.isFaceSturdy(
                level,
                below,
                Direction.UP
        );
    }


    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {

        BlockPos pos = context.getClickedPos();

        BlockState existingState =
                context.getLevel().getBlockState(pos);


        // Adding another Hornwort to an existing Hornwort.
        if (existingState.is(this)) {

            int amount = existingState.getValue(AMOUNT);

            return existingState.setValue(
                    AMOUNT,
                    Math.min(MAX_AMOUNT, amount + 1)
            );
        }


        // The block below must have a solid top face.
        BlockState belowState =
                context.getLevel().getBlockState(pos.below());

        if (!belowState.isFaceSturdy(
                context.getLevel(),
                pos.below(),
                Direction.UP
        )) {
            return null;
        }


        // The Hornwort itself must be placed in water.
        FluidState fluidState =
                context.getLevel().getFluidState(pos);

        if (!fluidState.is(Fluids.WATER)) {
            return null;
        }


        return this.defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                )
                .setValue(
                        WATERLOGGED,
                        true
                );
    }


    // -------------------------------------------------------------------------
    // WATER
    // -------------------------------------------------------------------------

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }


    // -------------------------------------------------------------------------
    // REPLACEMENT
    // -------------------------------------------------------------------------

    @Override
    public boolean canBeReplaced(
            BlockState state,
            BlockPlaceContext context
    ) {

        return !context.isSecondaryUseActive()
                && context.getItemInHand().is(this.asItem())
                && state.getValue(AMOUNT) < MAX_AMOUNT
                || super.canBeReplaced(state, context);
    }


    // -------------------------------------------------------------------------
    // BLOCK STATES
    // -------------------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(
                FACING,
                AMOUNT,
                WATERLOGGED
        );
    }


    @Override
    public BlockState rotate(
            BlockState state,
            net.minecraft.world.level.block.Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }


    @Override
    public BlockState mirror(
            BlockState state,
            net.minecraft.world.level.block.Mirror mirror
    ) {
        return state.rotate(
                mirror.getRotation(state.getValue(FACING))
        );
    }


    // -------------------------------------------------------------------------
    // SHAPE
    // -------------------------------------------------------------------------

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {

        return switch (state.getValue(AMOUNT)) {

            case 1 -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            case 4 -> SHAPE_4;

            default -> SHAPE_1;
        };
    }


    // -------------------------------------------------------------------------
    // BONEMEAL
    // -------------------------------------------------------------------------

    @Override
    public boolean isValidBonemealTarget(
            LevelReader level,
            BlockPos pos,
            BlockState state
    ) {
        return state.getValue(AMOUNT) < MAX_AMOUNT;
    }


    @Override
    public boolean isBonemealSuccess(
            Level level,
            RandomSource random,
            BlockPos pos,
            BlockState state
    ) {
        return true;
    }


    @Override
    public void performBonemeal(
            ServerLevel level,
            RandomSource random,
            BlockPos pos,
            BlockState state
    ) {

        int amount = state.getValue(AMOUNT);

        if (amount < MAX_AMOUNT) {

            level.setBlock(
                    pos,
                    state.setValue(
                            AMOUNT,
                            amount + 1
                    ),
                    2
            );

        } else {

            popResource(
                    level,
                    pos,
                    new ItemStack(this)
            );
        }
    }
}