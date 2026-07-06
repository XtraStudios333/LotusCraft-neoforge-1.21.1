package com.XtraMothian.lotuscraft.block.custom;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GrassSoilBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {

    public static final MapCodec<GrassSoilBlock> CODEC =
            simpleCodec(GrassSoilBlock::new);

    @Override
    public MapCodec<GrassSoilBlock> codec() {
        return CODEC;
    }

    public GrassSoilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Replacement for vanilla's private canBeGrass().
     */
    private static boolean canRemainGrass(LevelReader level, BlockPos pos) {

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        // Thin snow doesn't kill grass.
        if (aboveState.is(Blocks.SNOW)
                && aboveState.hasProperty(SnowLayerBlock.LAYERS)
                && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }

        // Waterlogged blocks kill grass.
        if (aboveState.getFluidState().is(FluidTags.WATER)
                && aboveState.getFluidState().getAmount() == 8) {
            return false;
        }

        int lightBlock = LightEngine.getLightBlockInto(
                level,
                level.getBlockState(pos),
                pos,
                aboveState,
                abovePos,
                Direction.UP,
                aboveState.getLightBlock(level, abovePos)
        );

        return lightBlock < level.getMaxLightLevel();
    }

    /**
     * Replacement for vanilla's private canPropagate().
     */
    private static boolean canSpread(LevelReader level, BlockPos pos) {
        return canRemainGrass(level, pos)
                && !level.getBlockState(pos.above()).getFluidState().is(FluidTags.WATER);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        // Turn back into soil if covered.
        if (!canRemainGrass(level, pos)) {

            BlockState soil = ModBlocks.getSoilVariant(state);

            if (soil != null) {
                level.setBlockAndUpdate(pos, soil);
            }

            return;
        }

        // Spread when bright enough.
        if (level.getBrightness(LightLayer.SKY, pos.above()) >= 9) {

            for (int i = 0; i < 4; ++i) {

                BlockPos targetPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );

                if (!canSpread(level, targetPos)) {
                    continue;
                }

                BlockState targetState = level.getBlockState(targetPos);

                BlockState grassVariant = ModBlocks.getGrassVariant(targetState);

                if (grassVariant != null) {

                    level.setBlockAndUpdate(
                            targetPos,
                            grassVariant.setValue(
                                    SNOWY,
                                    level.getBlockState(targetPos.above()).is(Blocks.SNOW)
                            )
                    );
                }
            }
        }
    }

    // ------------------------
    // Bone Meal
    // ------------------------

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {

        BlockPos above = pos.above();
        BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();

        Optional<Holder.Reference<PlacedFeature>> grassFeature =
                level.registryAccess()
                        .registryOrThrow(Registries.PLACED_FEATURE)
                        .getHolder(VegetationPlacements.GRASS_BONEMEAL);

        outer:
        for (int i = 0; i < 128; i++) {

            BlockPos placePos = above;

            for (int j = 0; j < i / 16; j++) {

                placePos = placePos.offset(
                        random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1
                );

                if (!level.getBlockState(placePos.below()).is(this)
                        || level.getBlockState(placePos).isCollisionShapeFullBlock(level, placePos)) {
                    continue outer;
                }
            }

            BlockState current = level.getBlockState(placePos);

            if (current.is(shortGrass.getBlock()) && random.nextInt(10) == 0) {
                ((BonemealableBlock) shortGrass.getBlock())
                        .performBonemeal(level, random, placePos, current);
            }

            if (current.isAir()) {

                Holder<PlacedFeature> feature;

                if (random.nextInt(8) == 0) {

                    List<ConfiguredFeature<?, ?>> flowers =
                            level.getBiome(placePos)
                                    .value()
                                    .getGenerationSettings()
                                    .getFlowerFeatures();

                    if (flowers.isEmpty()) {
                        continue;
                    }

                    feature = ((RandomPatchConfiguration) flowers.get(0).config()).feature();

                } else {

                    if (grassFeature.isEmpty()) {
                        continue;
                    }

                    feature = grassFeature.get();
                }

                feature.value().place(
                        level,
                        level.getChunkSource().getGenerator(),
                        random,
                        placePos
                );
            }
        }
    }
}