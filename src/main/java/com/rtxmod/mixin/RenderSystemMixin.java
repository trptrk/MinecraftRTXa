package com.rtxmod.mixin;

import com.rtxmod.RTXModClient;
import com.rtxmod.rendering.RTXRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into RenderSystem for window management and other low-level rendering events
 */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    
    // This method is called when the window is resized
    @Inject(method = "viewport", at = @At("HEAD"))
    private static void onViewportChange(int x, int y, int width, int height, CallbackInfo ci) {
        RTXRenderer renderer = RTXModClient.getRTXRenderer();
        
        if (renderer != null && renderer.isInitialized()) {
            // Notify RTX renderer of window resize
            renderer.onWindowResize(width, height);
        }
    }
}
