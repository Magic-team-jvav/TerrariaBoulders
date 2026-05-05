package org.confluence.terraria_boulders.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;

public class BoulderRenderer extends AbstractBoulderRenderer<BoulderEntity, AbstractBoulderRenderer.BoulderRenderState> {
    public BoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AbstractBoulderRenderer.BoulderRenderState createRenderState() {
        return new AbstractBoulderRenderer.BoulderRenderState();
    }
}
