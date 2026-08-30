package com.XtraMothian.lotuscraft.client;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.HashMap;
import java.util.Map;

public class FlowerClusterClientExtensions {

    /*
     * ============================================================
     * CLIENT FLOWER CACHE
     * ============================================================
     *
     * The cluster's flower is stored inside its BlockEntity.
     *
     * During destruction, Minecraft can request the particles
     * after the BlockEntity has already been removed.
     *
     * Therefore we remember the flower while the cluster exists.
     */

    private static final Map<BlockPos, Block> FLOWER_CACHE =
            new HashMap<>();

    /*
     * ============================================================
     * REGISTER CLIENT EXTENSION
     * ============================================================
     */

    public static void registerClientExtensions(
            RegisterClientExtensionsEvent event
    ) {

        event.registerBlock(
                new IClientBlockExtensions() {

                    /*
                     * ====================================================
                     * DESTROY PARTICLES
                     * ====================================================
                     */

                    @Override
                    public boolean addDestroyEffects(
                            BlockState state,
                            Level level,
                            BlockPos pos,
                            ParticleEngine manager
                    ) {

                        /*
                         * First try to obtain the flower directly
                         * from the BlockEntity.
                         */
                        Block flower =
                                getFlower(level, pos);

                        /*
                         * If the BlockEntity has already disappeared,
                         * use the cached flower instead.
                         */
                        if (flower == null) {

                            flower =
                                    FLOWER_CACHE.get(
                                            pos.immutable()
                                    );
                        }

                        /*
                         * We don't know what flower this cluster
                         * contained.
                         *
                         * Allow vanilla/Minecraft to handle it.
                         */
                        if (flower == null) {
                            return false;
                        }

                        BlockState flowerState =
                                flower.defaultBlockState();

                        /*
                         * Invisible blocks should not create
                         * terrain particles.
                         */
                        if (flowerState.getRenderShape()
                                == RenderShape.INVISIBLE) {

                            FLOWER_CACHE.remove(
                                    pos.immutable()
                            );

                            return false;
                        }

                        /*
                         * ====================================================
                         * USE VANILLA'S NORMAL DESTROY PARTICLES
                         * ====================================================
                         *
                         * ParticleEngine.destroy() creates the
                         * normal TerrainParticles and, importantly,
                         * uses the supplied BlockState to determine
                         * their texture.
                         *
                         * Because we supply the ORIGINAL FLOWER'S
                         * state, the particles should use the
                         * flower's texture instead of the cluster's.
                         */

                        manager.destroy(
                                pos,
                                flowerState
                        );

                        /*
                         * The particles have now been generated.
                         *
                         * Prevent Minecraft from generating the
                         * cluster's purple particles as well.
                         */
                        FLOWER_CACHE.remove(
                                pos.immutable()
                        );

                        return true;
                    }

                    /*
                     * ====================================================
                     * HIT PARTICLES
                     * ====================================================
                     */

                    @Override
                    public boolean addHitEffects(
                            BlockState state,
                            Level level,
                            HitResult target,
                            ParticleEngine manager
                    ) {

                        if (!(target instanceof BlockHitResult blockHit)) {
                            return false;
                        }

                        BlockPos pos =
                                blockHit.getBlockPos();

                        /*
                         * Get the flower from the BlockEntity.
                         */
                        Block flower =
                                getFlower(level, pos);

                        /*
                         * Fall back to our cache.
                         */
                        if (flower == null) {

                            flower =
                                    FLOWER_CACHE.get(
                                            pos.immutable()
                                    );
                        }

                        if (flower == null) {
                            return false;
                        }

                        BlockState flowerState =
                                flower.defaultBlockState();

                        if (flowerState.getRenderShape()
                                == RenderShape.INVISIBLE) {

                            return false;
                        }

                        /*
                         * The ParticleEngine.crack() method looks
                         * up the block at the position itself.
                         *
                         * That would return FlowerClusterBlock.
                         *
                         * So we construct the TerrainParticle
                         * directly with the flower state.
                         */

                        if (!(level instanceof ClientLevel clientLevel)) {
                            return false;
                        }

                        Direction side =
                                blockHit.getDirection();

                        AABB bounds =
                                flowerState
                                        .getShape(level, pos)
                                        .bounds();

                        double x =
                                pos.getX()
                                        + 0.5D;

                        double y =
                                pos.getY()
                                        + 0.5D;

                        double z =
                                pos.getZ()
                                        + 0.5D;

                        float offset =
                                0.1F;

                        /*
                         * Put the particle just outside the
                         * appropriate face.
                         */
                        switch (side) {

                            case DOWN ->

                                    y =
                                            pos.getY()
                                                    + bounds.minY
                                                    - offset;

                            case UP ->

                                    y =
                                            pos.getY()
                                                    + bounds.maxY
                                                    + offset;

                            case NORTH ->

                                    z =
                                            pos.getZ()
                                                    + bounds.minZ
                                                    - offset;

                            case SOUTH ->

                                    z =
                                            pos.getZ()
                                                    + bounds.maxZ
                                                    + offset;

                            case WEST ->

                                    x =
                                            pos.getX()
                                                    + bounds.minX
                                                    - offset;

                            case EAST ->

                                    x =
                                            pos.getX()
                                                    + bounds.maxX
                                                    + offset;
                        }

                        TerrainParticle particle =
                                new TerrainParticle(
                                        clientLevel,
                                        x,
                                        y,
                                        z,
                                        0.0D,
                                        0.0D,
                                        0.0D,
                                        flowerState,
                                        pos
                                );

                        /*
                         * Explicitly update the particle sprite
                         * using the flower state.
                         */
                        particle.updateSprite(
                                flowerState,
                                pos
                        );

                        particle
                                .setPower(0.2F)
                                .scale(0.6F);

                        manager.add(
                                particle
                        );

                        return true;
                    }
                },

                ModBlocks.FLOWER_CLUSTER.get()
        );
    }

    /*
     * ============================================================
     * GET FLOWER + CACHE IT
     * ============================================================
     */

    private static Block getFlower(
            Level level,
            BlockPos pos
    ) {

        if (!(level.getBlockEntity(pos)
                instanceof FlowerClusterBlockEntity cluster)) {

            return null;
        }

        Block flower =
                cluster.getFlower();

        if (flower != null) {

            /*
             * Store an immutable copy of the position.
             */
            FLOWER_CACHE.put(
                    pos.immutable(),
                    flower
            );
        }

        return flower;
    }
}