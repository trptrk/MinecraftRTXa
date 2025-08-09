package com.rtxmod.rendering;

import com.rtxmod.RTXMod;
import com.rtxmod.config.RTXConfig;
import com.rtxmod.rendering.pipeline.RayTracingPipeline;
import com.rtxmod.rendering.pipeline.PostProcessingPipeline;
import com.rtxmod.rendering.buffers.RTXFrameBuffer;
import com.rtxmod.rendering.shaders.RTXShaderManager;
import com.rtxmod.rendering.scene.SceneManager;
import com.rtxmod.util.RTXCapabilities;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.math.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;

/**
 * Main RTX Renderer class
 * Handles hardware-accelerated ray tracing for Minecraft
 */
public class RTXRenderer {
    
    private static RTXRenderer instance;
    
    private final RTXConfig config;
    private final RTXShaderManager shaderManager;
    private final RayTracingPipeline rayTracingPipeline;
    private final PostProcessingPipeline postProcessingPipeline;
    private final SceneManager sceneManager;
    private final RTXCapabilities capabilities;
    
    private RTXFrameBuffer primaryFrameBuffer;
    private RTXFrameBuffer temporalFrameBuffer;
    private boolean initialized = false;
    private boolean rtxSupported = false;
    
    // Performance tracking
    private long lastFrameTime = 0;
    private float averageFrameTime = 16.67f; // ~60 FPS default
    private int frameCount = 0;
    
    public RTXRenderer() {
        instance = this;
        this.config = new RTXConfig();
        this.capabilities = new RTXCapabilities();
        this.shaderManager = new RTXShaderManager();
        this.sceneManager = new SceneManager();
        
        // Initialize pipelines
        this.rayTracingPipeline = new RayTracingPipeline(shaderManager, sceneManager);
        this.postProcessingPipeline = new PostProcessingPipeline(shaderManager);
        
        RTXMod.LOGGER.info("RTX Renderer created");
    }
    
    public static RTXRenderer getInstance() {
        return instance;
    }
    
    public void initialize() {
        if (initialized) return;
        
        try {
            RTXMod.LOGGER.info("Initializing RTX Renderer...");
            
            // Check for RTX/Ray tracing support
            rtxSupported = capabilities.checkRayTracingSupport();
            
            if (!rtxSupported) {
                RTXMod.LOGGER.warn("Hardware ray tracing not supported on this system");
                RTXMod.LOGGER.info("Falling back to software ray tracing (limited functionality)");
            } else {
                RTXMod.LOGGER.info("Hardware ray tracing supported! Enabling RTX features");
            }
            
            // Initialize shader manager
            shaderManager.initialize();
            
            // Initialize frame buffers
            createFrameBuffers();
            
            // Initialize pipelines
            rayTracingPipeline.initialize();
            postProcessingPipeline.initialize();
            
            // Initialize scene manager
            sceneManager.initialize();
            
            initialized = true;
            RTXMod.LOGGER.info("RTX Renderer initialization complete!");
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Failed to initialize RTX Renderer: ", e);
            initialized = false;
        }
    }
    
    public void render(Matrix4f viewMatrix, Matrix4f projectionMatrix, float tickDelta) {
        if (!initialized || !config.isRayTracingEnabled()) {
            return;
        }
        
        long frameStart = System.nanoTime();
        
        try {
            // Update scene data
            sceneManager.update(tickDelta);
            
            // Primary ray tracing pass
            renderRayTracedFrame(viewMatrix, projectionMatrix, tickDelta);
            
            // Post-processing pipeline
            postProcessingPipeline.process(primaryFrameBuffer, temporalFrameBuffer, tickDelta);
            
            // Update performance metrics
            updatePerformanceMetrics(frameStart);
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Error during RTX rendering: ", e);
        }
    }
    
    private void renderRayTracedFrame(Matrix4f viewMatrix, Matrix4f projectionMatrix, float tickDelta) {
        // Bind primary frame buffer
        primaryFrameBuffer.bind();
        
        // Clear buffers
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT);
        
        // Set up ray tracing pipeline
        rayTracingPipeline.render(viewMatrix, projectionMatrix, primaryFrameBuffer, tickDelta);
        
        // Unbind frame buffer
        primaryFrameBuffer.unbind();
    }
    
    private void createFrameBuffers() {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        
        // Apply render scale
        int scaledWidth = (int)(width * (config.renderScale / 100.0f));
        int scaledHeight = (int)(height * (config.renderScale / 100.0f));
        
        primaryFrameBuffer = new RTXFrameBuffer(scaledWidth, scaledHeight, true);
        temporalFrameBuffer = new RTXFrameBuffer(scaledWidth, scaledHeight, true);
        
        RTXMod.LOGGER.info("Created RTX frame buffers: {}x{} (scale: {}%)", 
            scaledWidth, scaledHeight, config.renderScale);
    }
    
    public void onWindowResize(int width, int height) {
        if (!initialized) return;
        
        // Recreate frame buffers with new dimensions
        if (primaryFrameBuffer != null) {
            primaryFrameBuffer.delete();
        }
        if (temporalFrameBuffer != null) {
            temporalFrameBuffer.delete();
        }
        
        createFrameBuffers();
        
        // Notify pipelines of resize
        rayTracingPipeline.onResize(width, height);
        postProcessingPipeline.onResize(width, height);
    }
    
    private void updatePerformanceMetrics(long frameStart) {
        frameCount++;
        lastFrameTime = System.nanoTime() - frameStart;
        
        // Update rolling average
        float frameTimeMs = lastFrameTime / 1_000_000.0f;
        averageFrameTime = (averageFrameTime * 0.9f) + (frameTimeMs * 0.1f);
        
        // Log performance every 60 frames
        if (frameCount % 60 == 0) {
            float fps = 1000.0f / averageFrameTime;
            RTXMod.LOGGER.debug("RTX Performance: {:.1f} FPS ({:.2f}ms)", fps, averageFrameTime);
        }
    }
    
    public void cleanup() {
        if (!initialized) return;
        
        RTXMod.LOGGER.info("Cleaning up RTX Renderer...");
        
        try {
            // Cleanup pipelines
            if (rayTracingPipeline != null) {
                rayTracingPipeline.cleanup();
            }
            if (postProcessingPipeline != null) {
                postProcessingPipeline.cleanup();
            }
            
            // Cleanup frame buffers
            if (primaryFrameBuffer != null) {
                primaryFrameBuffer.delete();
            }
            if (temporalFrameBuffer != null) {
                temporalFrameBuffer.delete();
            }
            
            // Cleanup managers
            if (sceneManager != null) {
                sceneManager.cleanup();
            }
            if (shaderManager != null) {
                shaderManager.cleanup();
            }
            
            initialized = false;
            RTXMod.LOGGER.info("RTX Renderer cleanup complete");
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Error during RTX cleanup: ", e);
        }
    }
    
    // Getters
    public boolean isInitialized() { return initialized; }
    public boolean isRtxSupported() { return rtxSupported; }
    public RTXConfig getConfig() { return config; }
    public RTXShaderManager getShaderManager() { return shaderManager; }
    public SceneManager getSceneManager() { return sceneManager; }
    public float getAverageFrameTime() { return averageFrameTime; }
    public RTXCapabilities getCapabilities() { return capabilities; }
    
    // Debug information
    public String getDebugInfo() {
        if (!initialized) return "RTX Renderer: Not initialized";
        
        float fps = 1000.0f / averageFrameTime;
        return String.format("RTX Renderer: %.1f FPS | RTX: %s | Samples: %d | Bounces: %d",
            fps,
            rtxSupported ? "HW" : "SW",
            config.getSamplesPerPixel(),
            config.getMaxRayBounces()
        );
    }
}
