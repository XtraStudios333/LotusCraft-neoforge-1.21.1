package com.XtraMothian.lotuscraft.event;

import com.XtraMothian.lotuscraft.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

public class FlintKnappingEvent {

    public static final TagKey<Block> FLINT_KNAPPING_BLOCKS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("lotuscraft", "flint_knappable")
    );

    @SubscribeEvent
    public static void onKnap(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) {
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        ItemStack heldItem = event.getItemStack();
        var blockState = event.getLevel().getBlockState(event.getPos());
        Player player = event.getPlayer();

        if (heldItem.is(Items.FLINT) && blockState.is(FLINT_KNAPPING_BLOCKS)) {

            // Deduct from player hand on both Client and Server to prevent desync
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            // Get the precise visual click location on the block's face
            Vec3 clickLocation = event.getUseOnContext().getClickLocation();

            // Server-only actions (giving items and spawning networked particles)
            if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
                ItemStack knappedFlint = new ItemStack(ModItems.KNAPPED_FLINT.get(), 1);
                if (!player.getInventory().add(knappedFlint)) {
                    player.drop(knappedFlint, false);
                }

                // Create the data option to tell the particle engine to mimic the Flint item texture
                ItemParticleOption flintParticleData = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.FLINT));

                // Spawn 15 break particles bursting out slightly from the click location
                serverLevel.sendParticles(
                        flintParticleData,
                        clickLocation.x, clickLocation.y, clickLocation.z, // Position
                        10,                                                // Count
                        0.08D, 0.3D, 0.08D,                                  // Random spread offset
                        0.05D                                              // Particle velocity/speed
                );
            }

            // Audio cues and arm swings can safely happen on both sides
            event.getLevel().playSound(
                    null,
                    event.getPos(),
                    SoundEvents.CALCITE_BREAK,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.9F + event.getLevel().getRandom().nextFloat() * 0.4F
            );

            player.swing(InteractionHand.MAIN_HAND, true);

            event.cancelWithResult(ItemInteractionResult.CONSUME);
        }
    }
}