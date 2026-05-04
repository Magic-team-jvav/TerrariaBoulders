package org.confluence.terraria_boulders.init;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.item.BaseFoodItem;
import org.confluence.terraria_boulders.common.item.ModFoodPropertiesBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

public final class FoodItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(TerrariaBoulders.ID);

    public static final DeferredItem<BlockItem> BOULDER_BREAD = registerBlockItemFood("boulder_bread", builder -> builder
            .food(hasEffectProperties(20, 2.5f, ModFoodPropertiesBuilder.EffectData.of(ModEffects.CHOKING, 6000)))
            .duration(d -> 48)
            .useAnim(u -> UseAnim.EAT)
            .eatingSound(s -> SoundEvents.GENERIC_EAT), ModBlocks.BOULDER_BREAD_BLOCK.getDelegate());

    public static DeferredItem<BlockItem> registerBlockItemFood(String name, Consumer<BaseFoodItem.Builder> consumer, Holder<? extends Block> block) {
        return REGISTER.register(name, () -> {
            BaseFoodItem.Builder builder = BaseFoodItem.builder().stackTo(64);
            consumer.accept(builder);
            return new BlockItem(block.value(), builder.getProperties());
        });
    }

    // 自定义效果食物
    public static FoodProperties hasEffectProperties(int nutrition, float saturation, ModFoodPropertiesBuilder.EffectData... effects) {
        ModFoodPropertiesBuilder builder = ModFoodPropertiesBuilder.Builder()
                .nutrition(nutrition)
                .saturation(saturation)
                .fast()
                .alwaysEdible();
        Arrays.stream(effects).forEach(e -> builder.addEffect(new MobEffectInstance(e.effect(), e.duration(), e.level()), e.probability()));
        return builder.build();
    }

}
