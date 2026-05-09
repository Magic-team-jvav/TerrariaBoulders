package org.confluence.terraria_boulders.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderBlockEntity;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CamouflagedBoulderModel implements BlockStateModel {
    private final BlockStateModel originalModel; // 原始石头的模型

    public CamouflagedBoulderModel(BlockStateModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        //如果 level 为空，则是在渲染物品。此时没有 ModelData，直接走原版石头的渲染逻辑
        if (level == null || pos == null) {
            this.originalModel.collectParts(random, parts);
            return;
        }

        //获取ModelData
        ModelData data = level.getModelData(pos);
        //提取伪装方块状态
        BlockState mimic = data.get(CamouflagedBoulderBlockEntity.MIMIC_STATE_PROPERTY);
        if (mimic != null && !mimic.isAir() && !mimic.is(state.getBlock())) {//如果伪装目标是巨石方块本身直接渲染原始石头模型，防止递归
            //获取伪装目标的 BlockStateModel
            BlockStateModel mimicModel = Minecraft.getInstance()
                    .getModelManager()
                    .getBlockStateModelSet()
                    .get(mimic);
            if (mimicModel != null) {
                //递归调用目标collectParts
                mimicModel.collectParts(level, pos, mimic, random, parts);
                return;
            }
        }

        //没伪装时，显示原本的石头
        this.originalModel.collectParts(level, pos, state, random, parts);
    }

    /**
     * 返回不同的 Key，渲染引擎才会意识到模型变了，从而更新光照
     */
    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        ModelData data = level.getModelData(pos);
        BlockState mimic = data.get(CamouflagedBoulderBlockEntity.MIMIC_STATE_PROPERTY);
        //如果是钻石矿 Key就是钻石矿，如果是石头 Key就是石头
        return mimic != null ? mimic : state;
    }

    // 以下是原版接口要求的 Deprecated 兜底
    @Override @Deprecated public void collectParts(RandomSource random, List<BlockStateModelPart> parts) { this.originalModel.collectParts(random, parts); }
    @Override @Deprecated public Material.@NonNull Baked particleMaterial() { return originalModel.particleMaterial(); }
    @Override @Deprecated public int materialFlags() { return originalModel.materialFlags(); }
}
