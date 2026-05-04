package org.confluence.terraria_boulders.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.entity.boulder.*;

import java.util.function.Supplier;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTER = TerrariaBoulders.modRegister(Registries.ENTITY_TYPE);

    public static final DeferredHolder<EntityType<?>, EntityType<BoulderEntity>> BOULDER = register("boulder", () ->
            EntityType.Builder.<BoulderEntity>of(BoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<FollowerBoulderEntity>> FOLLOWER_BOULDER = register("follower_boulder", () ->
            EntityType.Builder.<FollowerBoulderEntity>of(FollowerBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<ExplodeBoulderEntity>> EXPLODE_BOULDER = register("explode_boulder", () ->
            EntityType.Builder.<ExplodeBoulderEntity>of(ExplodeBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<RollingCactusBoulderEntity>> ROLLING_CACTUS_BOULDER = register("rolling_cactus_boulder", () ->
            EntityType.Builder.<RollingCactusBoulderEntity>of(RollingCactusBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<RollingCactusBoulderEntity.SpikeProjectile>> ROLLING_CACTUS_SPIKE = register("rolling_cactus_spike", () ->
            EntityType.Builder.of(RollingCactusBoulderEntity.SpikeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<BouncyBoulderEntity>> BOUNCY_BOULDER = register("bouncy_boulder", () ->
            EntityType.Builder.<BouncyBoulderEntity>of(BouncyBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<GhoulderEntity>> GHOULDER = register("ghoulder", () ->
            EntityType.Builder.<GhoulderEntity>of(GhoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<LavaBoulderEntity>> LAVA_BOULDER = register("lava_boulder", () ->
            EntityType.Builder.<LavaBoulderEntity>of(LavaBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderBoulderEntity>> SPIDER_BOULDER = register("spider_boulder", () ->
            EntityType.Builder.<SpiderBoulderEntity>of(SpiderBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));
    public static final DeferredHolder<EntityType<?>, EntityType<RainbowBoulderEntity>> RAINBOW_BOULDER = register("rainbow_boulder", () ->
            EntityType.Builder.<RainbowBoulderEntity>of(RainbowBoulderEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(6));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> builder) {
        return REGISTER.register(name, () -> builder.get().build(TerrariaBoulders.modRlText(name)));
    }
}
