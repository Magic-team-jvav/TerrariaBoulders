package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.confluence.terraria_boulders.util.VectorUtils;

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

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        Vec3 motion = VectorUtils.relativeScale(getDeltaMovement(), blockHitResult.getDirection().getAxis(), -bounceFactor);
        if (Math.abs(motion.y) < 0.01) motion = new Vec3(motion.x, 0.0, motion.z);
        setDeltaMovement(motion.scale(frictionFactor));
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
