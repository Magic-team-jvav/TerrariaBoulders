package org.confluence.terraria_boulders.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;

public class CannonSeatEntity extends Entity {
    public CannonSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();

        //检查脚下是不是大炮
        if (!(this.level().getBlockEntity(this.blockPosition()) instanceof BoulderCannonBlockEntity be)) {
            if (!this.level().isClientSide()) {
                this.discard();//服务端销毁实体
            }
            return;
        }

        //视角同步
        Entity passenger = this.getFirstPassenger();
        if (passenger != null) {
            //让大炮的目标角度等于玩家的视角
            be.targetYaw = passenger.getYRot();
            be.setTargetPitch(passenger.getXRot());

            //让座位实体本身也跟着玩家转，防止玩家下车时视角乱飘
            this.setYRot(passenger.getYRot());
            this.setXRot(passenger.getXRot());
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

        } else if (!this.level().isClientSide()) {
            //没有乘客自动销毁
            this.discard();
            this.level().playSound(null, this.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.3F);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {}
}
