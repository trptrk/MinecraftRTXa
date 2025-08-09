package com.rtxmod.rendering.buffers;

import com.rtxmod.RTXMod;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * Advanced frame buffer for RTX rendering with multiple render targets
 * Supports HDR, depth, and additional G-buffer attachments
 */
public class RTXFrameBuffer {
    
    public static final int COLOR_ATTACHMENT = 0;
    public static final int NORMAL_ATTACHMENT = 1;
    public static final int MATERIAL_ATTACHMENT = 2;
    public static final int MOTION_VECTOR_ATTACHMENT = 3;
    public static final int DEPTH_ATTACHMENT = 4;
    
    private final int width;
    private final int height;
    private final boolean useHDR;
    
    private int framebufferId;
    private int colorTextureId;
    private int normalTextureId;
    private int materialTextureId;
    private int motionVectorTextureId;
    private int depthTextureId;
    private int depthRenderbufferId;
    
    private boolean initialized = false;
    
    public RTXFrameBuffer(int width, int height, boolean useHDR) {
        this.width = width;
        this.height = height;
        this.useHDR = useHDR;
        
        initialize();
    }
    
    private void initialize() {
        try {
            // Generate framebuffer
            framebufferId = GL46.glGenFramebuffers();
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, framebufferId);
            
            // Create color attachment (main render target)
            createColorAttachment();
            
            // Create normal attachment for ray tracing
            createNormalAttachment();
            
            // Create material attachment (roughness, metallic, etc.)
            createMaterialAttachment();
            
            // Create motion vector attachment for temporal effects
            createMotionVectorAttachment();
            
            // Create depth attachment
            createDepthAttachment();
            
            // Set draw buffers
            setDrawBuffers();
            
            // Check framebuffer completeness
            int status = GL46.glCheckFramebufferStatus(GL46.GL_FRAMEBUFFER);
            if (status != GL46.GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Framebuffer not complete: " + status);
            }
            
            // Unbind framebuffer
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
            
            initialized = true;
            RTXMod.LOGGER.info("Created RTX framebuffer {}x{} (HDR: {})", width, height, useHDR);
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Failed to create RTX framebuffer: ", e);
            cleanup();
        }
    }
    
    private void createColorAttachment() {
        colorTextureId = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, colorTextureId);
        
        // Use HDR format if enabled
        int internalFormat = useHDR ? GL46.GL_RGBA16F : GL46.GL_RGBA8;
        int format = GL46.GL_RGBA;
        int type = useHDR ? GL46.GL_HALF_FLOAT : GL46.GL_UNSIGNED_BYTE;
        
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, 0);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, GL46.GL_TEXTURE_2D, colorTextureId, 0);
    }
    
    private void createNormalAttachment() {
        normalTextureId = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, normalTextureId);
        
        // Use high precision for normals
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGB16F, width, height, 0, GL46.GL_RGB, GL46.GL_HALF_FLOAT, 0);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT1, GL46.GL_TEXTURE_2D, normalTextureId, 0);
    }
    
    private void createMaterialAttachment() {
        materialTextureId = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, materialTextureId);
        
        // RGBA8 for material properties (roughness, metallic, ao, emission)
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA8, width, height, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, 0);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT2, GL46.GL_TEXTURE_2D, materialTextureId, 0);
    }
    
    private void createMotionVectorAttachment() {
        motionVectorTextureId = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, motionVectorTextureId);
        
        // RG16F for motion vectors (x, y velocity)
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RG16F, width, height, 0, GL46.GL_RG, GL46.GL_HALF_FLOAT, 0);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT3, GL46.GL_TEXTURE_2D, motionVectorTextureId, 0);
    }
    
    private void createDepthAttachment() {
        // Create depth texture for sampling
        depthTextureId = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, depthTextureId);
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_DEPTH_COMPONENT32F, width, height, 0, GL46.GL_DEPTH_COMPONENT, GL46.GL_FLOAT, 0);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        
        GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_DEPTH_ATTACHMENT, GL46.GL_TEXTURE_2D, depthTextureId, 0);
    }
    
    private void setDrawBuffers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer drawBuffers = stack.mallocInt(4);
            drawBuffers.put(GL46.GL_COLOR_ATTACHMENT0);  // Color
            drawBuffers.put(GL46.GL_COLOR_ATTACHMENT1);  // Normal
            drawBuffers.put(GL46.GL_COLOR_ATTACHMENT2);  // Material
            drawBuffers.put(GL46.GL_COLOR_ATTACHMENT3);  // Motion Vector
            drawBuffers.flip();
            
            GL46.glDrawBuffers(drawBuffers);
        }
    }
    
    public void bind() {
        if (!initialized) return;
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, framebufferId);
        GL46.glViewport(0, 0, width, height);
    }
    
    public void unbind() {
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
    }
    
    public void clear() {
        bind();
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT);
    }
    
    public void bindColorTexture(int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, colorTextureId);
    }
    
    public void bindNormalTexture(int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, normalTextureId);
    }
    
    public void bindMaterialTexture(int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, materialTextureId);
    }
    
    public void bindMotionVectorTexture(int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, motionVectorTextureId);
    }
    
    public void bindDepthTexture(int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, depthTextureId);
    }
    
    public void bindAllTextures(int startUnit) {
        bindColorTexture(startUnit);
        bindNormalTexture(startUnit + 1);
        bindMaterialTexture(startUnit + 2);
        bindMotionVectorTexture(startUnit + 3);
        bindDepthTexture(startUnit + 4);
    }
    
    private void cleanup() {
        if (colorTextureId != 0) {
            GL46.glDeleteTextures(colorTextureId);
            colorTextureId = 0;
        }
        if (normalTextureId != 0) {
            GL46.glDeleteTextures(normalTextureId);
            normalTextureId = 0;
        }
        if (materialTextureId != 0) {
            GL46.glDeleteTextures(materialTextureId);
            materialTextureId = 0;
        }
        if (motionVectorTextureId != 0) {
            GL46.glDeleteTextures(motionVectorTextureId);
            motionVectorTextureId = 0;
        }
        if (depthTextureId != 0) {
            GL46.glDeleteTextures(depthTextureId);
            depthTextureId = 0;
        }
        if (depthRenderbufferId != 0) {
            GL46.glDeleteRenderbuffers(depthRenderbufferId);
            depthRenderbufferId = 0;
        }
        if (framebufferId != 0) {
            GL46.glDeleteFramebuffers(framebufferId);
            framebufferId = 0;
        }
    }
    
    public void delete() {
        cleanup();
        initialized = false;
        RTXMod.LOGGER.debug("Deleted RTX framebuffer");
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isUseHDR() { return useHDR; }
    public boolean isInitialized() { return initialized; }
    public int getFramebufferId() { return framebufferId; }
    public int getColorTextureId() { return colorTextureId; }
    public int getNormalTextureId() { return normalTextureId; }
    public int getMaterialTextureId() { return materialTextureId; }
    public int getMotionVectorTextureId() { return motionVectorTextureId; }
    public int getDepthTextureId() { return depthTextureId; }
}
