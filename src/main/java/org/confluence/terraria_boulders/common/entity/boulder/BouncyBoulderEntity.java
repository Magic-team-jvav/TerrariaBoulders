package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.init.ModEntityTypes;

public class BouncyBoulderEntity extends BoulderEntity {

    public BouncyBoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
        speed = 0.9;
        bounceFactor = 1.2;
    }

    public BouncyBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.BOUNCY_BOULDER.get(), level, pos, blockState);
        speed = 0.9;
        bounceFactor = 1.2;
    }

//    @Override
//    protected void onHitBlock(BlockHitResult blockHitResult) {
//        super.onHitBlock(blockHitResult);
//        Vec3 motion = VectorUtils.relativeScale(getDeltaMovement(), blockHitResult.getDirection().getAxis(), -bounceFactor);
//        //if (Math.abs(motion.y) < 0.01) motion = new Vec3(motion.x, 0.0, motion.z);
//        if (blockHitResult.getDirection().getAxis() == Direction.Axis.Y && Math.abs(motion.y) < 0.08) {//防止在平地上高频抽搐
//            motion = new Vec3(motion.x, 0.0, motion.z);
//        }
//        setDeltaMovement(motion.scale(frictionFactor));
//    }
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        Vec3 motion = getDeltaMovement();//当前速度
        Direction.Axis axis = blockHitResult.getDirection().getAxis();
        double currentX = motion.x;
        double currentY = motion.y;
        double currentZ = motion.z;

        //读取预测速度，而不是现在可能撞墙归零了的速度
        if (axis == Direction.Axis.X) {
            currentX = -this.preMoveVelocity.x * bounceFactor;
        } else if (axis == Direction.Axis.Y) {
            //下落太慢就只滚动
            if (Math.abs(this.preMoveVelocity.y) > 0.08) {
                //计算越弹越高的速度
                double rawY = -this.preMoveVelocity.y * bounceFactor;
                currentY = Math.min(rawY, 1.5);//垂直速度保险
            } else {
                currentY = 0.0;
            }
        } else if (axis == Direction.Axis.Z) {
            currentZ = -this.preMoveVelocity.z * bounceFactor;
        }

        //水平速度保险
        currentX = Math.max(-1.5, Math.min(currentX, 1.5));
        currentZ = Math.max(-1.5, Math.min(currentZ, 1.5));

        setDeltaMovement(new Vec3(currentX, currentY, currentZ));
    }

    @Override
    protected void verticalHitRebound(BlockHitResult blockHitResult, Direction direction) {
    }

    @Override
    protected void horizontalHitBlock(BlockHitResult blockHitResult, Direction direction) {
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.bounceFactor = input.getDoubleOr("BounceFactor",1.2);
        this.frictionFactor = input.getDoubleOr("FrictionFactor",0.9);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putDouble("BounceFactor", bounceFactor);
        output.putDouble("FrictionFactor", frictionFactor);
    }
}
