package com.rtxmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.rtxmod.rendering.RTXRenderer;
import com.rtxmod.config.RTXConfig;

/**
 * Client-side RTX Mod initialization
 * This is where all the ray tracing magic happens!
 */
@Environment(EnvType.CLIENT)
public class RTXModClient implements ClientModInitializer {
    
    private static RTXRenderer rtxRenderer;
    private static RTXConfig config;
    
    // Key bindings
    private static KeyBinding toggleRTXKeyBinding;
    private static KeyBinding reloadShadersKeyBinding;

    @Override
    public void onInitializeClient() {
        RTXMod.LOGGER.info("Initializing RTX Mod - Client Side");
        
        // Initialize configuration first
        config = new RTXConfig();
        
        // Initialize the RTX renderer
        rtxRenderer = new RTXRenderer();
        
        // Register key bindings, events, etc.
        registerKeyBindings();
        registerClientEvents();
        
        RTXMod.LOGGER.info("RTX Mod client initialization complete!");
    }
    
    private void registerKeyBindings() {
        // Toggle RTX key binding (R by default)
        toggleRTXKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rtx-mod.toggle_rtx",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.rtx-mod.rtx"
        ));
        
        // Reload shaders key binding (F5 by default)
        reloadShadersKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rtx-mod.reload_shaders",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F5,
            "category.rtx-mod.rtx"
        ));
        
        RTXMod.LOGGER.info("Registered RTX key bindings");
    }
    
    private void registerClientEvents() {
        // Client lifecycle events
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            RTXMod.LOGGER.info("Minecraft client started, initializing RTX renderer...");
            if (rtxRenderer != null) {
                rtxRenderer.initialize();
            }
        });
        
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            RTXMod.LOGGER.info("Minecraft client stopping, cleaning up RTX renderer...");
            if (rtxRenderer != null) {
                rtxRenderer.cleanup();
            }
        });
        
        // Register other client-side event handlers here
        // Render events for hooking into Minecraft's rendering pipeline would go here
        
        RTXMod.LOGGER.info("Registered RTX client events");
    }
    
    public static RTXRenderer getRTXRenderer() {
        return rtxRenderer;
    }
    
    public static RTXConfig getConfig() {
        return config;
    }
    
    public static KeyBinding getToggleRTXKeyBinding() {
        return toggleRTXKeyBinding;
    }
    
    public static KeyBinding getReloadShadersKeyBinding() {
        return reloadShadersKeyBinding;
    }
}
