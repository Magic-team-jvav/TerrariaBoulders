package org.confluence.terraria_boulders.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.block.boulder.GiantBoulderBlock;
import org.confluence.terraria_boulders.common.entity.block.GiantBoulderBlockEntity;
import org.jspecify.annotations.NonNull;

public class GiantBoulderRenderer implements BlockEntityRenderer<GiantBoulderBlockEntity, GiantBoulderRenderer.GiantBoulderRenderState> {
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();//渲染静态上下文
    private final BlockModelResolver blockModelResolver;//方块模型解析器

    public GiantBoulderRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    @NonNull
    public AABB getRenderBoundingBox(GiantBoulderBlockEntity entity) {
        //拿到原AABB
        AABB baseBox = new AABB(entity.getBlockPos());

        //获取Size
        if (entity.getLevel() != null && entity.getBlockState().getBlock() instanceof GiantBoulderBlock boulderBlock) {
            double currentSize = boulderBlock.getSize();

            //向四周外扩的半径为SIZE/2.0
            double inflateRadius = (currentSize / 2.0) + 0.5;//加0.5格安全边界

            return baseBox.inflate(inflateRadius);
        }

        //兜底向外扩展
        return baseBox.inflate(2.5);
    }

    //渲染缓存盒
    public static class GiantBoulderRenderState extends BlockEntityRenderState {
        public final BlockModelRenderState displayBlockModel = new BlockModelRenderState();
        public boolean isCenter = false;
        public double size = 3.0;//大小
        public double offsetX = 0.0;//偏移量
        public double offsetY = 0.0;
        public double offsetZ = 0.0;
    }

    @Override
    public GiantBoulderRenderState createRenderState() {
        return new GiantBoulderRenderState();
    }

    @Override
    public void extractRenderState(GiantBoulderBlockEntity blockEntity, GiantBoulderRenderState berState, float partialTicks, @NonNull Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, berState, breakProgress);

        Iterable<BlockPos> rel = blockEntity.getRelativePosIter();
        BlockState state = blockEntity.getBlockState();

        //检查
        if (rel == null || !rel.iterator().hasNext() || !(state.getBlock() instanceof GiantBoulderBlock boulderBlock)) {
            berState.displayBlockModel.clear();
            berState.isCenter = false;
            return;
        }

        //判断是否在中心
        if (GiantBoulderBlock.isCenter(rel)) {
            berState.isCenter = true;

            //对齐平移步长，方块Shape为 relCenterPos.x-0.5，由于渲染以西北下角放大，所以需要让模型往相反方向回拉，结合放大Size倍的偏移，renderOffset 就是对齐步长
            double size = boulderBlock.getSize();
            double renderOffset = 0.5 - (size / 2.0);

            berState.size = size;
            berState.offsetX = renderOffset;
            berState.offsetY = renderOffset;
            berState.offsetZ = renderOffset;

            //转换成渲染模型存入状态机
            this.blockModelResolver.update(berState.displayBlockModel, state, BLOCK_DISPLAY_CONTEXT);
        } else {
            //非中心方块不渲染
            berState.displayBlockModel.clear();
            berState.isCenter = false;
        }
    }

    @Override
    public void submit(GiantBoulderRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        BlockModelRenderState displayBlockModel = state.displayBlockModel;

        if (!state.isCenter || displayBlockModel.isEmpty()) return;

        poseStack.pushPose();

        //整体平移和放大
        poseStack.translate(state.offsetX, state.offsetY, state.offsetZ);

        float size = (float) state.size;
        poseStack.scale(size, size, size);

        //提交
        displayBlockModel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}