package org.confluence.terraria_boulders.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.confluence.terraria_boulders.TerrariaBoulders;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> BOULDER = register("boulder");

    private static ResourceKey<DamageType> register(String id) {
        return TerrariaBoulders.modResourceKey(Registries.DAMAGE_TYPE, id);
    }

    public static DamageSource of(Level level, ResourceKey<DamageType> key) {
        return of(level, key, null, null);
    }

    public static DamageSource of(Level level, ResourceKey<DamageType> key, @Nullable Entity causing) {
        return of(level, key, causing, causing);
    }

    public static DamageSource of(Level level, ResourceKey<DamageType> key, @Nullable Entity direct, @Nullable Entity causing) {
        return level.damageSources().source(key, direct, causing);
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        damageType(context, BOULDER, DamageScaling.ALWAYS, 5);
    }

    private static void damageType(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
        context.register(key, new DamageType(key.location().getPath(), scaling, exhaustion, effects, deathMessageType));
    }

    private static void damageType(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, DamageScaling scaling, float exhaustion, DamageEffects effects) {
        damageType(context, key, scaling, exhaustion, effects, DeathMessageType.DEFAULT);
    }

    private static void damageType(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, DamageScaling scaling, float exhaustion) {
        damageType(context, key, scaling, exhaustion, DamageEffects.HURT, DeathMessageType.DEFAULT);
    }
}
