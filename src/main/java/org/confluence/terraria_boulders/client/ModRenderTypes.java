package org.confluence.terraria_boulders.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class ModRenderTypes {
    public static final RenderType TRAIL_RENDER_TYPE = RenderType.create(
            "trail_render_type",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOutputState(RenderStateShard.WEATHER_TARGET)
                    .createCompositeState(false)
    );
}