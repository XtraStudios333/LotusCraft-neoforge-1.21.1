package com.XtraMothian.lotuscraft.util;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapeUtil {

    public static VoxelShape rotate(VoxelShape shape, int rotation) {

        VoxelShape rotated = Shapes.empty();

        for (var box : shape.toAabbs()) {

            double minX = box.minX;
            double minY = box.minY;
            double minZ = box.minZ;

            double maxX = box.maxX;
            double maxY = box.maxY;
            double maxZ = box.maxZ;

            switch (rotation) {

                case 90 -> {
                    rotated = Shapes.join(
                            rotated,
                            Shapes.box(
                                    1 - maxZ,
                                    minY,
                                    minX,
                                    1 - minZ,
                                    maxY,
                                    maxX
                            ),
                            BooleanOp.OR
                    );
                }

                case 180 -> {
                    rotated = Shapes.join(
                            rotated,
                            Shapes.box(
                                    1 - maxX,
                                    minY,
                                    1 - maxZ,
                                    1 - minX,
                                    maxY,
                                    1 - minZ
                            ),
                            BooleanOp.OR
                    );
                }

                case 270 -> {
                    rotated = Shapes.join(
                            rotated,
                            Shapes.box(
                                    minZ,
                                    minY,
                                    1 - maxX,
                                    maxZ,
                                    maxY,
                                    1 - minX
                            ),
                            BooleanOp.OR
                    );
                }

                default -> {
                    rotated = Shapes.join(
                            rotated,
                            Shapes.box(
                                    minX,
                                    minY,
                                    minZ,
                                    maxX,
                                    maxY,
                                    maxZ
                            ),
                            BooleanOp.OR
                    );
                }
            }
        }

        return rotated;
    }
}