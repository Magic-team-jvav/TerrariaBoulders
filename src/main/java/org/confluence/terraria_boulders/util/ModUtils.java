package org.confluence.terraria_boulders.util;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ModUtils {
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> a, BlockEntityType<E> b, BlockEntityTicker<? super E> ticker) {
        return a == b ? (BlockEntityTicker<A>) ticker : null;
    }

    public static boolean isSingleplayerOwner(ServerPlayer player) {
        return player.server.isSingleplayerOwner(player.nameAndId());
    }

    public static final class VectorUtil {
        /**
         * 给予实体B一个击退动量，方向为A→B
         *
         * @param a       实体A
         * @param b       实体B
         * @param scale   击退动量的缩放
         * @param motionY 击退的Y轴动量
         */
        public static void knockBackA2B(Entity a, Entity b, double scale, double motionY) {
            if (b instanceof LivingEntity living) {
                AttributeInstance instance = living.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                if (instance != null) scale *= (1.0 - instance.getValue());
            }
            if (scale > 0.0) {
                LivingEntity living = null;
                if (a instanceof TraceableEntity traceable && traceable.getOwner() instanceof LivingEntity living1)
                    living = living1;
                else if (a instanceof LivingEntity living1) living = living1;
                if (living != null) {
                    AttributeInstance instance = living.getAttribute(Attributes.ATTACK_KNOCKBACK);
                    if (instance != null) scale *= (1.0 + instance.getValue());
                }
                b.addDeltaMovement(getVectorA2B(a, b).scale(scale).add(0.0, motionY, 0.0));
            }
        }

        /**
         * 获得从实体A到实体B的单位向量，即A→B
         *
         * @param a 实体A
         * @param b 实体B
         * @return A→B的单位向量
         */
        public static Vec3 getVectorA2B(Entity a, Entity b) {
            return b.position().subtract(a.position()).normalize();
        }

        /**
         * 将输入的向量的某个轴乘一个缩放
         *
         * @param vec3  输入的向量
         * @param axis  某个轴
         * @param scale 缩放
         * @return 新向量
         */
        public static Vec3 relativeScale(Vec3 vec3, Direction.Axis axis, double scale) {
            double x = axis == Direction.Axis.X ? scale * vec3.x : vec3.x;
            double y = axis == Direction.Axis.Y ? scale * vec3.y : vec3.y;
            double z = axis == Direction.Axis.Z ? scale * vec3.z : vec3.z;
            return new Vec3(x, y, z);
        }
    }

    public static final class MathUtil {
        public static float epsilon = 1e-6f;

        public static boolean equal(float a, float b){
            return Math.abs(a - b) <= epsilon;
        }
    }

    public static final class LevelUtil {

        //无任何碰撞
        public static boolean noCollision(Level level, Entity entity, AABB aabb) {
            return level.noCollision(entity, aabb) && noEntityCollision(level, entity, aabb);//补上原版的空缺
        }

        //范围内是否没有实体，因为原版的noEntityCollision检测是是废品
        public static boolean noEntityCollision(Level level, Entity entity, AABB aabb) {
            return getLivingEntities(level, entity, aabb).isEmpty();
        }

        //捕获范围内实体
        public static List<LivingEntity> getLivingEntities(Level level, Entity entity, AABB aabb) {
            return level.getEntitiesOfClass(LivingEntity.class, aabb, livingEntity -> !livingEntity.equals(entity));//过滤实体
        }

        public static List<LivingEntity> getLivingEntities(Level level, AABB aabb) {
            return getLivingEntities(level, null, aabb);
        }
    }

    public static final class EntityUtil {
        public static boolean isSurvival(Entity entity) {
            return entity instanceof Player player && isSurvival(player);
        }

        public static boolean isSurvival(Player player){
            return !player.isCreative() && !player.isSpectator();
        }

        //是普通生物/生存模式玩家
        public static boolean isSurvivalOrMob(Entity entity) {
            return !(entity instanceof Player player) || isSurvival(player);
        }
    }
}
