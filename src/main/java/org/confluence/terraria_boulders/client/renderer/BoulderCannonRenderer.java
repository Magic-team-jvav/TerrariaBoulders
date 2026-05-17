package org.confluence.terraria_boulders.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.BoulderCannonModel;
import org.confluence.terraria_boulders.client.model.MiniBoulderCannonModel;
import org.confluence.terraria_boulders.client.state.BoulderCannonRenderState;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;
import org.confluence.terraria_boulders.event.ModClientEvent;
import org.jspecify.annotations.NonNull;

public class BoulderCannonRenderer implements BlockEntityRenderer<BoulderCannonBlockEntity, BoulderCannonRenderState> {
    //private final MiniBoulderCannonModel model;
    //private final BoulderCannonModel model;
    // 🚀 核心破解法：双生模型！彻底避开 1.21 的延迟渲染竞争Bug
    private final BoulderCannonModel modelBase;
    private final BoulderCannonModel modelBarrel;
    private static final Identifier TEXTURE_LOADED = Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "textures/block/boulder_cannon.png");
    private static final Identifier TEXTURE_EMPTY = Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "textures/block/boulder_cannon_empty.png");

    public BoulderCannonRenderer(BlockEntityRendererProvider.Context context) {
        //this.model = new BoulderCannonModel(context.bakeLayer(ModClientEvent.CANNON_LAYER));
        //模型一：画底座，隐藏炮管
        this.modelBase = new BoulderCannonModel(context.bakeLayer(ModClientEvent.CANNON_LAYER));
        this.modelBase.body.visible = false;
        //模型二：炮管
        this.modelBarrel = new BoulderCannonModel(context.bakeLayer(ModClientEvent.CANNON_LAYER));
    }

    @Override
    public BoulderCannonRenderState createRenderState() {
        return new BoulderCannonRenderState();
    }

//    @Override
//    public void extractRenderState(BoulderCannonBlockEntity be, BoulderCannonRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
//        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
//        state.yaw = be.getCurrentYaw();
//        state.yawO = be.currentYawO;
//        state.pitch = be.getCurrentPitch();
//        state.pitchO = be.currentPitchO;
//        state.partialTicks = partialTicks;
//
//        //提取装填状态
//        state.isEmpty = be.getCannonAmmo().isEmpty();
//
//        //获取大炮位置世界亮度，存入state
//        if (be.getLevel() != null) {
//            state.lightCoords = LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos());
//        } else {
//            state.lightCoords = 15728880;
//        }
//    }

    @Override
    @NonNull
    public AABB getRenderBoundingBox(BoulderCannonBlockEntity blockEntity) {
        //将默认的1x1x1渲染判定框向四周各扩大2格，防止视锥体剔除
        return new AABB(blockEntity.getBlockPos()).inflate(2.0D);
    }

    @Override
    public void extractRenderState(BoulderCannonBlockEntity be, BoulderCannonRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);

        state.yaw = be.getCurrentYaw();
        state.yawO = be.currentYawO;
        state.pitch = be.getCurrentPitch();
        state.pitchO = be.currentPitchO;

        LocalPlayer localPlayer = net.minecraft.client.Minecraft.getInstance().player;
        if (localPlayer != null && localPlayer.getVehicle() instanceof org.confluence.terraria_boulders.common.entity.CannonSeatEntity seat) {
            if (seat.blockPosition().equals(be.getBlockPos())) {
                state.yaw = Mth.wrapDegrees(localPlayer.getYRot());
                state.yawO = Mth.wrapDegrees(localPlayer.yRotO);
                state.pitch = Mth.clamp(Mth.wrapDegrees(localPlayer.getXRot()), BoulderCannonBlockEntity.MIN_PITCH, BoulderCannonBlockEntity.MAX_PITCH);
                state.pitchO = Mth.clamp(Mth.wrapDegrees(localPlayer.xRotO), BoulderCannonBlockEntity.MIN_PITCH, BoulderCannonBlockEntity.MAX_PITCH);
            }
        }

        state.partialTicks = partialTicks;
        state.isEmpty = be.getCannonAmmo().isEmpty();

        if (be.getLevel() != null) {
            state.lightCoords = LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos());
        } else {
            state.lightCoords = 15728880;
        }
    }

    @Override
    public void submit(BoulderCannonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        //基础居中与翻转
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        Identifier currentTexture = state.isEmpty ? TEXTURE_EMPTY : TEXTURE_LOADED;
        RenderType renderType = RenderTypes.entityCutout(currentTexture);

        //提交静止的底盘
        collector.submitModelPart(this.modelBase.bb_main, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);

        float lerpYaw = Mth.rotLerp(state.partialTicks, state.yawO, state.yaw);
        float lerpPitch = Mth.lerp(state.partialTicks, state.pitchO, state.pitch);

        //炮台水平旋转
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(lerpYaw + 180.0F));

        //转盘、侧板
        collector.submitModelPart(this.modelBase.root, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);

        //炮管垂直旋转 (Pitch)
        // 重新计算的精确炮管世界绝对轴心：
        float pivotY = 12.0384F / 16.0F;
        float pivotZ = 0.1153F / 16.0F;

        poseStack.pushPose();
        //把画笔移动到炮管轴心
        poseStack.translate(0, pivotY, pivotZ);

        //旋转仰角
        poseStack.mulPose(Axis.XP.rotationDegrees(lerpPitch));

        //临时清空炮管自己的局部偏移，让它完美吸附在当前的画笔（轴心）上
        float oldY = this.modelBarrel.body.y;
        float oldZ = this.modelBarrel.body.z;
        this.modelBarrel.body.x = 0;
        this.modelBarrel.body.y = 0;
        this.modelBarrel.body.z = 0;

        //提交炮管
        collector.submitModelPart(this.modelBarrel.body, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);

        //恢复偏移
        this.modelBarrel.body.y = oldY;
        this.modelBarrel.body.z = oldZ;

        poseStack.popPose();//弹出 Pitch
        poseStack.popPose();//弹出 Yaw
        poseStack.popPose();//弹出 全局中心
    }

//    @Override
//    public void submit(BoulderCannonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
//        poseStack.pushPose();
//
//        //将坐标移到方块中心，由于bb的模型中心点通常在底面中心，所以Y轴只移1.5(原版模型通常倒置，所以经常伴随 180度翻转)
//        poseStack.translate(0.5D, 1.5D, 0.5D);
//        //bb导出的Java模型默认上下颠倒的，翻转过来
//        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
//        poseStack.mulPose(Axis.YP.rotationDegrees(state.getLerpYaw() + 180.0F));//应用角度
//
//        //获取当前材质
//        Identifier currentTexture = state.isEmpty ? TEXTURE_EMPTY : TEXTURE_LOADED;
//        //获取当前画笔和贴图
//        RenderType renderType = RenderTypes.entityCutout(currentTexture);
//
//        //底座
//        collector.submitModelPart(
//                this.model.bb_main,                 //模型根骨骼
//                poseStack,                           //当前矩阵栈
//                renderType,                          //渲染类型（+贴图）
//                state.lightCoords,                   //ackedLight
//                OverlayTexture.NO_OVERLAY,           //overlay
//                null                                 //TextureAtlasSprite
//        );
//
//        //在这里才旋转X，底座不受影响
//        //炮管body的轴心点在 (0, 21.0276, 4.1552)
//        //先移到这个轴心点，旋转后再移回去
//        float pX = 0.0F / 16.0F;
//        float pY = 21.0276F / 16.0F;
//        float pZ = 4.1552F / 16.0F;
//
//        poseStack.pushPose();
//
//        //移到炮管的旋转轴心
//        poseStack.translate(pX, pY, pZ);
//
//        //应用垂直旋转
//        poseStack.mulPose(Axis.XP.rotationDegrees(state.getLerpPitch()));
//
//        //移回原位，使offset生效
//        poseStack.translate(-pX, -pY, -pZ);
//
//        //炮管
//        collector.submitModelPart(
//                this.model.body,         //模型根骨骼
//                poseStack,                  //当前矩阵栈
//                renderType,                 //渲染类型（+贴图）
//                state.lightCoords,          //ackedLight
//                OverlayTexture.NO_OVERLAY,  //overlay
//                null                        //TextureAtlasSprite
//        );
//
//        poseStack.popPose();//弹出炮管变换
//        poseStack.popPose();//弹出整体变换
//    }
}
