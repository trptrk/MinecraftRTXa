package com.rtxmod.mixin;

import com.rtxmod.RTXModClient;
import com.rtxmod.rendering.RTXRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into WorldRenderer to integrate RTX rendering
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    
    @Inject(method = "render", at = @At("HEAD"))
    private void onWorldRenderStart(MatrixStack matrices, float tickDelta, long limitTime, 
                                  boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, 
                                  LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, 
                                  CallbackInfo ci) {
        
        RTXRenderer renderer = RTXModClient.getRTXRenderer();
        
        if (renderer != null && renderer.isInitialized() && RTXModClient.getConfig().isRayTracingEnabled()) {
            // Get view matrix from camera
            Matrix4f viewMatrix = matrices.peek().getPositionMatrix().copy();
            
            // Render RTX frame
            renderer.render(viewMatrix, projectionMatrix, tickDelta);
        }
    }
}
