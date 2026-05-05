package org.confluence.terraria_boulders.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;

@javax.annotation.ParametersAreNonnullByDefault
public class MultiplyExplosionDamageCalculator extends ExplosionDamageCalculator {
    private final float multiplier;

    public MultiplyExplosionDamageCalculator(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getEntityDamageAmount(Explosion explosion, Entity entity) {
        return super.getEntityDamageAmount(explosion, entity, ServerExplosion.getSeenPercent(explosion.center(), entity)) * multiplier;
    }

}
