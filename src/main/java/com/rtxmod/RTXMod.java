package com.rtxmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main RTX Mod class for server-side initialization
 */
public class RTXMod implements ModInitializer {
    public static final String MOD_ID = "rtx-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing RTX Mod - Server Side");
        
        // Register any server-side components here
        // Note: Most RTX functionality will be client-side only
    }
}
