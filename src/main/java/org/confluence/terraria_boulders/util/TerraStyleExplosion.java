package org.confluence.terraria_boulders.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TerraStyleExplosion extends ServerExplosion {

    public TerraStyleExplosion(
            ServerLevel level,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            Vec3 center,
            float radius,
            Explosion.BlockInteraction blockInteraction
    ) {
        super(level, source, damageSource, damageCalculator, center, radius, false, blockInteraction);
    }
//
//    @Override
//    public int explode() {
//        Set<BlockPos> toBlowSet = new HashSet<>();
//        level.gameEvent(source, GameEvent.EXPLODE, center);
//
//        float obsidianBasedExplosionResistance = ModBlocks.getObsidianBasedExplosionResistance(0);
//        BlockPos origin = BlockPos.containing(center);
//        AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius, center.x + radius, center.y + radius, center.z + radius);
//        double radiusP2 = radius * radius;
//        double inner = Mth.square(Mth.floor(radius - 0.5F));
//
//        BlockPos.betweenClosedStream(area).forEach(pos -> {
//            double sqr = pos.distSqr(origin);
//            if (level.isInWorldBounds(pos) && sqr <= radiusP2) {
//                if (!level.getFluidState(pos).isEmpty()) return; // 无视流体
//                BlockState blockState = level.getBlockState(pos);
//                damageCalculator.getBlockExplosionResistance(this, level, pos, blockState, Fluids.EMPTY.defaultFluidState()).ifPresent(resistance -> {
//                    if (resistance < obsidianBasedExplosionResistance) {
//                        if (sqr < inner || this.level.getRandom().nextFloat() < 0.8F) {
//                            toBlowSet.add(pos.immutable());
//                        }
//                    }
//                });
//            }
//        });
//
//        List<Entity> list = level.getEntities(source, area);
//        List<BlockPos> toBlowList = List.copyOf(toBlowSet);
//        EventHooks.onExplosionDetonate(level, this, list, toBlowList);
//        for (Entity entity : list) {
//            if (entity.ignoreExplosion(this)) continue;
//
//            if (damageCalculator.shouldDamageEntity(this, entity)) {
//                entity.hurtServer(level, damageSource, Math.min(damageCalculator.getEntityDamageAmount(this, entity, ServerExplosion.getSeenPercent(center, entity)), radius * 10));
//            }
//            if (entity instanceof Player player && !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
//                hitPlayers.put(player, Vec3.ZERO);
//            }
//            entity.onExplosionHit(source);
//        }
//        if (interactsWithBlocks()) {
//            ProfilerFiller profiler = Profiler.get();
//            profiler.push("explosion_blocks");
//            List<Pair<ItemStack, BlockPos>> drops = new ArrayList<>();
//            Util.shuffle(toBlowList, level.getRandom());
//
//            for (BlockPos blockpos : toBlowSet) {
//                level.getBlockState(blockpos).onExplosionHit(level, blockpos, this, (stack, pos) -> addOrAppendStack(drops, stack, pos));
//            }
//
//            for (Pair<ItemStack, BlockPos> pair : drops) {
//                Block.popResource(level, pair.getSecond(), pair.getFirst());
//            }
//
//            profiler.pop();
//        }
//        ParticleOptions explosionParticle = this.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;
//        int blockCount = toBlowList.size();
//
//        for (ServerPlayer player : level.players()) {
//            if (player.distanceToSqr(center) < 4096.0) {
//                Optional<Vec3> playerKnockback = Optional.ofNullable(this.getHitPlayers().get(player));
//                player.connection.send(new ClientboundExplodePacket(center, radius, blockCount, playerKnockback, explosionParticle,  SoundEvents.GENERIC_EXPLODE, Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES));
//            }
//        }
//        return blockCount;
//    }

    public static void terraExplode(
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
        level.explode(source, damageSource, damageCalculator, x, y, z, radius * 1.5F, false, explosionInteraction);
    }
}
