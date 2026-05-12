package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jspecify.annotations.NonNull;

public class CamouflagedBoulderEntity extends BoulderEntity {
    private final float NO_COLLISION_BLOCK_RADIUS = 0.1F;

    public CamouflagedBoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CamouflagedBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), level, pos, blockState);
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

    //在服务端设置伪装时刷新碰撞箱
    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        this.refreshDimensionsFromBlock();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        //方块状态改变了同步更新碰撞箱
        if (getBlockStateAccessor().equals(key)) {
            refreshDimensionsFromBlock();
        }
    }

    //物理引擎拿碰撞箱大小
    @Override
    @NonNull
    public EntityDimensions getDimensions(Pose pose) {
        BlockState mimicState = this.getBlockState();

        if (mimicState != null && !mimicState.isAir()) {
            VoxelShape shape = mimicState.getCollisionShape(this.level(), BlockPos.ZERO);
            if (!shape.isEmpty()) {
                AABB bounds = shape.bounds();
                float width = (float) Math.max(bounds.getXsize(), bounds.getZsize());
                float height = (float) bounds.getYsize();
                return EntityDimensions.fixed(width, height);
            } else {
                return EntityDimensions.fixed(NO_COLLISION_BLOCK_RADIUS / 2.0F, NO_COLLISION_BLOCK_RADIUS / 2.0F);//无碰撞方块
            }
        }
        return super.getDimensions(pose);
    }

    //巨石换伪装时主动调用
    public void refreshDimensionsFromBlock() {
        BlockState state = getBlockState();

        if (state != null && !state.isAir()) {
            VoxelShape shape = state.getCollisionShape(level(), BlockPos.ZERO);
            if (!shape.isEmpty()) {
                AABB bounds = shape.bounds();
                //同步更新滚动动画半径
                this.radius = (float) Math.max(bounds.getXsize(), bounds.getZsize()) / 2.0F;
            } else {
                this.radius = NO_COLLISION_BLOCK_RADIUS;//无碰撞方块给个极小的半径
            }
        }

        //刷新黑框和物理体积
        this.refreshDimensions();
    }
}
