package org.confluence.terraria_boulders.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.CamouflagedBoulderModel;
import org.confluence.terraria_boulders.client.model.MiniBoulderCannonModel;
import org.confluence.terraria_boulders.client.renderer.BoulderCannonRenderer;
import org.confluence.terraria_boulders.client.renderer.CamouflagedBoulderRenderer;
import org.confluence.terraria_boulders.client.renderer.CamouflagedBoulderSpecialRenderer;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModEntityTypes;

import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = TerrariaBoulders.ID, value = Dist.CLIENT)
public class ModClientEvent {

    //--------注册--------
    //public static final DeferredRegister<MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> REGISTER = DeferredRegister.create(BuiltInRegistries.SPECIAL_MODEL_RENDERER, "terraria_boulders");
    //伪装巨石 Codec
    //public static final Supplier<MapCodec<CamouflagedBoulderSpecialRenderer.Unbaked>> CAMOUFLAGED_BOULDER_CODEC = REGISTER.register("camouflaged_boulder", () -> CamouflagedBoulderSpecialRenderer.Unbaked.CODEC);
    @SubscribeEvent
    public static void onRegisterSpecialModels(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.parse("terraria_boulders:camouflaged_boulder"), CamouflagedBoulderSpecialRenderer.Unbaked.CODEC);
    }

    //--------事件--------

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //vent.registerBlockEntityRenderer(ModBlockEntityTypes.CAMOUFLAGED_BOULDER.get(), CamouflagedBoulderBER::new);
        event.registerEntityRenderer(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), CamouflagedBoulderRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.BOULDER_CANNON.get(), BoulderCannonRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CANNON_SEAT.get(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> blockModels = event.getBakingResult().blockStateModels();
        //枚举方块的所有可能状态
        for (BlockState state : ModBlocks.CAMOUFLAGED_BOULDER.get().getStateDefinition().getPossibleStates()) {
            BlockStateModel original = blockModels.get(state);
            if (original != null) {
                //包装成动态模型
                blockModels.put(state, new CamouflagedBoulderModel(original));
            }
        }
    }

    //全局标识符
    public static final ModelLayerLocation CANNON_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "mini_boulder_cannon"), "main");

    //注册层结构
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CANNON_LAYER, MiniBoulderCannonModel::createBodyLayer);
    }
}
