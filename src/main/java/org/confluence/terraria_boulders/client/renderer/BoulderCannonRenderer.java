package org.confluence.terraria_boulders.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.MiniBoulderCannonModel;
import org.confluence.terraria_boulders.client.state.BoulderCannonRenderState;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;
import org.confluence.terraria_boulders.event.ModClientEvent;

public class BoulderCannonRenderer implements BlockEntityRenderer<BoulderCannonBlockEntity, BoulderCannonRenderState> {
    private final MiniBoulderCannonModel model;
    private static final Identifier TEXTURE_LOADED = Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "textures/block/mini_boulder_cannon.png");
    private static final Identifier TEXTURE_EMPTY = Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "textures/block/mini_boulder_cannon_empty.png");

    public BoulderCannonRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MiniBoulderCannonModel(context.bakeLayer(ModClientEvent.CANNON_LAYER));
    }

    @Override
    public BoulderCannonRenderState createRenderState() {
        return new BoulderCannonRenderState();
    }

    @Override
    public void extractRenderState(BoulderCannonBlockEntity be, BoulderCannonRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.yaw = be.currentYaw;
        state.pitch = be.currentPitch;

        //提取装填状态
        state.isEmpty = be.getCannonAmmo().isEmpty();

        //获取大炮位置世界亮度，存入state
        if (be.getLevel() != null) {
            state.lightCoords = LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos());
        } else {
            state.lightCoords = 15728880;
        }
    }

    @Override
    public void submit(BoulderCannonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        //将坐标移到方块中心，由于Blockbench的模型中心点通常在底面中心，所以 Y 轴只移 1.5 (原版模型通常倒置，所以经常伴随 180度翻转)
        poseStack.translate(0.5D, 1.5D, 0.5D);
        //BlockBench导出的Java模型默认上下颠倒的，翻转过来
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw + 180.0F));//应用角度

        //获取当前材质
        Identifier currentTexture = state.isEmpty ? TEXTURE_EMPTY : TEXTURE_LOADED;
        //获取当前画笔和贴图
        RenderType renderType = RenderTypes.entityCutout(currentTexture);

        //底座
        collector.submitModelPart(
                this.model.bb_main,         //模型根骨骼
                poseStack,                  //当前矩阵栈
                renderType,                 //渲染类型（+贴图）
                state.lightCoords,          //ackedLight
                OverlayTexture.NO_OVERLAY,  //overlay
                null                        //TextureAtlasSprite
        );

        //在这里才旋转X，底座不受影响
        //炮管body的轴心点在 (0, 21.0276, 4.1552)
        //先移到这个轴心点，旋转后再移回去
        float pX = 0.0F / 16.0F;
        float pY = 21.0276F / 16.0F;
        float pZ = 4.1552F / 16.0F;

        poseStack.pushPose();

        //移到炮管的旋转轴心
        poseStack.translate(pX, pY, pZ);

        //应用垂直旋转
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));

        //移回原位，使offset生效
        poseStack.translate(-pX, -pY, -pZ);

        //炮管
        collector.submitModelPart(
                this.model.body,         //模型根骨骼
                poseStack,                  //当前矩阵栈
                renderType,                 //渲染类型（+贴图）
                state.lightCoords,          //ackedLight
                OverlayTexture.NO_OVERLAY,  //overlay
                null                        //TextureAtlasSprite
        );

        poseStack.popPose();//弹出炮管变换
        poseStack.popPose();//弹出整体变换
    }
}
