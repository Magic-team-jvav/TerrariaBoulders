package org.confluence.terraria_boulders.init;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;

public final class FoodItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(TerrariaBoulders.ID);

    public static final DeferredItem<BlockItem> BOULDER_BREAD = registerBlockItemFood("boulder_bread", new Item.Properties()
            .food(new FoodProperties(20, 2.5f, true), Consumable.builder()
                    .sound(SoundEvents.GENERIC_EAT)
                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.CHOKING), 6000))
                    .animation(ItemUseAnimation.EAT)
                    .consumeSeconds(48)
                    .build()), ModBlocks.BOULDER_BREAD_BLOCK.getDelegate());

    public static DeferredItem<BlockItem> registerBlockItemFood(String name, Item.Properties properties, Holder<? extends Block> block) {
        return REGISTER.register(name, () -> new BlockItem(block.value(), properties));
    }
}
