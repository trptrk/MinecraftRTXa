package com.rtxmod.util;

import com.rtxmod.RTXMod;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GLCapabilities;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to check RTX and ray tracing capabilities
 */
public class RTXCapabilities {
    
    private boolean hardwareRayTracingSupported = false;
    private boolean nvRayTracingSupported = false;
    private boolean amdRayTracingSupported = false;
    private boolean intelRayTracingSupported = false;
    private boolean vulkanRayTracingSupported = false;
    
    private String gpuVendor = "Unknown";
    private String gpuRenderer = "Unknown";
    private String openglVersion = "Unknown";
    
    private Set<String> supportedExtensions = new HashSet<>();
    
    public RTXCapabilities() {
        checkCapabilities();
    }
    
    private void checkCapabilities() {
        try {
            GLCapabilities caps = GL.getCapabilities();
            
            // Get GPU information
            gpuVendor = GL46.glGetString(GL46.GL_VENDOR);
            gpuRenderer = GL46.glGetString(GL46.GL_RENDERER);
            openglVersion = GL46.glGetString(GL46.GL_VERSION);
            
            RTXMod.LOGGER.info("GPU Vendor: {}", gpuVendor);
            RTXMod.LOGGER.info("GPU Renderer: {}", gpuRenderer);
            RTXMod.LOGGER.info("OpenGL Version: {}", openglVersion);
            
            // Get supported extensions
            int numExtensions = GL46.glGetInteger(GL46.GL_NUM_EXTENSIONS);
            for (int i = 0; i < numExtensions; i++) {
                String extension = GL46.glGetStringi(GL46.GL_EXTENSIONS, i);
                supportedExtensions.add(extension);
            }
            
            // Check for NVIDIA RTX support
            if (gpuVendor.toLowerCase().contains("nvidia")) {
                checkNvidiaRTXSupport(caps);
            }
            
            // Check for AMD RDNA ray tracing support  
            if (gpuVendor.toLowerCase().contains("amd")) {
                checkAMDRayTracingSupport(caps);
            }
            
            // Check for Intel Arc ray tracing support
            if (gpuVendor.toLowerCase().contains("intel")) {
                checkIntelRayTracingSupport(caps);
            }
            
            // Check for Vulkan ray tracing support
            checkVulkanRayTracingSupport();
            
            // Overall hardware ray tracing support
            hardwareRayTracingSupported = nvRayTracingSupported || 
                                        amdRayTracingSupported || 
                                        intelRayTracingSupported;
            
            logCapabilities();
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Error checking RTX capabilities: ", e);
        }
    }
    
    private void checkNvidiaRTXSupport(GLCapabilities caps) {
        // Check for RTX-specific extensions
        boolean hasRTXExtensions = 
            supportedExtensions.contains("GL_NV_ray_tracing") ||
            supportedExtensions.contains("GL_NVX_ray_tracing") ||
            supportedExtensions.contains("GL_NV_mesh_shader") ||
            caps.GL_NV_ray_tracing;
            
        // Check GPU model for RTX/GTX series
        String renderer = gpuRenderer.toLowerCase();
        boolean isRTXGPU = renderer.contains("rtx") || 
                          renderer.contains("geforce rtx") ||
                          renderer.contains("quadro rtx") ||
                          renderer.contains("tesla v100") ||
                          renderer.contains("titan rtx");
        
        // GTX 16 series and some GTX 10 series support ray tracing in software
        boolean isCompatibleGTX = renderer.contains("gtx 16") || 
                                 renderer.contains("gtx 1660") ||
                                 renderer.contains("gtx 1070") ||
                                 renderer.contains("gtx 1080");
        
        nvRayTracingSupported = hasRTXExtensions && (isRTXGPU || isCompatibleGTX);
        
        RTXMod.LOGGER.info("NVIDIA RTX Support: {} (RTX GPU: {}, Extensions: {})", 
            nvRayTracingSupported, isRTXGPU, hasRTXExtensions);
    }
    
    private void checkAMDRayTracingSupport(GLCapabilities caps) {
        // Check for AMD RDNA2/RDNA3 ray tracing support
        String renderer = gpuRenderer.toLowerCase();
        boolean isRDNA2Plus = renderer.contains("rx 6") || 
                             renderer.contains("rx 7") ||
                             renderer.contains("radeon rx 6") ||
                             renderer.contains("radeon rx 7");
        
        // Check for ray tracing extensions
        boolean hasRayTracingExtensions = 
            supportedExtensions.contains("GL_AMD_ray_tracing") ||
            supportedExtensions.contains("GL_EXT_ray_tracing");
        
        amdRayTracingSupported = isRDNA2Plus && hasRayTracingExtensions;
        
        RTXMod.LOGGER.info("AMD Ray Tracing Support: {} (RDNA2+: {}, Extensions: {})", 
            amdRayTracingSupported, isRDNA2Plus, hasRayTracingExtensions);
    }
    
    private void checkIntelRayTracingSupport(GLCapabilities caps) {
        // Check for Intel Arc ray tracing support
        String renderer = gpuRenderer.toLowerCase();
        boolean isArcGPU = renderer.contains("arc") || 
                          renderer.contains("intel arc") ||
                          renderer.contains("dg2");
        
        // Check for ray tracing extensions
        boolean hasRayTracingExtensions = 
            supportedExtensions.contains("GL_INTEL_ray_tracing") ||
            supportedExtensions.contains("GL_EXT_ray_tracing");
        
        intelRayTracingSupported = isArcGPU && hasRayTracingExtensions;
        
        RTXMod.LOGGER.info("Intel Ray Tracing Support: {} (Arc GPU: {}, Extensions: {})", 
            intelRayTracingSupported, isArcGPU, hasRayTracingExtensions);
    }
    
    private void checkVulkanRayTracingSupport() {
        // Note: This is a simplified check. In a full implementation,
        // you would also check Vulkan capabilities for VK_KHR_ray_tracing_pipeline
        vulkanRayTracingSupported = supportedExtensions.contains("GL_EXT_ray_tracing") ||
                                   supportedExtensions.contains("GL_KHR_ray_tracing");
    }
    
    private void logCapabilities() {
        RTXMod.LOGGER.info("=== RTX Capabilities Summary ===");
        RTXMod.LOGGER.info("Hardware Ray Tracing: {}", hardwareRayTracingSupported);
        RTXMod.LOGGER.info("NVIDIA RTX: {}", nvRayTracingSupported);
        RTXMod.LOGGER.info("AMD Ray Tracing: {}", amdRayTracingSupported);
        RTXMod.LOGGER.info("Intel Ray Tracing: {}", intelRayTracingSupported);
        RTXMod.LOGGER.info("Vulkan Ray Tracing: {}", vulkanRayTracingSupported);
        
        // Log key extensions for debugging
        RTXMod.LOGGER.debug("Key extensions found:");
        for (String ext : supportedExtensions) {
            if (ext.toLowerCase().contains("ray") || 
                ext.toLowerCase().contains("rtx") ||
                ext.toLowerCase().contains("mesh")) {
                RTXMod.LOGGER.debug("  - {}", ext);
            }
        }
    }
    
    // Public getters
    public boolean checkRayTracingSupport() {
        return hardwareRayTracingSupported;
    }
    
    public boolean isHardwareRayTracingSupported() {
        return hardwareRayTracingSupported;
    }
    
    public boolean isNvidiaRTXSupported() {
        return nvRayTracingSupported;
    }
    
    public boolean isAMDRayTracingSupported() {
        return amdRayTracingSupported;
    }
    
    public boolean isIntelRayTracingSupported() {
        return intelRayTracingSupported;
    }
    
    public boolean isVulkanRayTracingSupported() {
        return vulkanRayTracingSupported;
    }
    
    public String getGPUVendor() {
        return gpuVendor;
    }
    
    public String getGPURenderer() {
        return gpuRenderer;
    }
    
    public String getOpenGLVersion() {
        return openglVersion;
    }
    
    public Set<String> getSupportedExtensions() {
        return new HashSet<>(supportedExtensions);
    }
    
    public boolean hasExtension(String extension) {
        return supportedExtensions.contains(extension);
    }
    
    // Feature capability checks
    public boolean supportsHardwareMeshShaders() {
        return supportedExtensions.contains("GL_NV_mesh_shader") ||
               supportedExtensions.contains("GL_EXT_mesh_shader");
    }
    
    public boolean supportsVariableRateShading() {
        return supportedExtensions.contains("GL_NV_shading_rate_image") ||
               supportedExtensions.contains("GL_EXT_fragment_shading_rate");
    }
    
    public boolean supportsRayQuery() {
        return supportedExtensions.contains("GL_EXT_ray_query") ||
               supportedExtensions.contains("GL_NV_ray_tracing");
    }
    
    public boolean supportsDLSS() {
        // DLSS is NVIDIA-specific and requires RTX support
        return nvRayTracingSupported && gpuRenderer.toLowerCase().contains("rtx");
    }
    
    public boolean supportsFSR() {
        // AMD FSR can work on various GPUs, but works best on RDNA+
        return amdRayTracingSupported || nvRayTracingSupported;
    }
}
