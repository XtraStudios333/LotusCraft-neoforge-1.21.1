package com.XtraMothian.lotuscraft.client.renderer;

import com.XtraMothian.lotuscraft.block.custom.FlowerClusterBlock;
import com.XtraMothian.lotuscraft.block.entity.custom.FlowerClusterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Axis;

import java.util.List;

public class FlowerClusterRenderer
        implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<FlowerClusterBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public FlowerClusterRenderer(
            net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context
    ) {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(
            FlowerClusterBlockEntity cluster,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Block flower = cluster.getFlower();

        if (flower == null) {
            return;
        }

        BlockState flowerState = flower.defaultBlockState();

        int amount = cluster.getBlockState()
                .getValue(FlowerClusterBlock.AMOUNT);

        BlockPos pos = cluster.getBlockPos();

        /*
         * One deterministic random source per cluster.
         *
         * This means the arrangement does not change every frame
         * and remains the same after leaving/re-entering the world.
         */
        RandomSource random = RandomSource.create(pos.asLong());

        /*
         * Randomly rotate the entire cluster by 0, 90, 180,
         * or 270 degrees.
         */
        int rotation = random.nextInt(4);

        poseStack.pushPose();

        /*
         * Rotate around the center of the block.
         */
        poseStack.translate(
                0.5F,
                0.0F,
                0.5F
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        rotation * 90.0F
                )
        );

        poseStack.translate(
                -0.5F,
                0.0F,
                -0.5F
        );

        /*
         * Base positions for the individual flowers.
         */
        switch (amount) {

            case 2 -> {

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        -0.24F,
                        -0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        0.24F,
                        0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );
            }

            case 3 -> {

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        0.0F,
                        -0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        -0.24F,
                        0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        0.24F,
                        0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );
            }

            case 4 -> {

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        -0.24F,
                        -0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        0.24F,
                        -0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        -0.24F,
                        0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );

                renderRandomFlower(
                        flowerState,
                        poseStack,
                        buffer,
                        0.24F,
                        0.24F,
                        random,
                        packedLight,
                        packedOverlay
                );
            }
        }

        poseStack.popPose();
    }

    private void renderRandomFlower(
            BlockState flowerState,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float baseX,
            float baseZ,
            RandomSource random,
            int packedLight,
            int packedOverlay
    ) {
        /*
         * ±0.15 block random offset.
         */
        float randomX =
                (random.nextFloat() * 0.30F) - 0.15F;

        float randomZ =
                (random.nextFloat() * 0.30F) - 0.15F;

        poseStack.pushPose();

        poseStack.translate(
                baseX + randomX,
                0.0F,
                baseZ + randomZ
        );

        renderFlowerModel(
                flowerState,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }

    /**
     * Renders the flower's existing baked model manually.
     *
     * The important difference from renderSingleBlock() is that
     * we control the vertex color ourselves instead of allowing
     * ModelBlockRenderer to apply directional block shading.
     */
    private void renderFlowerModel(
            BlockState flowerState,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        BakedModel model =
                blockRenderer.getBlockModel(flowerState);

        RandomSource random = RandomSource.create(42L);

        VertexConsumer consumer =
                buffer.getBuffer(RenderType.cutout());

        /*
         * Render both the general quads and the directional quads.
         */
        renderQuads(
                model.getQuads(
                        flowerState,
                        null,
                        random
                ),
                poseStack,
                consumer,
                packedLight,
                packedOverlay
        );

        for (net.minecraft.core.Direction direction :
                net.minecraft.core.Direction.values()) {

            random.setSeed(42L);

            renderQuads(
                    model.getQuads(
                            flowerState,
                            direction,
                            random
                    ),
                    poseStack,
                    consumer,
                    packedLight,
                    packedOverlay
            );
        }
    }

    /**
     * Writes baked quads directly into the VertexConsumer.
     *
     * We intentionally do NOT use the baked quad's vertex color.
     * Every vertex receives pure white (255,255,255,255).
     *
     * This removes the directional color multiplier that was
     * making the cluster flowers appear shaded/darker than
     * normal flowers.
     */
    private void renderQuads(
            List<BakedQuad> quads,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay
    ) {
        for (BakedQuad quad : quads) {

            int[] vertices = quad.getVertices();

            int vertexCount =
                    vertices.length / 8;

            for (int i = 0; i < vertexCount; i++) {

                int index = i * 8;

                /*
                 * BLOCK vertex format:
                 *
                 * 0 = X
                 * 1 = Y
                 * 2 = Z
                 * 3 = color
                 * 4 = U
                 * 5 = V
                 * 6 = light
                 * 7 = normal
                 */

                float x =
                        Float.intBitsToFloat(vertices[index]);

                float y =
                        Float.intBitsToFloat(vertices[index + 1]);

                float z =
                        Float.intBitsToFloat(vertices[index + 2]);

                int color =
                        0xFFFFFFFF;

                float u =
                        Float.intBitsToFloat(vertices[index + 4]);

                float v =
                        Float.intBitsToFloat(vertices[index + 5]);

                /*
                 * Use the actual world light level.
                 *
                 * This means the flower can still become dark
                 * at night or inside a dark area.
                 */
                int light =
                        packedLight;

                /*
                 * Constant normal.
                 *
                 * We don't use the baked quad normal because
                 * directional normals are part of what causes
                 * the flower faces to receive different shading.
                 */
                float normalX = 0.0F;
                float normalY = 1.0F;
                float normalZ = 0.0F;

                consumer.addVertex(
                        poseStack.last(),
                        x,
                        y,
                        z
                );

                consumer.setColor(255, 255, 255, 255);

                consumer.setUv(u, v);

                consumer.setOverlay(packedOverlay);

                consumer.setLight(light);

                consumer.setNormal(
                        poseStack.last(),
                        normalX,
                        normalY,
                        normalZ
                );
            }
        }
    }
}