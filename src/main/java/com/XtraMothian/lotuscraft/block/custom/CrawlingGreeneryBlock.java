package com.XtraMothian.lotuscraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrawlingGreeneryBlock extends Block {

    public static final MapCodec<CrawlingGreeneryBlock> CODEC =
            simpleCodec(CrawlingGreeneryBlock::new);

    public static final DirectionProperty FACING =
            BlockStateProperties.FACING;

    public static final IntegerProperty AMOUNT =
            IntegerProperty.create("amount", 1, 3);

    // ------------------------------------------------------------
    // Shapes
    //
    // The patch grows outward from the center:
    //
    // Stage 1:  6x6
    // Stage 2: 11x11
    // Stage 3: 16x16
    //
    // Each shape is only 1 pixel (1/16 block) thick.
    // ------------------------------------------------------------

    private static final VoxelShape FLOOR_1 =
            Block.box(4, 0, 4, 12, 1, 12);

    private static final VoxelShape FLOOR_2 =
            Block.box(2, 0, 2, 14, 1, 14);

    private static final VoxelShape FLOOR_3 =
            Block.box(0, 0, 0, 16, 1, 16);

    // Ceiling
    private static final VoxelShape CEILING_1 =
            Block.box(4, 15, 4, 12, 16, 12);

    private static final VoxelShape CEILING_2 =
            Block.box(2, 15, 2, 14, 16, 14);

    private static final VoxelShape CEILING_3 =
            Block.box(0, 15, 0, 16, 16, 16);

    // North wall
    private static final VoxelShape NORTH_1 =
            Block.box(4, 4, 0, 12, 12, 1);

    private static final VoxelShape NORTH_2 =
            Block.box(2, 2, 0, 14, 14, 1);

    private static final VoxelShape NORTH_3 =
            Block.box(0, 0, 0, 16, 16, 1);

    // South wall
    private static final VoxelShape SOUTH_1 =
            Block.box(4, 4, 15, 12, 12, 16);

    private static final VoxelShape SOUTH_2 =
            Block.box(2, 2, 15, 14, 14, 16);

    private static final VoxelShape SOUTH_3 =
            Block.box(0, 0, 15, 16, 16, 16);

    // West wall
    private static final VoxelShape WEST_1 =
            Block.box(0, 4, 4, 1, 12, 12);

    private static final VoxelShape WEST_2 =
            Block.box(0, 2, 2, 1, 14, 14);

    private static final VoxelShape WEST_3 =
            Block.box(0, 0, 0, 1, 16, 16);

    // East wall
    private static final VoxelShape EAST_1 =
            Block.box(15, 4, 4, 16, 12, 12);

    private static final VoxelShape EAST_2 =
            Block.box(15, 2, 2, 16, 14, 14);

    private static final VoxelShape EAST_3 =
            Block.box(15, 0, 0, 16, 16, 16);


    public CrawlingGreeneryBlock(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.UP)
                        .setValue(AMOUNT, 1)
        );
    }

    @Override
    public MapCodec<CrawlingGreeneryBlock> codec() {
        return CODEC;
    }


    // ------------------------------------------------------------
    // Shape
    // ------------------------------------------------------------

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context
    ) {
        Direction facing = state.getValue(FACING);
        int amount = state.getValue(AMOUNT);

        return switch (facing) {
            case UP -> switch (amount) {
                case 1 -> FLOOR_1;
                case 2 -> FLOOR_2;
                default -> FLOOR_3;
            };

            case DOWN -> switch (amount) {
                case 1 -> CEILING_1;
                case 2 -> CEILING_2;
                default -> CEILING_3;
            };

            case NORTH -> switch (amount) {
                case 1 -> NORTH_1;
                case 2 -> NORTH_2;
                default -> NORTH_3;
            };

            case SOUTH -> switch (amount) {
                case 1 -> SOUTH_1;
                case 2 -> SOUTH_2;
                default -> SOUTH_3;
            };

            case WEST -> switch (amount) {
                case 1 -> WEST_1;
                case 2 -> WEST_2;
                default -> WEST_3;
            };

            case EAST -> switch (amount) {
                case 1 -> EAST_1;
                case 2 -> EAST_2;
                default -> EAST_3;
            };
        };
    }


    // ------------------------------------------------------------
    // Placement
    // ------------------------------------------------------------

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());

        if (existing.is(this)) {
            int amount = existing.getValue(AMOUNT);

            if (amount < 3) {
                return existing.setValue(AMOUNT, amount + 1);
            }

            return existing;
        }

        Direction clickedFace = context.getClickedFace();

        // FACING is reversed horizontally, but UP/DOWN remain unchanged.
        Direction facing = clickedFace.getAxis().isHorizontal()
                ? clickedFace.getOpposite()
                : clickedFace;

        // canAttach() expects the FACING value, not the clicked face.
        if (!canAttach(context.getLevel(), context.getClickedPos(), facing)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(AMOUNT, 1);
    }


    // ------------------------------------------------------------
    // Can the moss attach to this face?
    // ------------------------------------------------------------

    private static boolean canAttach(
            BlockGetter level,
            BlockPos mossPos,
            Direction facing
    ) {
        // FACING is reversed for horizontal directions,
        // so convert it back to the actual attachment face.
        Direction attachmentFace = facing.getAxis().isHorizontal()
                ? facing.getOpposite()
                : facing;

        BlockPos supportPos = mossPos.relative(attachmentFace.getOpposite());

        BlockState supportState = level.getBlockState(supportPos);

        return supportState.isFaceSturdy(
                level,
                supportPos,
                attachmentFace,
                SupportType.FULL
        );
    }


    // ------------------------------------------------------------
    // Survival
    // ------------------------------------------------------------

    @Override
    protected boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        return canAttach(
                level,
                pos,
                state.getValue(FACING)
        );
    }


    // ------------------------------------------------------------
    // Remove moss when its supporting block disappears
    // ------------------------------------------------------------

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState facingState,
            net.minecraft.world.level.LevelAccessor level,
            BlockPos currentPos,
            BlockPos facingPos
    ) {
        Direction facing = state.getValue(FACING);

        Direction attachmentFace = facing.getAxis().isHorizontal()
                ? facing.getOpposite()
                : facing;

        if (direction == attachmentFace.getOpposite()) {
            if (!canAttach(level, currentPos, facing)) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(
                state,
                direction,
                facingState,
                level,
                currentPos,
                facingPos
        );
    }


    // ------------------------------------------------------------
    // Right-clicking existing moss increases its size
    // ------------------------------------------------------------

    @Override
    protected boolean canBeReplaced(
            BlockState state,
            BlockPlaceContext context
    ) {
        return !context.isSecondaryUseActive()
                && context.getItemInHand().is(this.asItem())
                && state.getValue(AMOUNT) < 3
                || super.canBeReplaced(state, context);
    }


    // ------------------------------------------------------------
    // Blockstate
    // ------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, AMOUNT);
    }
}
