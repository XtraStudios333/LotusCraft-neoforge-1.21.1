package com.XtraMothian.lotuscraft.block.entity.custom;

import com.XtraMothian.lotuscraft.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public class FlowerClusterBlockEntity extends BlockEntity {

    private Block flower;

    public FlowerClusterBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities.FLOWER_CLUSTER.get(),
                pos,
                state
        );
    }

    public void setFlower(Block flower) {
        this.flower = flower;

        setChanged();

        // Tell the client that this BlockEntity's data changed.
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public Block getFlower() {
        return flower;
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        if (flower != null) {
            ResourceLocation id =
                    BuiltInRegistries.BLOCK.getKey(flower);

            tag.putString(
                    "Flower",
                    id.toString()
            );
        }
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Flower")) {
            ResourceLocation id =
                    ResourceLocation.parse(
                            tag.getString("Flower")
                    );

            flower = BuiltInRegistries.BLOCK.get(id);
        }
    }

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries
    ) {
        CompoundTag tag = new CompoundTag();

        saveAdditional(tag, registries);

        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}