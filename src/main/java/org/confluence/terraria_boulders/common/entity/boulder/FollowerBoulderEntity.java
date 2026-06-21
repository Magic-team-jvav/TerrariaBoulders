package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jetbrains.annotations.Nullable;

public class FollowerBoulderEntity extends BoulderEntity {
    private int tick;
    private Entity target;

    public FollowerBoulderEntity(EntityType<FollowerBoulderEntity> entityType, Level level) {
        super(entityType, level);
        this.stepHeightDenominator = 1.0F;
        this.unbreakable = true;
    }

    public FollowerBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(level, pos, blockState);
        this.stepHeightDenominator = 1.0F;
        this.unbreakable = true;
    }

    public FollowerBoulderEntity(EntityType<FollowerBoulderEntity> entityType, Level level, Vec3 pos, BlockState blockState) {
        super(entityType, level, pos, blockState);
        this.stepHeightDenominator = 1.0F;
        this.unbreakable = true;
    }


    @Override
    public void baseTick() {
        super.baseTick();
        if (target != null) {
            Vec3 vec3 = target.position().subtract(position()).normalize();
            vec3 = new Vec3(vec3.x, 0.0, vec3.z);
            setDeltaMovement(vec3.scale(speed / 1.75F));
            this.yRotO = getYRot();
            setYRot((float) (Mth.atan2(vec3.x, vec3.z) * Mth.RAD_TO_DEG));
            if (distanceTo(target) >= 30) onRemove();
        }
        if (this.tick++ >= 20 * 20) onRemove();
    }

    @Override
    protected void onBoulderHitEntity(EntityHitResult entityHitResult) {
        super.onBoulderHitEntity(entityHitResult);
        onRemove();
    }

    @Override
    public void targetTo(@Nullable Entity entity) {
        if (entity != null) {
            target = entity;
        }
    }
}
