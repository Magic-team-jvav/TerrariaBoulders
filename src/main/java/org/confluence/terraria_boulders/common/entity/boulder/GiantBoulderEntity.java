package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.init.ModEntityTypes;

public class GiantBoulderEntity extends BoulderEntity {
    private int Size = 3;

    //给EntityType.Builder使用的构造函数
    public GiantBoulderEntity(EntityType<? extends GiantBoulderEntity> type, Level level) {
        super(type, level);
    }

    //给BoulderFactory使用的构造函数
    public GiantBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.GIANT_BOULDER.get(), level, pos, blockState);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        //相当于动态计算.sized()
        float currentSize = (float) this.getSize();
        return EntityDimensions.scalable(currentSize, currentSize);
    }

    public int getSize() {
        return this.Size;
    }

    public void setSize(int size) {
        this.Size = size;
        this.refreshDimensions();//重新计算世界碰撞箱
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSize(input.getIntOr("Size", 3));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Size", this.Size);
    }
}
