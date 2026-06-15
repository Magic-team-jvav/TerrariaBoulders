package org.confluence.terraria_boulders.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.confluence.terraria_boulders.common.entity.boulder.GiantBoulderEntity;

public class GiantBoulderRenderer extends AbstractBoulderRenderer<GiantBoulderEntity, GiantBoulderRenderer.GiantBoulderRenderState> {

    public GiantBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public GiantBoulderRenderState createRenderState() {
        return new GiantBoulderRenderState();
    }

    @Override
    public void extractRenderState(GiantBoulderEntity entity, GiantBoulderRenderState state, float partialTicks) {
        //先让基类提取基础数据
        super.extractRenderState(entity, state, partialTicks);

        //获取Size
        double currentSize = entity.getSize();
        state.size = currentSize;
        //改变半径
        state.radius = (float) (currentSize / 2.0);

        //使用最新数据解析
        this.blockModelResolver.update(state.displayBlockModel, state.blockState, BLOCK_DISPLAY_CONTEXT);
    }

    //渲染状态盒
    public static class GiantBoulderRenderState extends AbstractBoulderRenderer.BoulderRenderState {
        public double size = 3.0;//大小
    }
}
