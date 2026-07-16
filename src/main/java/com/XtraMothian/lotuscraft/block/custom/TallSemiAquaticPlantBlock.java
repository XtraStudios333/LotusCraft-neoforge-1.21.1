package com.XtraMothian.lotuscraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;

public class TallSemiAquaticPlantBlock extends DoublePlantBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public TallSemiAquaticPlantBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HALF, DoubleBlockHalf.LOWER)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HALF, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (!level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }

        FluidState lowerFluid = level.getFluidState(pos);
        FluidState upperFluid = level.getFluidState(pos.above());

        // Reject if the upper half would also be underwater.
        if (upperFluid.is(FluidTags.WATER)) {
            return null;
        }

        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());

        if (fluid.is(FluidTags.WATER) && fluid.getAmount() != 8) {
            return null;
        }

        BlockState state = super.getStateForPlacement(context);

        if (state == null) {
            return null;
        }

        return state.setValue(WATERLOGGED, lowerFluid.is(FluidTags.WATER));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {

        // Upper half must sit on the lower half.
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());

            return below.is(this)
                    && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        // Lower half
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        FluidState lowerFluid = level.getFluidState(pos);
        FluidState upperFluid = level.getFluidState(pos.above());

        // Is the lower half in a SOURCE water block?
        boolean inSourceWater =
                lowerFluid.is(FluidTags.WATER) &&
                        lowerFluid.getAmount() == 8;

        if (inSourceWater) {

            // Don't allow deeper than one block of water.
            if (upperFluid.is(FluidTags.WATER)) {
                return false;
            }

            // Underwater, any sturdy full block is valid.
            return belowState.isFaceSturdy(level, belowPos, Direction.UP);
        }

        // On land, only dirt-tagged blocks.
        return belowState.is(BlockTags.DIRT);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {

        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}