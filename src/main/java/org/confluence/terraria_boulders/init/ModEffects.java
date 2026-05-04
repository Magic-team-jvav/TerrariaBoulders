package org.confluence.terraria_boulders.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.effect.PublicMobEffect;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> REGISTER = TerrariaBoulders.modRegister(BuiltInRegistries.MOB_EFFECT);

    public static final DeferredHolder<MobEffect, MobEffect> CHOKING = REGISTER.register("choking", id ->
            new PublicMobEffect(MobEffectCategory.HARMFUL, 0x708090)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, id, -0.30F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
}
