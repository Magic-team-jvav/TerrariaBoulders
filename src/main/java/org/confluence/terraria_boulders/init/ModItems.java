package org.confluence.terraria_boulders.init;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBreadBlock;
import org.confluence.terraria_boulders.common.block.boulder.ContactEffectBoulderBlock;
import org.confluence.terraria_boulders.common.block.boulder.FullCollisionBoulderBlock;
import org.confluence.terraria_boulders.common.entity.boulder.*;
import org.confluence.terraria_boulders.common.item.CamouflagedBoulderItem;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(TerrariaBoulders.ID);

    public static final DeferredItem<BlockItem> BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.BOULDER);
    public static final DeferredItem<BlockItem> OAK_LOG_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.OAK_LOG_BOULDER);
    public static final DeferredItem<BlockItem> FOLLOWER_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.FOLLOWER_BOULDER);
    public static final DeferredItem<BlockItem> EXPLODE_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.EXPLODE_BOULDER);
    public static final DeferredItem<BlockItem> ROLLING_CACTUS_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.ROLLING_CACTUS_BOULDER);
    public static final DeferredItem<BlockItem> BOUNCY_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.BOUNCY_BOULDER);
    public static final DeferredItem<BlockItem> GHOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.GHOULDER);
    public static final DeferredItem<BlockItem> LAVA_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.LAVA_BOULDER);
    public static final DeferredItem<BlockItem> SPIDER_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.SPIDER_BOULDER);
    public static final DeferredItem<BlockItem> RAINBOW_BOULDER = REGISTER.registerSimpleBlockItem(ModBlocks.RAINBOW_BOULDER);
    public static final DeferredItem<BlockItem> BOULDER_BREAD = registerBlockItemFood("boulder_bread", new Item.Properties()
            .food(new FoodProperties(20, 2.5f, true), Consumable.builder()
                    .sound(SoundEvents.GENERIC_EAT)
                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.CHOKING), 6000))
                    .animation(ItemUseAnimation.EAT)
                    .consumeSeconds(48)
                    .build()), ModBlocks.BOULDER_BREAD_BLOCK.getDelegate());
    public static final DeferredItem<CamouflagedBoulderItem> CAMOUFLAGED_BOULDER =
            REGISTER.registerItem("camouflaged_boulder",
                    (properties) -> new CamouflagedBoulderItem(ModBlocks.CAMOUFLAGED_BOULDER.get(), properties.component(ModDataComponents.IS_LOCKED.get(), false)));//伪装巨石
    public static final DeferredItem<BlockItem> BOULDER_CANNON = REGISTER.registerSimpleBlockItem(ModBlocks.BOULDER_CANNON);//巨石大炮

    public static DeferredItem<BlockItem> registerBlockItemFood(String name, Item.Properties properties, Holder<? extends Block> block) {
        return REGISTER.registerItem(name, (properties1) -> new BlockItem(block.value(), properties1), () -> properties);
    }
}
