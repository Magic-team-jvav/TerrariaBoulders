package org.confluence.terraria_boulders.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderEntity;

public class CamouflagedBoulderRenderer extends AbstractBoulderRenderer<CamouflagedBoulderEntity, AbstractBoulderRenderer.BoulderRenderState> {
    private final net.minecraft.client.renderer.block.BlockModelResolver blockModelResolver;

    public CamouflagedBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public BoulderRenderState createRenderState() {
        return new AbstractBoulderRenderer.BoulderRenderState();
    }

    @Override
    public void extractRenderState(CamouflagedBoulderEntity entity, AbstractBoulderRenderer.BoulderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        //覆盖方块状态
        BlockState mimic = entity.getMimicState();
        //System.out.println("Client Mimic State: " + mimic);
        if (mimic != null && mimic != state.blockState) {
            state.blockState = mimic;
            //基类的super方法已经用默认石头update过一次模型了，用伪装块的状态再update一次，否则渲染出来的还是石头
            this.blockModelResolver.update(state.displayBlockModel, state.blockState, BLOCK_DISPLAY_CONTEXT);
        }
    }
}