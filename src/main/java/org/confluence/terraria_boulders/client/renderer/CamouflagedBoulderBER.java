package org.confluence.terraria_boulders.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.entity.block.CamouflagedBoulderBlockEntity;
import org.jspecify.annotations.NonNull;

public class CamouflagedBoulderBER implements BlockEntityRenderer<CamouflagedBoulderBlockEntity, CamouflagedBoulderBER.BoulderBERState> {
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();//渲染静态上下文
    private final BlockModelResolver blockModelResolver;//方块模型解析器

    public CamouflagedBoulderBER(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    //方块实体渲染缓存盒
    public static class BoulderBERState extends BlockEntityRenderState {
        //伪装方块模型数据
        public final BlockModelRenderState displayBlockModel = new BlockModelRenderState();
    }

    //创建缓存盒实例
    @Override
    public BoulderBERState createRenderState() {
        return new BoulderBERState();
    }

    //提取状态
    @Override
    public void extractRenderState(CamouflagedBoulderBlockEntity blockEntity, BoulderBERState state, float partialTicks, @NonNull Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        //提取坐标、光照和方块状态
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);

        //获取伪装目标
        BlockState mimicState = blockEntity.getMimicState();

        if (mimicState != null && !mimicState.isAir()) {
            //将mimicState转换成渲染模型，存入displayBlockModel
            this.blockModelResolver.update(state.displayBlockModel, mimicState, BLOCK_DISPLAY_CONTEXT);
        } else {
            state.displayBlockModel.clear();//如果没有伪装，清空模型防残影
        }
    }


    //主渲染方法
    @Override
    public void submit(BoulderBERState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        BlockModelRenderState displayBlockModel = state.displayBlockModel;

        if (displayBlockModel.isEmpty()) return;

        poseStack.pushPose();

        //提交
        displayBlockModel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);//最后一个参数是外轮廓高亮颜色

        poseStack.popPose();
    }
}