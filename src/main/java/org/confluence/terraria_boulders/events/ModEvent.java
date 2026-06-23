package org.confluence.terraria_boulders.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.model.RollingCactusSpikeModel;
import org.confluence.terraria_boulders.client.renderer.entity.BoulderRenderer;
import org.confluence.terraria_boulders.client.renderer.entity.CamouflagedBoulderRenderer;
import org.confluence.terraria_boulders.client.renderer.entity.RainbowBoulderRenderer;
import org.confluence.terraria_boulders.client.renderer.entity.RollingCactusSpikeRenderer;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.common.item.BoulderGloveItem;
import org.confluence.terraria_boulders.configs.TCCommonConfigs;
import org.confluence.terraria_boulders.events.custom.IEntityInteractable;
import org.confluence.terraria_boulders.events.custom.IUseItemOnBlock;
import org.confluence.terraria_boulders.init.ModEffects;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.confluence.terraria_boulders.events.custom.IBlockBreakable;

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
        event.registerEntityRenderer(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), CamouflagedBoulderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SNOWY_BOULDER.get(), BoulderRenderer::new);
    }

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RollingCactusSpikeModel.LAYER_LOCATION, RollingCactusSpikeModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void livingEntityUseItemEvent$Finish(LivingEntityUseItemEvent.Finish event) {
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

    //Block的修复手持物品潜行状态下无法触发useItem方法的问题
    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) return;

        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        Block block = state.getBlock();

        if(block instanceof IUseItemOnBlock iUseItemOnBlock){
            InteractionResult result = iUseItemOnBlock.useItemOnBlock(event.getItemStack(), state, level, event.getPlayer(), event.getHand(), event.getUseOnContext().getHitResult());

            //不允许后续放置动作发生
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }

    //Item的交互实体事件
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IEntityInteractable iEntityInteractable){
            InteractionResult result = iEntityInteractable.interactEntity(stack, event.getEntity(), event.getTarget(), event.getHand());
            //如果接口返回消耗(CONSUME)或成功(SUCCESS)则取消后续事件
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }

    //Block的方块破坏事件
    @SubscribeEvent
    public static void onBoulderBreak(BreakBlockEvent event) {
        BlockState state = event.getState();
        //判断是否继承探测器
        if (state.getBlock() instanceof IBlockBreakable iBlockBreakable) {
            iBlockBreakable.onRemove((Level) event.getLevel(), state, event.getPos(), event.getPlayer());
        }
    }
}
