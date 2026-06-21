package org.confluence.terraria_boulders.init;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CamouflagedBoulderBehaviours {
    private static final Object2ObjectMap<Block, CamouflagedBoulderEntity.CamouflagedBehaviour> BEHAVIOURS = new Object2ObjectArrayMap<>();

    @Nullable
    public static CamouflagedBoulderEntity.CamouflagedBehaviour get(Block block) {
        return BEHAVIOURS.get(block);
    }

    public static Object2ObjectMap<Block, CamouflagedBoulderEntity.CamouflagedBehaviour> getAll() {
        return Object2ObjectMaps.unmodifiable(BEHAVIOURS);
    }

    public static void init() {

    }

    //雪块
    public static final CamouflagedBoulderEntity.CamouflagedBehaviour SNOW_BLOCK = create(Blocks.SNOW_BLOCK, new CamouflagedBoulderEntity.CamouflagedBehaviour() {
        //滚过的路上生成顶层雪
        @Override
        public void onTick(CamouflagedBoulderEntity entity, Runnable baseTick) {
            baseTick.run();
            BlockPos pos = entity.blockPosition(); //巨石当前位置
            Level level = entity.level();
            if (level.isClientSide()) return;
            if (level.isEmptyBlock(pos) && level.getBlockState(pos.below()).isSolid()) {
                level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
            }
        }

        //冻结生物
        @Override
        public void onHitEntity(CamouflagedBoulderEntity entity, Consumer<EntityHitResult> baseHitEntity, EntityHitResult result) {
            baseHitEntity.accept(result);//父类方法
            if (!entity.level().isClientSide()) {
                if (result.getEntity() instanceof LivingEntity living) {
                    living.setTicksFrozen(living.getTicksFrozen() + 100);
                }
            }
            else{
                //在后面加雪花粒子
                entity.level().addParticle(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY(), entity.getZ(), 0, 0.1, 0);
            }
        }

        @Override
        public float getDamage(EntityHitResult result) {return 0.0F;}//雪块巨石没伤害
    });

    //黑曜石
    public static final CamouflagedBoulderEntity.CamouflagedBehaviour OBSIDIAN = create(Blocks.OBSIDIAN, new CamouflagedBoulderEntity.CamouflagedBehaviour() {
        @Override
        public void onHitBlock(CamouflagedBoulderEntity e, Consumer<BlockHitResult> baseHitBlock, BlockHitResult r) {
            Level level = e.level();
            Direction direction = r.getDirection();

            // 正面撞墙
            if (direction.getAxis() != Direction.Axis.Y) {
                boolean destroyedAny = false;

                //矢量扫荡，顺着巨石上一帧的速度（preMoveVelocity）向前拉伸碰撞箱，这样较薄的方块也可以
                Vec3 intendedMove = e.preMoveVelocity != null ? e.preMoveVelocity : Vec3.ZERO;
                AABB scanBox = e.getBoundingBox().expandTowards(intendedMove).inflate(0.05);//加一点容差

                BlockPos minPos = BlockPos.containing(scanBox.minX, scanBox.minY, scanBox.minZ);
                BlockPos maxPos = BlockPos.containing(scanBox.maxX, scanBox.maxY, scanBox.maxZ);

                // 遍历接触到的所有方块
                for (BlockPos targetPos : BlockPos.betweenClosed(minPos, maxPos)) {
                    BlockState state = level.getBlockState(targetPos);

                    if (!state.isAir()) {
                        float hardness = state.getDestroySpeed(level, targetPos);

                        // 撞到的方块硬度小于5，干碎
                        if (hardness >= 0.0F && hardness < 5.0F) {
                            //第二个参数，客户端不掉落物品
                            level.destroyBlock(targetPos, !level.isClientSide(), e);
                            destroyedAny = true;
                        }
                    }
                }

                // 只要撞碎一块
                if (destroyedAny) {
                    // 在服务端处理数据
                    if (!level.isClientSide()) {
                        e.setBreakValue(e.getBreakValue() + 1.0f); // 增加损坏值
                    }

                    //双端同时纠正速度
                    if (e.preMoveVelocity != null) {
                        e.setDeltaMovement(e.preMoveVelocity);
                    }

                    return;//不触发撞墙反弹
                }
            }

            if (!level.isClientSide()) {
                e.setBreakLimit(10.0f); // 设置固定耐久值
            }

            // 如果全是硬墙拆不动，或者正在落地，执行父类的反弹物理
            baseHitBlock.accept(r);
        }
    });

    public static CamouflagedBoulderEntity.CamouflagedBehaviour create(Block block, CamouflagedBoulderEntity.CamouflagedBehaviour behaviour) {
        BEHAVIOURS.put(block, behaviour);
        return behaviour;
    }
}
