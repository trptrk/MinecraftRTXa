package com.rtxmod.mixin;

import com.rtxmod.RTXModClient;
import com.rtxmod.rendering.RTXRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into the GameRenderer for RTX integration
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // Check for key presses and handle RTX-related input
        RTXRenderer renderer = RTXModClient.getRTXRenderer();
        
        if (renderer != null) {
            // Handle key bindings
            if (RTXModClient.getToggleRTXKeyBinding() != null && RTXModClient.getToggleRTXKeyBinding().wasPressed()) {
                boolean currentState = RTXModClient.getConfig().isRayTracingEnabled();
                RTXModClient.getConfig().setRayTracingEnabled(!currentState);
                
                if (currentState) {
                    net.minecraft.client.MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(net.minecraft.text.Text.literal("§c[RTX] Ray tracing disabled"));
                } else {
                    net.minecraft.client.MinecraftClient.getInstance().inGameHud.getChatHud()
                        .addMessage(net.minecraft.text.Text.literal("§a[RTX] Ray tracing enabled"));
                }
            }
            
            if (RTXModClient.getReloadShadersKeyBinding() != null && RTXModClient.getReloadShadersKeyBinding().wasPressed()) {
                renderer.getShaderManager().reloadShaders();
                net.minecraft.client.MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(net.minecraft.text.Text.literal("§e[RTX] Shaders reloaded"));
            }
        }
    }
}
