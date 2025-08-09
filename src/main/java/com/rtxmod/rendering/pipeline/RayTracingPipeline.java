package com.rtxmod.rendering.pipeline;

import com.rtxmod.RTXMod;
import com.rtxmod.rendering.buffers.RTXFrameBuffer;
import com.rtxmod.rendering.scene.SceneManager;
import com.rtxmod.rendering.shaders.RTXShaderManager;
import com.rtxmod.rendering.shaders.RTXShaderProgram;

import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL46;

/**
 * Hardware-accelerated ray tracing rendering pipeline
 */
public class RayTracingPipeline {
    
    private final RTXShaderManager shaderManager;
    private final SceneManager sceneManager;
    
    private boolean initialized = false;
    private int frameCounter = 0;
    
    public RayTracingPipeline(RTXShaderManager shaderManager, SceneManager sceneManager) {
        this.shaderManager = shaderManager;
        this.sceneManager = sceneManager;
    }
    
    public void initialize() {
        if (initialized) return;
        
        try {
            RTXMod.LOGGER.info("Initializing Ray Tracing Pipeline...");
            
            // Initialize any pipeline-specific resources here
            
            initialized = true;
            RTXMod.LOGGER.info("Ray Tracing Pipeline initialization complete!");
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Failed to initialize Ray Tracing Pipeline: ", e);
            initialized = false;
        }
    }
    
    public void render(Matrix4f viewMatrix, Matrix4f projectionMatrix, RTXFrameBuffer frameBuffer, float tickDelta) {
        if (!initialized) return;
        
        frameCounter++;
        
        // Get the ray tracing compute shader
        RTXShaderProgram rayTracingShader = shaderManager.getShaderProgram(RTXShaderManager.RAY_TRACING_PROGRAM);
        if (rayTracingShader == null) {
            RTXMod.LOGGER.warn("Ray tracing shader not available, skipping render");
            return;
        }
        
        // Bind the shader and set uniforms
        rayTracingShader.bind();
        
        // Set camera matrices
        rayTracingShader.setUniform("uViewMatrix", viewMatrix);
        rayTracingShader.setUniform("uProjectionMatrix", projectionMatrix);
        
        // Set rendering parameters
        rayTracingShader.setUniform("uTime", (float) (System.currentTimeMillis() % 1000000) / 1000.0f);
        rayTracingShader.setUniform("uFrame", frameCounter);
        rayTracingShader.setUniform("uTickDelta", tickDelta);
        
        // Set screen dimensions
        rayTracingShader.setUniform("uScreenWidth", frameBuffer.getWidth());
        rayTracingShader.setUniform("uScreenHeight", frameBuffer.getHeight());
        
        // Bind output images
        rayTracingShader.bindImage("img_output", frameBuffer.getColorTextureId(), 0, GL46.GL_WRITE_ONLY, GL46.GL_RGBA16F);
        rayTracingShader.bindImage("img_normal", frameBuffer.getNormalTextureId(), 1, GL46.GL_WRITE_ONLY, GL46.GL_RGB16F);
        rayTracingShader.bindImage("img_material", frameBuffer.getMaterialTextureId(), 2, GL46.GL_WRITE_ONLY, GL46.GL_RGBA8);
        rayTracingShader.bindImage("img_motion", frameBuffer.getMotionVectorTextureId(), 3, GL46.GL_WRITE_ONLY, GL46.GL_RG16F);
        
        // Dispatch the compute shader
        int workGroupsX = (frameBuffer.getWidth() + 15) / 16;   // 16x16 local work group size
        int workGroupsY = (frameBuffer.getHeight() + 15) / 16;
        
        GL46.glDispatchCompute(workGroupsX, workGroupsY, 1);
        
        // Ensure all writes are complete before continuing
        GL46.glMemoryBarrier(GL46.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL46.GL_TEXTURE_FETCH_BARRIER_BIT);
        
        rayTracingShader.unbind();
    }
    
    public void onResize(int width, int height) {
        // Handle pipeline resize if needed
        RTXMod.LOGGER.debug("Ray tracing pipeline resize: {}x{}", width, height);
    }
    
    public void cleanup() {
        if (!initialized) return;
        
        RTXMod.LOGGER.info("Cleaning up Ray Tracing Pipeline...");
        
        // Cleanup pipeline-specific resources
        
        initialized = false;
        RTXMod.LOGGER.info("Ray Tracing Pipeline cleanup complete");
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public int getFrameCounter() {
        return frameCounter;
    }
}
