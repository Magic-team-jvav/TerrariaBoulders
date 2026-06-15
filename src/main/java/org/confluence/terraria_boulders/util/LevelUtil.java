package org.confluence.terraria_boulders.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class LevelUtil {

    //无任何碰撞
    public static boolean noCollision(Level level, Entity entity, AABB aabb){
        return level.noCollision(entity, aabb) && noEntityCollision(level, entity, aabb);//补上原版的空缺
    }

    //范围内是否没有实体，因为原版的noEntityCollision检测是是废品
    public static boolean noEntityCollision(Level level, Entity entity, AABB aabb){
        return getLivingEntities(level, entity, aabb).isEmpty();
    }

    //捕获范围内实体
    public static List<LivingEntity> getLivingEntities(Level level, Entity entity, AABB aabb){
        return level.getEntitiesOfClass(LivingEntity.class, aabb, livingEntity -> !livingEntity.equals(entity));//过滤实体
    }

    public static List<LivingEntity> getLivingEntities(Level level, AABB aabb){
        return getLivingEntities(level, null, aabb);
    }
}
