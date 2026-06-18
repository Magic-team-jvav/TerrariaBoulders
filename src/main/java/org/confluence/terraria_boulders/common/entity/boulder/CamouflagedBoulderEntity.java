package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CamouflagedBoulderEntity extends BoulderEntity {

    public CamouflagedBoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CamouflagedBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), level, pos, blockState);
    }

    @Override
    public void tick() {
        //super.tick();//让行为决定调用
        //运行行为
        CamouflagedBoulderBehaviour behaviour = this.getBehaviour();
        if(behaviour != null) {
            behaviour.onTick(this, super::tick);
        }
        else{//没有特殊行为就正常调用
            super.tick();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        //super.onHitEntity(result);
        //运行行为
        CamouflagedBoulderBehaviour behaviour = this.getBehaviour();
        if(behaviour != null) {
            behaviour.onHitEntity(this, super::onHitEntity, result);
        }
        else{
            super.onHitEntity(result);
        }
    }

    @Override
    protected void onBoulderHitBlock(BlockHitResult result) {
        //运行行为
        CamouflagedBoulderBehaviour behaviour = this.getBehaviour();
        if(behaviour != null) {
            behaviour.onHitBlock(this, super::onBoulderHitBlock, result);
        }
        else{
            super.onBoulderHitBlock(result);
        }
    }

    //查表获取行为
    public CamouflagedBoulderBehaviour getBehaviour() {
        return CamouflagedBoulderBehaviours.BEHAVIOURS.get(this.getBlockState().getBlock());
    }

    //将方块的原生摩擦力系数转化为衰减系数
//    @Override
//    protected void applyFrictionAndRotation() {
//        BlockState state = getBlockState();
//
//        float blockFriction = state.getBlock().getFriction();
//        double finalFriction = 0.99 - (1.0 - blockFriction) * 0.5;
//
//        Vec3 deltaMovement = getDeltaMovement().scale(finalFriction);
//        setDeltaMovement(deltaMovement);
//        rotate(deltaMovement);
//    }

    /**
     * 从方块状态中提取碰撞箱（或交互箱兜底）的宽高
     */
    @Nullable
    private Vec2 getBlockSize(BlockState state) {
        VoxelShape shape = state.getCollisionShape(level(), BlockPos.ZERO);
        if (shape.isEmpty()) {
            shape = state.getInteractionShape(level(), BlockPos.ZERO);
        }
        if (shape.isEmpty()) return null;

        AABB bounds = shape.bounds();
        return new Vec2((float) Math.max(bounds.getXsize(), bounds.getZsize()), (float) bounds.getYsize());
    }

    //设置伪装时同步更新碰撞箱
    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        this.updateDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        //方块状态改变了同步更新碰撞箱
        if (getBlockStateAccessor().equals(key)) {
            updateDimensions();
        }
    }

    //物理引擎拿碰撞箱大小
    @Override
    @NonNull
    public EntityDimensions getDimensions(Pose pose) {
        BlockState state = getBlockState();
        if (state == null || state.isAir()) {
            return super.getDimensions(pose);
        }
        Vec2 size = getBlockSize(state);
        if (size != null) {
            return EntityDimensions.fixed(size.x, size.y);
        }
        // 无碰撞方块
        return EntityDimensions.fixed(1, 1);
    }

    //伪装方块改变时主动调用：同步物理体积和滚动半径
    public void updateDimensions() {
        BlockState state = getBlockState();
        if (state == null || state.isAir()) {
            refreshDimensions();
            return;
        }
        Vec2 size = getBlockSize(state);
        this.radius = size != null ? Math.max(size.x, size.y) / 2.0F : 0.5f;
        refreshDimensions();
    }

    public interface CamouflagedBoulderBehaviour {
        void onTick(CamouflagedBoulderEntity entity, Runnable/*无参方法*/ baseTick);
        void onHitEntity(CamouflagedBoulderEntity entity, Consumer<EntityHitResult>/*单参无返回值方法*/ baseHitEntity, EntityHitResult result);
        void onHitBlock(CamouflagedBoulderEntity entity, Consumer<BlockHitResult> baseHitBlock/*BiConsumer<BlockHitResult, Direction>*//*双参无返回值方法*/, BlockHitResult result);
    }

    public static class CamouflagedBoulderBehaviours {

        //绑定方块行为
        private static final Map<Block, CamouflagedBoulderBehaviour> BEHAVIOURS = Map.of(

                //雪块
                Blocks.SNOW_BLOCK, new CamouflagedBoulderBehaviour() {
                    //滚过的路上生成顶层雪
                    @Override
                    public void onTick(CamouflagedBoulderEntity entity, Runnable baseTick) {
                        baseTick.run();
                        BlockPos pos = entity.blockPosition();//巨石当前位置
                        Level level = entity.level();
                        if(level.isClientSide()) return;
                        if (level.isEmptyBlock(pos) && level.getBlockState(pos.below()).isSolid()) {
                            level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
                        }
                    }
                    //冻结生物
                    @Override
                    public void onHitEntity(CamouflagedBoulderEntity entity, Consumer<EntityHitResult> baseHitEntity, EntityHitResult result) {
                        baseHitEntity.accept(result);//父类方法
                        if(entity.level().isClientSide()) return;
                        if (result.getEntity() instanceof LivingEntity living) {
                            living.setTicksFrozen(living.getTicksFrozen() + 100);
                        }
                    }
                    @Override
                    public void onHitBlock(CamouflagedBoulderEntity entity, Consumer<BlockHitResult> baseHitBlock, BlockHitResult result) {baseHitBlock.accept(result);}
                },

                //黑曜石
                Blocks.OBSIDIAN, new CamouflagedBoulderBehaviour() {
                    @Override
                    public void onTick(CamouflagedBoulderEntity entity, Runnable baseTick) {baseTick.run();}
                    @Override
                    public void onHitEntity(CamouflagedBoulderEntity entity, Consumer<EntityHitResult> baseHitEntity, EntityHitResult result) {baseHitEntity.accept(result);}
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
                                    e.setDamageValue(e.getDamageValue() + 1.0f); // 增加损坏值
                                }

                                //双端同时纠正速度
                                if (e.preMoveVelocity != null) {
                                    e.setDeltaMovement(e.preMoveVelocity);
                                }

                                return;//不触发撞墙反弹
                            }
                        }

                        if (!level.isClientSide()) {
                            e.setDurability(10.0f); // 设置固定耐久值
                        }

                        // 如果全是硬墙拆不动，或者正在落地，执行父类的反弹物理
                        baseHitBlock.accept(r);
                    }
                }
        );
    }
}
