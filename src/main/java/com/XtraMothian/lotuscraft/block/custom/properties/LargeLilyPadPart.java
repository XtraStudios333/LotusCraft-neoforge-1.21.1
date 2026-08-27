package com.XtraMothian.lotuscraft.block.custom.properties;

import net.minecraft.util.StringRepresentable;

public enum LargeLilyPadPart implements StringRepresentable {

    TOP_LEFT("top_left"),
    TOP_RIGHT("top_right"),
    BOTTOM_LEFT("bottom_left"),
    BOTTOM_RIGHT("bottom_right");

    private final String name;

    LargeLilyPadPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}