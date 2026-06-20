package org.confluence.terraria_boulders.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.BoulderCannonModel;
import org.confluence.terraria_boulders.client.model.BoulderGloveItemModel;
import org.confluence.terraria_boulders.client.model.CamouflagedBoulderModel;
import org.confluence.terraria_boulders.client.model.MiniBoulderCannonModel;
import org.confluence.terraria_boulders.client.renderer.block.BoulderCannonRenderer;
import org.confluence.terraria_boulders.client.renderer.block.CamouflagedBoulderSpecialRenderer;
import org.confluence.terraria_boulders.client.renderer.entity.CamouflagedBoulderRenderer;
import org.confluence.terraria_boulders.common.entity.CannonSeatEntity;
import org.confluence.terraria_boulders.common.network.MountClickPayload;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModEntityTypes;

import java.util.Map;

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
        //BlockEntityRenderer
        event.registerBlockEntityRenderer(ModBlockEntityTypes.BOULDER_CANNON.get(), BoulderCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CAMOUFLAGED_BOULDER.get(), org.confluence.terraria_boulders.client.renderer.block.CamouflagedBoulderRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.GIANT_BOULDER.get(), org.confluence.terraria_boulders.client.renderer.block.GiantBoulderRenderer::new);
        //EntityRenderer
        event.registerEntityRenderer(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), CamouflagedBoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CANNON_SEAT.get(), NoopRenderer::new);//CamouflagedBoulderBER
        event.registerEntityRenderer(ModEntityTypes.GIANT_BOULDER.get(), org.confluence.terraria_boulders.client.renderer.entity.GiantBoulderRenderer::new);
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

    //坐在大炮上时，左键开火，右键装填
    @SubscribeEvent
    public static void onKeyInput(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof CannonSeatEntity) {
            if (event.isAttack()) {//左键
                ClientPacketDistributor.sendToServer(new MountClickPayload(true));
                event.setCanceled(true);//取消左键
            } else if (event.isUseItem()) {//右键
                ClientPacketDistributor.sendToServer(new MountClickPayload(false));
                event.setCanceled(true);//取消右键
            }
        }
    }

    //全局标识符
    public static final ModelLayerLocation MINI_CANNON_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "mini_boulder_cannon"), "main");
    public static final ModelLayerLocation CANNON_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "boulder_cannon"), "main");
    public static final ModelLayerLocation BOULDER_GLOVE_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "boulder_glove"), "main");

    //注册层结构
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MINI_CANNON_LAYER, MiniBoulderCannonModel::createBodyLayer);
        event.registerLayerDefinition(CANNON_LAYER, BoulderCannonModel::createBodyLayer);
        event.registerLayerDefinition(BOULDER_GLOVE_LAYER, BoulderGloveItemModel::createBodyLayer);
    }
}
