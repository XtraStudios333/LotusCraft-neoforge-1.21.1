package com.XtraMothian.lotuscraft.block.custom;

import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CloversBlock extends PinkPetalsBlock {

    public CloversBlock(Properties properties) {
        super(properties);
    }

    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 1, 4);
}