package com.rtxmod.config;

/**
 * Configuration class for RTX settings
 */
public class RTXConfig {
    
    // Ray tracing settings
    public boolean enableRayTracing = false;
    public int maxRayBounces = 3;
    public int samplesPerPixel = 4;
    public float rayTracingDistance = 128.0f;
    
    // Global illumination settings
    public boolean enableGlobalIllumination = true;
    public float globalIlluminationStrength = 1.0f;
    public int giSamples = 16;
    
    // Reflection settings
    public boolean enableReflections = true;
    public float reflectionStrength = 1.0f;
    public int reflectionQuality = 1; // 0 = low, 1 = medium, 2 = high
    
    // Ambient occlusion settings
    public boolean enableAmbientOcclusion = true;
    public float aoStrength = 0.8f;
    public float aoRadius = 2.0f;
    
    // Shadow settings
    public boolean enableRayTracedShadows = true;
    public float shadowStrength = 1.0f;
    public int shadowSamples = 8;
    
    // Performance settings
    public boolean enableTemporalUpsampling = true;
    public boolean enableDenoising = true;
    public int renderScale = 100; // Percentage of screen resolution
    
    // Debug settings
    public boolean showDebugInfo = false;
    public boolean wireframeMode = false;
    public boolean showBoundingBoxes = false;
    
    public RTXConfig() {
        // Load configuration from file if it exists
        loadConfig();
    }
    
    public void loadConfig() {
        // TODO: Implement config file loading
        // For now, use default values
    }
    
    public void saveConfig() {
        // TODO: Implement config file saving
    }
    
    public void resetToDefaults() {
        enableRayTracing = false;
        maxRayBounces = 3;
        samplesPerPixel = 4;
        rayTracingDistance = 128.0f;
        
        enableGlobalIllumination = true;
        globalIlluminationStrength = 1.0f;
        giSamples = 16;
        
        enableReflections = true;
        reflectionStrength = 1.0f;
        reflectionQuality = 1;
        
        enableAmbientOcclusion = true;
        aoStrength = 0.8f;
        aoRadius = 2.0f;
        
        enableRayTracedShadows = true;
        shadowStrength = 1.0f;
        shadowSamples = 8;
        
        enableTemporalUpsampling = true;
        enableDenoising = true;
        renderScale = 100;
        
        showDebugInfo = false;
        wireframeMode = false;
        showBoundingBoxes = false;
    }
    
    // Getters and setters
    public boolean isRayTracingEnabled() {
        return enableRayTracing;
    }
    
    public void setRayTracingEnabled(boolean enabled) {
        this.enableRayTracing = enabled;
    }
    
    public int getMaxRayBounces() {
        return maxRayBounces;
    }
    
    public void setMaxRayBounces(int bounces) {
        this.maxRayBounces = Math.max(1, Math.min(bounces, 10));
    }
    
    public int getSamplesPerPixel() {
        return samplesPerPixel;
    }
    
    public void setSamplesPerPixel(int samples) {
        this.samplesPerPixel = Math.max(1, Math.min(samples, 64));
    }
}
