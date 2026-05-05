package org.confluence.terraria_boulders.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.RollingCactusSpikeModel;
import org.confluence.terraria_boulders.client.renderer.BoulderRenderer;
import org.confluence.terraria_boulders.client.renderer.RainbowBoulderRenderer;
import org.confluence.terraria_boulders.client.renderer.RollingCactusSpikeRenderer;
import org.confluence.terraria_boulders.configs.TCCommonConfigs;
import org.confluence.terraria_boulders.init.ModEffects;
import org.confluence.terraria_boulders.init.ModEntityTypes;

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

    @SubscribeEvent
    public static void livingEntityUseItemEvent$Finish(LivingEntityUseItemEvent.Finish event){
        ItemStack item = event.getItem();
        if (item.is(Tags.Items.DRINKS_WATER) || item.is(Tags.Items.DRINKS_WATERY)) {
            event.getEntity().removeEffect(ModEffects.CHOKING);
        }
    }

    @SubscribeEvent
    public static void modConfig$Loading(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON && TerrariaBoulders.ID.equals(event.getConfig().getModId())) {
            TCCommonConfigs.onLoad();
        }
    }

    @SubscribeEvent
    public static void modConfig$Reloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON && TerrariaBoulders.ID.equals(event.getConfig().getModId())) {
            TCCommonConfigs.onLoad();
        }
    }
}
