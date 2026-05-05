package org.confluence.terraria_boulders.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.RollingCactusSpikeModel;
import org.confluence.terraria_boulders.common.entity.boulder.RollingCactusBoulderEntity;

public class RollingCactusSpikeRenderer extends EntityRenderer<RollingCactusBoulderEntity.SpikeProjectile, RollingCactusSpikeRenderer.RollingCactusSpikeRenderState> {
    private static final Identifier TEXTURE = TerrariaBoulders.modRl("textures/entity/rolling_cactus_spike.png");

    private final RollingCactusSpikeModel model;

    public RollingCactusSpikeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RollingCactusSpikeModel(context.bakeLayer(RollingCactusSpikeModel.LAYER_LOCATION));
    }

    public RollingCactusSpikeModel getModel() {
        return this.model;
    }

    @Override
    public RollingCactusSpikeRenderState createRenderState() {
        return new RollingCactusSpikeRenderState();
    }

    @Override
    public void extractRenderState(RollingCactusBoulderEntity.SpikeProjectile entity, RollingCactusSpikeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRotO = entity.yRotO;
        state.yRot = entity.getYRot();
        state.xRotO = entity.xRotO;
        state.xRot = entity.getXRot();
        state.renderType = model.renderType(TEXTURE);
    }

    @Override
    public void submit(RollingCactusSpikeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        BlockModelRenderState displayBlockModel = state.displayBlockModel;
        if (displayBlockModel.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(state.partialTick, state.xRotO, state.xRot)));
        submitNodeCollector.submitModel(model, state, poseStack, state.renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        poseStack.popPose();
    }

    public static class RollingCactusSpikeRenderState extends EntityRenderState {
        public BlockModelRenderState displayBlockModel = new BlockModelRenderState();
        public float yRotO;
        public float xRotO;
        public float yRot;
        public float xRot;
        public RenderType renderType;
    }
}
