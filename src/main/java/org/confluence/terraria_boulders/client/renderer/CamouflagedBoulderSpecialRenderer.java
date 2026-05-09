package org.confluence.terraria_boulders.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.confluence.terraria_boulders.event.ModClientEvent;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;


public class CamouflagedBoulderSpecialRenderer implements SpecialModelRenderer<BlockModelRenderState> {

    @Nullable
    @Override
    public BlockModelRenderState extractArgument(ItemStack stack) {
        BlockState mimicState = stack.get(ModDataComponents.MIMIC_STATE.get());
        BlockState stateToRender = CamouflagedBoulderBlock.DEFAULT_CAMOUFLAGE.get();

        if (mimicState != null && mimicState.getBlock() != null) {
            Block block = mimicState.getBlock();
            if (block != Blocks.AIR) {
                stateToRender = block.defaultBlockState();
            }
        }

        //实例化数据容器
        BlockModelRenderState renderState = new BlockModelRenderState();

        //获取模型
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(stateToRender);

        //初始化容器并获取用于存储几何碎片的List
        List<BlockStateModelPart> parts = renderState.setupModel(new Matrix4f(), false);//是否是半透明方块

        //抽取面片
        //model.collectParts(renderState.scratchRandomSource(42L), parts);
        model.collectParts(
                null,                                  // level: 无世界环境
                BlockPos.ZERO,                         // pos: 原点坐标
                stateToRender,                         // state: 你要伪装的方块状态
                renderState.scratchRandomSource(42L),  // random: 固定随机种子(42L保证材质不闪烁)
                parts                                  // parts: 接收几何碎片的容器
        );

        return renderState;
    }

    @Override
    public void submit(@Nullable BlockModelRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int packedColor) {
        if (state == null || state.isEmpty()) return;
        //poseStack.pushPose();
        state.submitMultiLayer(poseStack, collector, light, overlay, packedColor);
        //poseStack.popPose();
    }

    //获取渲染边界
    //模型占据空间，没看到这个区域就不会执行submit
    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        //1x1x1
//        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
//        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
        consumer.accept(new Vector3f(-0.5F, -0.5F, -0.5F));
        consumer.accept(new Vector3f(0.5F, 0.5F, 0.5F));
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<BlockModelRenderState> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Nullable
        @Override
        public SpecialModelRenderer<BlockModelRenderState> bake(@NonNull BakingContext context) {
            return new CamouflagedBoulderSpecialRenderer();
        }

        @Override
        @NonNull
        public MapCodec<? extends SpecialModelRenderer.Unbaked<BlockModelRenderState>> type() {
            return CODEC;
        }
    }
}