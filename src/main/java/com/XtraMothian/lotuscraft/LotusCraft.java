package com.XtraMothian.lotuscraft;

import com.XtraMothian.lotuscraft.block.ModBlocks;
import com.XtraMothian.lotuscraft.block.custom.ModBlockColors;
import com.XtraMothian.lotuscraft.client.ClientModEvents; // Import client color handler
import com.XtraMothian.lotuscraft.event.FlintKnappingEvent;
import com.XtraMothian.lotuscraft.item.ModCreativeModeTabs;
import com.XtraMothian.lotuscraft.item.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist; // Added for side checking
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment; // Used to safely check sides
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(LotusCraft.MOD_ID)
public class LotusCraft {
    public static final String MOD_ID = "lotuscraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Unified single constructor accepting both mod loaders
    public LotusCraft(IEventBus modEventBus, ModContainer modContainer) {
        // 1. Core Lifecycle Setup
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // 2. Mod Deferred Registries
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // 3. Global Event Bus Registrations (Game Bus)
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(FlintKnappingEvent.class);

        // 4. Config Registration
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 5. Safe Client-Side Initialization
        // In NeoForge 1.21.1, dist is an enum. Compare it to Dist.CLIENT.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientModEvents::registerBlockColors);
            modEventBus.addListener(ClientModEvents::registerItemColors);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Setup code (e.g. packet registration) goes here
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Code to populate vanilla creative tabs goes here
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("LotusCraft server starting...");
    }
}