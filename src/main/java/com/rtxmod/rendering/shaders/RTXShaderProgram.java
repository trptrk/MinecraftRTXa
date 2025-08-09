package com.rtxmod.rendering.shaders;

import com.rtxmod.RTXMod;
import org.lwjgl.opengl.GL46;
import org.joml.*;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for OpenGL shader programs with RTX-specific features
 */
public class RTXShaderProgram {
    
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private int computeShaderId;
    
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private boolean isCompute = false;
    private boolean linked = false;
    
    public RTXShaderProgram() {
        programId = GL46.glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }
    }
    
    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL46.GL_VERTEX_SHADER);
    }
    
    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL46.GL_FRAGMENT_SHADER);
    }
    
    public void createComputeShader(String shaderCode) throws Exception {
        computeShaderId = createShader(shaderCode, GL46.GL_COMPUTE_SHADER);
        isCompute = true;
    }
    
    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = GL46.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }
        
        GL46.glShaderSource(shaderId, shaderCode);
        GL46.glCompileShader(shaderId);
        
        if (GL46.glGetShaderi(shaderId, GL46.GL_COMPILE_STATUS) == 0) {
            String log = GL46.glGetShaderInfoLog(shaderId, 1024);
            throw new Exception("Error compiling Shader code: " + log + " for shader type: " + shaderType);
        }
        
        GL46.glAttachShader(programId, shaderId);
        return shaderId;
    }
    
    public void link() throws Exception {
        GL46.glLinkProgram(programId);
        if (GL46.glGetProgrami(programId, GL46.GL_LINK_STATUS) == 0) {
            String log = GL46.glGetProgramInfoLog(programId, 1024);
            throw new Exception("Error linking Shader code: " + log);
        }
        
        if (vertexShaderId != 0) {
            GL46.glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            GL46.glDetachShader(programId, fragmentShaderId);
        }
        if (computeShaderId != 0) {
            GL46.glDetachShader(programId, computeShaderId);
        }
        
        linked = true;
        
        // Validate program
        GL46.glValidateProgram(programId);
        if (GL46.glGetProgrami(programId, GL46.GL_VALIDATE_STATUS) == 0) {
            RTXMod.LOGGER.warn("Warning validating Shader code: {}", GL46.glGetProgramInfoLog(programId, 1024));
        }
    }
    
    public void bind() {
        GL46.glUseProgram(programId);
    }
    
    public void unbind() {
        GL46.glUseProgram(0);
    }
    
    public void cleanup() {
        unbind();
        if (programId != 0) {
            GL46.glDeleteProgram(programId);
            programId = 0;
        }
    }
    
    // Uniform setters
    public void setUniform(String uniformName, Matrix4f value) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            GL46.glUniformMatrix4fv(getUniformLocation(uniformName), false, buffer);
        }
    }
    
    public void setUniform(String uniformName, Matrix3f value) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            value.get(buffer);
            GL46.glUniformMatrix3fv(getUniformLocation(uniformName), false, buffer);
        }
    }
    
    public void setUniform(String uniformName, Vector4f value) {
        GL46.glUniform4f(getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }
    
    public void setUniform(String uniformName, Vector3f value) {
        GL46.glUniform3f(getUniformLocation(uniformName), value.x, value.y, value.z);
    }
    
    public void setUniform(String uniformName, Vector2f value) {
        GL46.glUniform2f(getUniformLocation(uniformName), value.x, value.y);
    }
    
    public void setUniform(String uniformName, float value) {
        GL46.glUniform1f(getUniformLocation(uniformName), value);
    }
    
    public void setUniform(String uniformName, int value) {
        GL46.glUniform1i(getUniformLocation(uniformName), value);
    }
    
    public void setUniform(String uniformName, boolean value) {
        GL46.glUniform1i(getUniformLocation(uniformName), value ? 1 : 0);
    }
    
    public void setUniform(String uniformName, float[] values) {
        GL46.glUniform1fv(getUniformLocation(uniformName), values);
    }
    
    public void setUniform(String uniformName, int[] values) {
        GL46.glUniform1iv(getUniformLocation(uniformName), values);
    }
    
    // Array uniforms
    public void setUniform(String uniformName, Vector3f[] values) {
        int location = getUniformLocation(uniformName);
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(values.length * 3);
            for (Vector3f value : values) {
                value.get(buffer);
                buffer.position(buffer.position() + 3);
            }
            buffer.flip();
            GL46.glUniform3fv(location, buffer);
        }
    }
    
    public void setUniform(String uniformName, Matrix4f[] values) {
        int location = getUniformLocation(uniformName);
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(values.length * 16);
            for (Matrix4f value : values) {
                value.get(buffer);
                buffer.position(buffer.position() + 16);
            }
            buffer.flip();
            GL46.glUniformMatrix4fv(location, false, buffer);
        }
    }
    
    // Texture binding
    public void bindTexture(String uniformName, int textureId, int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
        setUniform(uniformName, unit);
    }
    
    public void bindTextureArray(String uniformName, int textureId, int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D_ARRAY, textureId);
        setUniform(uniformName, unit);
    }
    
    public void bindCubemap(String uniformName, int textureId, int unit) {
        GL46.glActiveTexture(GL46.GL_TEXTURE0 + unit);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureId);
        setUniform(uniformName, unit);
    }
    
    // Image binding for compute shaders
    public void bindImage(String uniformName, int textureId, int unit, int access, int format) {
        GL46.glBindImageTexture(unit, textureId, 0, false, 0, access, format);
        setUniform(uniformName, unit);
    }
    
    // Shader storage buffer binding
    public void bindSSBO(int ssboId, int binding) {
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, binding, ssboId);
    }
    
    // Uniform buffer binding
    public void bindUBO(int uboId, int binding) {
        GL46.glBindBufferBase(GL46.GL_UNIFORM_BUFFER, binding, uboId);
    }
    
    private int getUniformLocation(String uniformName) {
        return uniformLocations.computeIfAbsent(uniformName, name -> {
            int location = GL46.glGetUniformLocation(programId, name);
            if (location == -1) {
                RTXMod.LOGGER.debug("Uniform '{}' not found in shader program {}", name, programId);
            }
            return location;
        });
    }
    
    // Utility methods for compute shaders
    public void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ) {
        if (!isCompute) {
            RTXMod.LOGGER.warn("Trying to dispatch compute on non-compute shader program");
            return;
        }
        bind();
        GL46.glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
    }
    
    public void memoryBarrier(int barriers) {
        GL46.glMemoryBarrier(barriers);
    }
    
    // Getters
    public int getProgramId() {
        return programId;
    }
    
    public boolean isLinked() {
        return linked;
    }
    
    public boolean isComputeShader() {
        return isCompute;
    }
    
    // Debug methods
    public String getProgramInfoLog() {
        return GL46.glGetProgramInfoLog(programId);
    }
    
    public void printActiveUniforms() {
        int uniformCount = GL46.glGetProgrami(programId, GL46.GL_ACTIVE_UNIFORMS);
        RTXMod.LOGGER.info("Active uniforms for program {}: {}", programId, uniformCount);
        
        for (int i = 0; i < uniformCount; i++) {
            String name = GL46.glGetActiveUniform(programId, i, 256);
            int location = GL46.glGetUniformLocation(programId, name);
            RTXMod.LOGGER.info("  {} -> location {}", name, location);
        }
    }
}
