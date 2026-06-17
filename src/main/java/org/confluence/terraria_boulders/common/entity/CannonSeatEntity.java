package org.confluence.terraria_boulders.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.block.BoulderCannonBlock;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;

public class CannonSeatEntity extends Entity {
    public CannonSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    //在上面左键时触发开火
    public void onLeftClick(Player player){
        BlockPos pos = BlockPos.containing(this.position());
        Level level = this.level();
        BlockEntity be = level.getBlockEntity(pos);
        if(be == null) return;
        BlockState state = be.getBlockState();
        if(state.getBlock() instanceof BoulderCannonBlock bcBlock){
            bcBlock.launch(state, level, pos, true);
        }
    }

    //在上面右键时触发装填
    public void onRightClick(Player player){
        BlockPos pos = BlockPos.containing(this.position());
        Level level = this.level();
        BlockEntity be = level.getBlockEntity(pos);
        if(be == null) return;
        BlockState state = be.getBlockState();
        if(be instanceof BoulderCannonBlockEntity bcbe && state.getBlock() instanceof BoulderCannonBlock bcBlock){
            //获取手上的物品，右手物品为空则获取左手
            ItemStack stack = player.getMainHandItem();
            if(stack.isEmpty()) stack = player.getOffhandItem();
            //装弹
            bcBlock.reload(pos, bcbe, level, stack, player);
        }

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
            //be.targetYaw = passenger.getYRot();
            //be.setTargetPitch(passenger.getXRot());
            be.setTarget(passenger.getYRot(), passenger.getXRot());

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
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        passenger.absSnapRotationTo(this.getViewYRot(0.0F), this.getViewXRot(0.0F));
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        if (passenger instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRot = getYRot();
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return super.getPassengerAttachmentPoint(passenger, dimensions, scale).add(0, 0.5, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
    }
}
