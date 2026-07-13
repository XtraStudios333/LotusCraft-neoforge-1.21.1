package com.XtraMothian.lotuscraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FoliageBlock extends BushBlock {

    public static final MapCodec<FoliageBlock> CODEC =
            simpleCodec(FoliageBlock::new);

    public FoliageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT);
    }
}