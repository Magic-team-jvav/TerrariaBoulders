package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.terraria_boulders.init.CamouflagedBoulderBehaviours;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class CamouflagedBoulderEntity extends BoulderEntity {
    private @Nullable CamouflagedBehaviour behaviour;

    static {
        CamouflagedBoulderBehaviours.init();
    }

    public CamouflagedBoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CamouflagedBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), level, pos, blockState);
    }

    @Override
    public void tick() {
        //让行为决定调不调用
        //运行行为
        if (behaviour != null) {
            behaviour.onTick(this, super::tick);
        } else {//没有特殊行为就正常调用
            super.tick();
        }
    }

    @Override
    protected void onBoulderHitEntity(EntityHitResult result) {
        //运行行为
        if (behaviour != null) {
            behaviour.onHitEntity(this, super::onBoulderHitEntity, result);
        } else {
            super.onBoulderHitEntity(result);
        }
    }

    @Override
    protected void onBoulderHitBlock(BlockHitResult result) {
        //运行行为
        if (behaviour != null) {
            behaviour.onHitBlock(this, super::onBoulderHitBlock, result);
        } else {
            super.onBoulderHitBlock(result);
        }
    }

    //查表获取行为
    @Nullable
    public CamouflagedBehaviour getBehaviour() {
        return CamouflagedBoulderBehaviours.get(this.getBlockState().getBlock());
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

    @Override
    public float getDamage(EntityHitResult result){
        if(this.behaviour != null) {
            float damage = this.behaviour.getDamage(result);
            if(damage == CamouflagedBehaviour.USE_DEFAULT) {
                return super.getDamage(result);
            }
            return damage;
        }
        else{
            return super.getDamage(result);
        }
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
        this.behaviour = this.getBehaviour();
    }

    public interface CamouflagedBehaviour {
        float USE_DEFAULT = Float.MAX_VALUE;

        default void onTick(CamouflagedBoulderEntity entity, Runnable/*无参方法*/ baseTick) {baseTick.run();}

        default void onHitEntity(CamouflagedBoulderEntity entity, Consumer<EntityHitResult>/*单参无返回值方法*/ baseOnHitEntity, EntityHitResult result) {baseOnHitEntity.accept(result);}

        default void onHitBlock(CamouflagedBoulderEntity entity, Consumer<BlockHitResult> baseOnHitBlock/*BiConsumer<BlockHitResult, Direction>*//*双参无返回值方法*/, BlockHitResult result) {baseOnHitBlock.accept(result);}

        default float getDamage(EntityHitResult result) {return USE_DEFAULT;}//默认USE_DEFAULT代表用原方法默认值
    }
}
