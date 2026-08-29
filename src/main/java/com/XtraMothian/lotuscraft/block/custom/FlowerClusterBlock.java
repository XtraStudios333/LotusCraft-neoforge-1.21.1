package com.XtraMothian.lotuscraft.block.custom;

import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FlowerClusterBlock extends Block implements EntityBlock {

    public static final IntegerProperty AMOUNT =
            IntegerProperty.create("amount", 2, 4);

    public FlowerClusterBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AMOUNT, 2)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(AMOUNT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlowerClusterBlockEntity(pos, state);
    }
}