# Minecraft RTX Mod for Fabric 1.20.4

A comprehensive ray tracing mod for Minecraft that provides hardware-accelerated RTX features using modern OpenGL and compute shaders.

## Features

### ✨ Ray Tracing
- **Hardware-accelerated ray tracing** with support for NVIDIA RTX, AMD RDNA2+, and Intel Arc GPUs
- **Real-time global illumination** with configurable bounce lighting
- **Ray-traced reflections** with multiple quality settings  
- **Ray-traced shadows** with soft shadow sampling
- **Ambient occlusion** with screen-space and ray-traced methods

### 🎮 Performance
- **Temporal upsampling** for improved performance
- **AI-based denoising** to reduce ray tracing noise
- **Dynamic render scaling** (50%-200%) 
- **DLSS-like upscaling** using compute shaders
- **Adaptive quality** based on GPU capabilities

### 🎨 Visual Enhancements
- **HDR rendering** with tone mapping
- **PBR material system** (roughness, metallic, emission)
- **Motion vectors** for temporal effects
- **G-buffer rendering** with multiple render targets

### ⚙️ Configuration
- **In-game settings** via ModMenu integration
- **Real-time shader reloading** for development
- **Debug visualization** modes
- **Hardware capability detection**

## Controls

- **R**: Toggle RTX on/off
- **F5**: Reload RTX shaders (for development)

## System Requirements

### Minimum (Software Ray Tracing)
- Java 17+
- OpenGL 4.6 compatible GPU
- 8GB RAM
- Fabric 1.20.4

### Recommended (Hardware Ray Tracing)
- **NVIDIA**: RTX 20/30/40 series, GTX 1660/1070/1080
- **AMD**: RX 6000/7000 series (RDNA2/RDNA3)  
- **Intel**: Arc A-series (DG2)
- 16GB RAM
- Fabric 1.20.4

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.4
2. Download and install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Optional: Install [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu) for in-game configuration
4. Place the RTX Mod JAR file in your `.minecraft/mods` folder

## Technical Implementation

### Architecture
```
RTXRenderer
├── RTXCapabilities - GPU detection & feature checking
├── RTXShaderManager - Shader compilation & management  
├── RayTracingPipeline - Main ray tracing compute shaders
├── PostProcessingPipeline - Denoising, temporal accumulation, tone mapping
├── SceneManager - World data organization for ray tracing
└── RTXFrameBuffer - Multi-target HDR framebuffers
```

### Rendering Pipeline
1. **G-Buffer Generation**: Traditional rasterization for primary visibility
2. **Ray Tracing Pass**: Compute shader for lighting, reflections, and GI  
3. **Temporal Accumulation**: Progressive refinement across frames
4. **Denoising**: AI-based noise reduction using G-buffer data
5. **Tone Mapping**: HDR to LDR conversion with exposure control
6. **Upscaling**: Optional AI upscaling for performance

### Shaders
- **G-Buffer**: `gbuffer.vert`, `gbuffer.frag` - Primary visibility
- **Ray Tracing**: `raytracing.comp` - Main ray tracing compute shader
- **Temporal**: `temporal_accumulation.comp` - Frame accumulation  
- **Denoising**: `denoising.comp` - AI-based denoising
- **Tone Mapping**: `tonemap.vert`, `tonemap.frag` - HDR processing
- **Upscaling**: `upscaling.comp` - AI upscaling

## Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd minecraft-rtx-fabric-mod

# Build the mod
gradlew build

# The compiled JAR will be in build/libs/
```

## Configuration

The mod creates a configuration file at `.minecraft/config/rtx-mod.json` with these options:

```json
{
  "rayTracing": {
    "enabled": false,
    "maxBounces": 3,
    "samplesPerPixel": 4,
    "distance": 128.0
  },
  "globalIllumination": {
    "enabled": true,
    "strength": 1.0,
    "samples": 16
  },
  "reflections": {
    "enabled": true,
    "strength": 1.0,
    "quality": 1
  },
  "performance": {
    "renderScale": 100,
    "temporalUpsampling": true,
    "denoising": true
  }
}
```

## Development

This mod uses:
- **Fabric API** for Minecraft integration
- **Mixin** for hooking into the render pipeline
- **LWJGL 3.3.3** for OpenGL access
- **JOML** for math operations
- **ModMenu** for configuration GUI

### Key Classes
- `RTXRenderer`: Main rendering coordinator
- `RTXCapabilities`: Hardware detection
- `RTXShaderManager`: Shader compilation and management
- `RayTracingPipeline`: Core ray tracing implementation
- `PostProcessingPipeline`: Post-processing effects

### Mixins
- `GameRendererMixin`: Key binding handling
- `WorldRendererMixin`: Main render loop integration  
- `RenderSystemMixin`: Window resize handling

## License

This project is licensed under the MIT License. See LICENSE file for details.

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Troubleshooting

### Common Issues

**"RTX not supported"**: Your GPU doesn't support hardware ray tracing. The mod will fall back to software ray tracing with limited functionality.

**Low FPS**: Try reducing render scale, samples per pixel, or max ray bounces in the configuration.

**Shader compilation errors**: Make sure you have updated GPU drivers and OpenGL 4.6 support.

### Debug Mode

Enable debug mode in the configuration to see:
- RTX capability detection results
- Shader compilation logs
- Performance metrics
- Hardware information

## Acknowledgments

- Minecraft Fabric development team
- LWJGL for OpenGL bindings
- JOML for math utilities
- Ray tracing algorithm research and implementations
