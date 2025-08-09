package com.rtxmod.rendering.shaders;

import com.rtxmod.RTXMod;
import org.lwjgl.opengl.GL46;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages RTX shaders including ray tracing, compute, and post-processing shaders
 */
public class RTXShaderManager {
    
    private final Map<String, RTXShaderProgram> shaderPrograms = new HashMap<>();
    private boolean initialized = false;
    
    // Shader program names
    public static final String RAY_TRACING_PROGRAM = "ray_tracing";
    public static final String TEMPORAL_ACCUMULATION_PROGRAM = "temporal_accumulation";
    public static final String DENOISING_PROGRAM = "denoising";
    public static final String TONE_MAPPING_PROGRAM = "tone_mapping";
    public static final String UPSCALING_PROGRAM = "upscaling";
    public static final String G_BUFFER_PROGRAM = "g_buffer";
    public static final String LIGHTING_PROGRAM = "lighting";
    
    public void initialize() {
        if (initialized) return;
        
        try {
            RTXMod.LOGGER.info("Initializing RTX Shader Manager...");
            
            // Load core RTX shaders
            loadRayTracingShaders();
            loadPostProcessingShaders();
            loadUtilityShaders();
            
            initialized = true;
            RTXMod.LOGGER.info("RTX Shader Manager initialization complete! Loaded {} programs", shaderPrograms.size());
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Failed to initialize RTX Shader Manager: ", e);
            initialized = false;
        }
    }
    
    private void loadRayTracingShaders() {
        // G-Buffer generation shader
        createShaderProgram(G_BUFFER_PROGRAM,
            "/assets/rtx-mod/shaders/gbuffer.vert",
            "/assets/rtx-mod/shaders/gbuffer.frag");
        
        // Main ray tracing compute shader
        createComputeShaderProgram(RAY_TRACING_PROGRAM,
            "/assets/rtx-mod/shaders/raytracing.comp");
        
        // Lighting shader for traditional rendering fallback
        createShaderProgram(LIGHTING_PROGRAM,
            "/assets/rtx-mod/shaders/lighting.vert",
            "/assets/rtx-mod/shaders/lighting.frag");
    }
    
    private void loadPostProcessingShaders() {
        // Temporal accumulation for progressive ray tracing
        createComputeShaderProgram(TEMPORAL_ACCUMULATION_PROGRAM,
            "/assets/rtx-mod/shaders/temporal_accumulation.comp");
        
        // AI-based denoising
        createComputeShaderProgram(DENOISING_PROGRAM,
            "/assets/rtx-mod/shaders/denoising.comp");
        
        // Tone mapping and HDR processing
        createShaderProgram(TONE_MAPPING_PROGRAM,
            "/assets/rtx-mod/shaders/tonemap.vert",
            "/assets/rtx-mod/shaders/tonemap.frag");
        
        // AI upscaling (DLSS-like)
        createComputeShaderProgram(UPSCALING_PROGRAM,
            "/assets/rtx-mod/shaders/upscaling.comp");
    }
    
    private void loadUtilityShaders() {
        // Add more utility shaders as needed
    }
    
    private void createShaderProgram(String name, String vertexPath, String fragmentPath) {
        try {
            String vertexSource = loadShaderSource(vertexPath);
            String fragmentSource = loadShaderSource(fragmentPath);
            
            RTXShaderProgram program = new RTXShaderProgram();
            program.createVertexShader(vertexSource);
            program.createFragmentShader(fragmentSource);
            program.link();
            
            shaderPrograms.put(name, program);
            RTXMod.LOGGER.debug("Loaded shader program: {}", name);
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Failed to load shader program {}: ", name, e);
        }
    }
    
    private void createComputeShaderProgram(String name, String computePath) {
        try {
            String computeSource = loadShaderSource(computePath);
            
            RTXShaderProgram program = new RTXShaderProgram();
            program.createComputeShader(computeSource);
            program.link();
            
            shaderPrograms.put(name, program);
            RTXMod.LOGGER.debug("Loaded compute shader program: {}", name);
            
        } catch (Exception e) {
            RTXMod.LOGGER.error("Failed to load compute shader program {}: ", name, e);
        }
    }
    
    private String loadShaderSource(String resourcePath) throws IOException {
        InputStream stream = RTXShaderManager.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            // Create a default shader if the resource doesn't exist
            return createDefaultShader(resourcePath);
        }
        
        StringBuilder source = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
        }
        
        return source.toString();
    }
    
    private String createDefaultShader(String resourcePath) {
        RTXMod.LOGGER.warn("Shader resource not found: {}, creating default", resourcePath);
        
        if (resourcePath.contains("vert")) {
            return createDefaultVertexShader();
        } else if (resourcePath.contains("frag")) {
            return createDefaultFragmentShader();
        } else if (resourcePath.contains("comp")) {
            return createDefaultComputeShader();
        }
        
        return "#version 460 core\nvoid main() {}\n";
    }
    
    private String createDefaultVertexShader() {
        return """
            #version 460 core
            
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec2 aTexCoord;
            layout (location = 2) in vec3 aNormal;
            
            uniform mat4 uModel;
            uniform mat4 uView;
            uniform mat4 uProjection;
            
            out vec2 vTexCoord;
            out vec3 vNormal;
            out vec3 vWorldPos;
            
            void main() {
                vec4 worldPos = uModel * vec4(aPos, 1.0);
                vWorldPos = worldPos.xyz;
                vTexCoord = aTexCoord;
                vNormal = normalize(mat3(uModel) * aNormal);
                
                gl_Position = uProjection * uView * worldPos;
            }
            """;
    }
    
    private String createDefaultFragmentShader() {
        return """
            #version 460 core
            
            in vec2 vTexCoord;
            in vec3 vNormal;
            in vec3 vWorldPos;
            
            layout (location = 0) out vec4 fragColor;
            layout (location = 1) out vec3 fragNormal;
            layout (location = 2) out vec4 fragMaterial;
            layout (location = 3) out vec2 fragMotionVector;
            
            uniform sampler2D uDiffuseTexture;
            uniform vec3 uCameraPos;
            
            void main() {
                vec4 diffuse = texture(uDiffuseTexture, vTexCoord);
                fragColor = diffuse;
                fragNormal = normalize(vNormal) * 0.5 + 0.5;
                fragMaterial = vec4(0.5, 0.0, 1.0, 0.0); // roughness, metallic, ao, emission
                fragMotionVector = vec2(0.0); // No motion for now
            }
            """;
    }
    
    private String createDefaultComputeShader() {
        return """
            #version 460 core
            
            layout (local_size_x = 16, local_size_y = 16) in;
            layout (rgba16f, binding = 0) uniform image2D img_output;
            
            uniform float uTime;
            
            void main() {
                ivec2 pixel_coords = ivec2(gl_GlobalInvocationID.xy);
                ivec2 dims = imageSize(img_output);
                
                if (pixel_coords.x >= dims.x || pixel_coords.y >= dims.y) {
                    return;
                }
                
                vec2 uv = vec2(pixel_coords) / vec2(dims);
                vec4 color = vec4(uv, 0.5, 1.0);
                
                imageStore(img_output, pixel_coords, color);
            }
            """;
    }
    
    public RTXShaderProgram getShaderProgram(String name) {
        return shaderPrograms.get(name);
    }
    
    public void useShaderProgram(String name) {
        RTXShaderProgram program = shaderPrograms.get(name);
        if (program != null) {
            program.bind();
        } else {
            RTXMod.LOGGER.warn("Shader program not found: {}", name);
        }
    }
    
    public void dispatchCompute(String programName, int workGroupX, int workGroupY, int workGroupZ) {
        RTXShaderProgram program = shaderPrograms.get(programName);
        if (program != null && program.isComputeShader()) {
            program.bind();
            GL46.glDispatchCompute(workGroupX, workGroupY, workGroupZ);
            GL46.glMemoryBarrier(GL46.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        } else {
            RTXMod.LOGGER.warn("Compute shader program not found or invalid: {}", programName);
        }
    }
    
    public void reloadShaders() {
        RTXMod.LOGGER.info("Reloading RTX shaders...");
        
        // Cleanup existing shaders
        cleanup();
        shaderPrograms.clear();
        
        // Reload all shaders
        initialize();
    }
    
    public void cleanup() {
        if (!initialized) return;
        
        RTXMod.LOGGER.info("Cleaning up RTX shaders...");
        
        for (RTXShaderProgram program : shaderPrograms.values()) {
            program.cleanup();
        }
        shaderPrograms.clear();
        
        initialized = false;
        RTXMod.LOGGER.info("RTX shader cleanup complete");
    }
    
    // Utility methods
    public boolean isInitialized() {
        return initialized;
    }
    
    public int getShaderCount() {
        return shaderPrograms.size();
    }
    
    public boolean hasShader(String name) {
        return shaderPrograms.containsKey(name);
    }
}
