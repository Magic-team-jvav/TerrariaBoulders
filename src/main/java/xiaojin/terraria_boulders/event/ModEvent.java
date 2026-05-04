package xiaojin.terraria_boulders.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xiaojin.terraria_boulders.TerrariaBoulders;
import xiaojin.terraria_boulders.client.model.RollingCactusSpikeModel;
import xiaojin.terraria_boulders.client.renderer.BoulderRenderer;
import xiaojin.terraria_boulders.client.renderer.RainbowBoulderRenderer;
import xiaojin.terraria_boulders.client.renderer.RollingCactusSpikeRenderer;
import xiaojin.terraria_boulders.init.ModEntityTypes;

@EventBusSubscriber(modid = TerrariaBoulders.ID)
public class ModEvent {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FOLLOWER_BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EXPLODE_BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ROLLING_CACTUS_BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ROLLING_CACTUS_SPIKE.get(), RollingCactusSpikeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BOUNCY_BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GHOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LAVA_BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPIDER_BOULDER.get(), BoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RAINBOW_BOULDER.get(), RainbowBoulderRenderer::new);
    }

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RollingCactusSpikeModel.LAYER_LOCATION, RollingCactusSpikeModel::createBodyLayer);
    }
}
