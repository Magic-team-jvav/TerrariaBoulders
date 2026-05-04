package org.confluence.terraria_boulders.init;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.item.BaseFoodItem;
import org.confluence.terraria_boulders.common.item.ModFoodPropertiesBuilder;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(TerrariaBoulders.ID);

    public static final DeferredItem<BaseFoodItem.BItem> BOULDER_BREAD = registerBlockItemFood("boulder_bread", builder -> builder
            .food(hasEffectProperties(20, 2.5f, ModFoodPropertiesBuilder.EffectData.of(ModEffects.CHOKING, 6000)))
            .duration(d -> 48).useAnim(u -> UseAnim.EAT).eatingSound(s -> SoundEvents.GENERIC_EAT), ModBlocks.BOULDER_BREAD_BLOCK); //巨石面包

    public static DeferredItem<BaseFoodItem.BItem> registerBlockItemFood(String name, Consumer<BaseFoodItem.Builder> consumer, Supplier<? extends Block> block) {
        return REGISTER.register(name, () -> {
            BaseFoodItem.Builder builder = BaseFoodItem.builder().stackTo(64);
            consumer.accept(builder);
            return new BaseFoodItem.BItem(block.get(), builder.getProperties());
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
