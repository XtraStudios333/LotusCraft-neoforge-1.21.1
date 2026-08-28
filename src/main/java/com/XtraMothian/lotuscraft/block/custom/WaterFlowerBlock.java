package com.XtraMothian.lotuscraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterFlowerBlock extends WaterlilyBlock {

    private static final float SUBTLE_MULTIPLIER = 0.08f;

    public WaterFlowerBlock(BlockBehaviour.Properties properties) {
        super(properties.offsetType(BlockBehaviour.OffsetType.XYZ));
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        long seed = Mth.getSeed(pos.getX(), 0, pos.getZ());

        double x = (((double) ((float) (seed & 15L) / 15.0F)) - 0.5D)
                * SUBTLE_MULTIPLIER;

        double z = (((double) ((float) ((seed >> 8) & 15L) / 15.0F)) - 0.5D)
                * SUBTLE_MULTIPLIER;

        return super.getShape(state, level, pos, context)
                .move(x, 0.0D, z);
    }
}