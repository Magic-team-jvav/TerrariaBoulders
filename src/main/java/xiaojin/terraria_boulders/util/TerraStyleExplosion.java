package xiaojin.terraria_boulders.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mesdag.particlestorm.PSGameClient;
import org.mesdag.particlestorm.data.molang.MolangExp;
import org.mesdag.particlestorm.particle.ParticleEmitter;
import xiaojin.terraria_boulders.TerrariaBoulders;
import xiaojin.terraria_boulders.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class TerraStyleExplosion extends Explosion {

    public TerraStyleExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, Explosion.BlockInteraction blockInteraction) {
        super(level, source, damageSource, damageCalculator, x, y, z, radius, false, blockInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
    }

    @Override
    public void explode() {
        Vec3 center = new Vec3(x, y, z);
        level.gameEvent(source, GameEvent.EXPLODE, center);

        float obsidianBasedExplosionResistance = ModBlocks.getObsidianBasedExplosionResistance(0);
        BlockPos origin = BlockPos.containing(x, y, z);
        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        double radiusP2 = radius * radius;
        double inner = Mth.square(Mth.floor(radius - 0.5F));

        BlockPos.betweenClosedStream(area).forEach(pos -> {
            double sqr = pos.distSqr(origin);
            if (level.isInWorldBounds(pos) && sqr <= radiusP2) {
                if (!level.getFluidState(pos).isEmpty()) return; // 无视流体
                BlockState blockState = level.getBlockState(pos);
                damageCalculator.getBlockExplosionResistance(this, level, pos, blockState, Fluids.EMPTY.defaultFluidState()).ifPresent(resistance -> {
                    if (resistance < obsidianBasedExplosionResistance) {
                        if (sqr < inner || random.nextFloat() < 0.8F) {
                            toBlow.add(pos.immutable());
                        }
                    }
                });
            }
        });

        List<Entity> list = level.getEntities(source, area);
        net.neoforged.neoforge.event.EventHooks.onExplosionDetonate(level, this, list, radius + radius);
        for (Entity entity : list) {
            if (entity.ignoreExplosion(this)) continue;

            if (damageCalculator.shouldDamageEntity(this, entity)) {
                entity.hurt(damageSource, Math.min(damageCalculator.getEntityDamageAmount(this, entity), radius * 10));
            }
            if (entity instanceof Player player && !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                hitPlayers.put(player, Vec3.ZERO);
            }
            entity.onExplosionHit(source);
        }
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        if (!level.isClientSide && interactsWithBlocks()) {
            level.getProfiler().push("explosion_blocks");
            List<Pair<ItemStack, BlockPos>> drops = new ArrayList<>();
            Util.shuffle(toBlow, level.random);

            for (BlockPos blockpos : toBlow) {
                level.getBlockState(blockpos).onExplosionHit(level, blockpos, this, (stack, pos) -> addOrAppendStack(drops, stack, pos));
            }

            for (Pair<ItemStack, BlockPos> pair : drops) {
                Block.popResource(level, pair.getSecond(), pair.getFirst());
            }

            level.getProfiler().pop();
        }
    }

    public static Explosion terraExplode(
            ServerLevel level,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x,
            double y,
            double z,
            float radius,
            Level.ExplosionInteraction explosionInteraction
    ) {
        return level.explode(source, damageSource, damageCalculator, x, y, z, radius * 1.5F, false, explosionInteraction);
    }

    private static Explosion.BlockInteraction getDestroyType(ServerLevel level, GameRules.Key<GameRules.BooleanValue> gameRule) {
        return level.getGameRules().getBoolean(gameRule) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
    }
}
