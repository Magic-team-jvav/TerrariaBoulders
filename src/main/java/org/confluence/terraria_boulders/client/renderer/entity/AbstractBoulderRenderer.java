package org.confluence.terraria_boulders.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.block.state.BlockState;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;

public abstract class AbstractBoulderRenderer<E extends BoulderEntity, S extends AbstractBoulderRenderer.BoulderRenderState> extends EntityRenderer<E, S> {
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    protected final BlockModelResolver blockModelResolver;

    public AbstractBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
    }

    /**
     * 获取状态数据
     */
    @Override
    public void extractRenderState(E entity, S state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.rotateO = entity.rotateO;
        state.rotate = entity.rotate;
        state.radius = entity.radius;
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        state.boxWidth = dimensions.width();
        state.boxHeight = dimensions.height();
        state.maxRemoveTick = entity.maxRemoveTick;
        state.maxStillTick = entity.maxStillTick;
        state.speed = entity.speed;
        state.minRemoveSpeed = entity.minRemoveSpeed;
        state.bounceFactor = entity.bounceFactor;
        state.frictionFactor = entity.frictionFactor;
        state.generation = entity.generation;
        state.stillTickCount = entity.stillTickCount;
        state.blockState = entity.getBlockState();

        state.yRot = entity.getYRot();

        this.blockModelResolver.update(state.displayBlockModel, state.blockState, BLOCK_DISPLAY_CONTEXT);
    }

    /**
     * 提交需要渲染的东西
     */
    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        BlockModelRenderState displayBlockModel = state.displayBlockModel;

        if (displayBlockModel.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        float radius = state.radius;
        poseStack.translate(0, state.boxHeight / 2, 0);
        poseStack.mulPose(Axis.ZP.rotation(-Mth.rotLerp(state.partialTick, state.rotateO, state.rotate)));
        poseStack.translate(-radius, -radius, -radius);
        displayBlockModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    public static class BoulderRenderState extends EntityRenderState {
        public BlockModelRenderState displayBlockModel = new BlockModelRenderState();

        public float rotateO = 0.0F;
        public float rotate = 0.0F;

        public float radius = 0.5F; // 半径
        public float boxWidth = 1.0f;
        public float boxHeight = 1.0f;
        public int maxRemoveTick = 1200; // 最大移除时间
        public int maxStillTick = 20; // 最大静止时间
        public double speed = 0.7; // 速度
        public double minRemoveSpeed = 0.007; // 最小移除速度
        public double bounceFactor = 0.3;
        public double frictionFactor = 0.9;
        public int generation = 0; // 分裂代数，0为原始巨石

        public int stillTickCount; // 静止刻计时

        public BlockState blockState = BoulderEntity.DEFAULTED_BLOCK_STATE;
        public float yRot;
    }
}
