package com.XtraMothian.lotuscraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SargassumBlock extends WaterlilyBlock {

    public static final MapCodec<WaterlilyBlock> CODEC =
            simpleCodec(SargassumBlock::new);

    @Override
    public MapCodec<WaterlilyBlock> codec() {
        return CODEC;
    }

    public SargassumBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state,
                                           BlockGetter level,
                                           BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state,
                             Level level,
                             BlockPos pos,
                             Entity entity) {

        if (entity instanceof LivingEntity && entity.isInWater()) {
            entity.makeStuckInBlock(state, new Vec3(1.0D, 1.0D, 1.0D));
        }

        super.entityInside(state, level, pos, entity);
    }
}