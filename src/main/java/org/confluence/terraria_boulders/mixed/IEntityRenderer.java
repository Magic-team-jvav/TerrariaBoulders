package org.confluence.terraria_boulders.mixed;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public interface IEntityRenderer {
    EntityRendererProvider.Context getTerraria_boulders$context();

    static IEntityRenderer of(EntityRenderer<?, ?> renderer) {
        return (IEntityRenderer) renderer;
    }
}
