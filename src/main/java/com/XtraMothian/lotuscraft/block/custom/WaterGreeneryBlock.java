package com.XtraMothian.lotuscraft.block.custom;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


public class WaterGreeneryBlock extends WaterlilyBlock {

    public static final MapCodec<WaterGreeneryBlock> CODEC =
            simpleCodec(WaterGreeneryBlock::new);

    public static final IntegerProperty AMOUNT =
            IntegerProperty.create("amount", 1, 3);


    // ------------------------------------------------------------
    // Shapes
    // ------------------------------------------------------------

    private static final VoxelShape GREENERY_1 =
            Block.box(4, 0, 4, 12, 1, 12);

    private static final VoxelShape GREENERY_2 =
            Block.box(2, 0, 2, 14, 1, 14);

    private static final VoxelShape GREENERY_3 =
            Block.box(0, 0, 0, 16, 1, 16);


    public WaterGreeneryBlock(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AMOUNT, 1)
        );
    }


    // ------------------------------------------------------------
    // Blockstate
    // ------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(AMOUNT);
    }


    // ------------------------------------------------------------
    // Right-click existing greenery
    // ------------------------------------------------------------

    @Override
    public ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (stack.is(this.asItem())
                && state.getValue(AMOUNT) < 3) {

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

                // Consume one item in Survival.
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                level.playSound(
                        null,
                        pos,
                        newState.getSoundType().getPlaceSound(),
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        (newState.getSoundType().getVolume() + 1.0F) / 2.0F,
                        newState.getSoundType().getPitch() * 0.8F
                );
            }

            return ItemInteractionResult.sidedSuccess(
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


    // ------------------------------------------------------------
    // Placement
    //
    // Same placement logic as AquaticClusterBlock:
    //
    // - Existing greenery -> increase AMOUNT
    // - Otherwise -> normal Waterlily placement
    // ------------------------------------------------------------

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
                            3,
                            existing.getValue(AMOUNT) + 1
                    )
            );
        }

        // Normal WaterlilyBlock placement.
        BlockState state =
                super.getStateForPlacement(context);

        if (state == null) {
            return null;
        }

        return state.setValue(
                AMOUNT,
                1
        );
    }


    // ------------------------------------------------------------
    // Allow the same item to replace the greenery
    // ------------------------------------------------------------

    @Override
    protected boolean canBeReplaced(
            BlockState state,
            BlockPlaceContext context
    ) {
        return context.getItemInHand().is(this.asItem())
                && state.getValue(AMOUNT) < 3;
    }


    // ------------------------------------------------------------
    // Shape
    // ------------------------------------------------------------

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return switch (state.getValue(AMOUNT)) {
            case 1 -> GREENERY_1;
            case 2 -> GREENERY_2;
            default -> GREENERY_3;
        };
    }


    // ------------------------------------------------------------
    // Survival
    //
    // Uses WaterlilyBlock's normal water survival rules.
    // ------------------------------------------------------------

    @Override
    protected boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        return super.canSurvive(
                state,
                level,
                pos
        );
    }
}