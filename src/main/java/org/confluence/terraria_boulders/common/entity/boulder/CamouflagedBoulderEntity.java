package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CamouflagedBoulderEntity extends BoulderEntity {

    //存BlockState的数据访问器
    //private static final EntityDataAccessor<BlockState> MIMIC_STATE = SynchedEntityData.defineId(CamouflagedBoulderEntity.class, EntityDataSerializers.BLOCK_STATE);

    public CamouflagedBoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
    }

//    @Override
//    protected void defineSynchedData(SynchedEntityData.Builder builder) {
//        super.defineSynchedData(builder);
//        //默认石头
//        builder.define(MIMIC_STATE, Blocks.STONE.defaultBlockState());
//    }

    //Getter and Setter
    public BlockState getMimicState() {
        //return this.entityData.get(MIMIC_STATE);
        return this.getBlockState();
    }

    public void setMimicState(BlockState state) {
        //this.entityData.set(MIMIC_STATE, state);
        this.setBlockState(state);
    }

//    public boolean isMimicLocked() {
//        return this.isLocked();
//    }
//
//    public void setMimicLocked(boolean locked) {
//        this.setLocked(locked);
//    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        BlockState currentState = this.getMimicState();
        if (currentState != null && !currentState.isAir()) {
            //存数据
            output.storeNullable("MimicState", BlockState.CODEC, currentState);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        //通过 BlockState.CODEC解包
        BlockState savedState = input.read("MimicState", BlockState.CODEC).orElse(null);

        if (savedState != null && !savedState.isAir()) {
            this.setMimicState(savedState);
        }
//        else {
//            this.setMimicState(Blocks.STONE.defaultBlockState());//兜底
//        }
    }
}
