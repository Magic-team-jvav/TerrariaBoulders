package org.confluence.terraria_boulders.common.entity.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.stream.StreamSupport;

public class GiantBoulderBlockEntity extends BlockEntity {
    private Iterable<BlockPos> RelativePosIter;//相对坐标

    public GiantBoulderBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntityTypes.GIANT_BOULDER.get(), worldPosition, blockState);
    }

    public Iterable<BlockPos> getRelativePosIter() { return RelativePosIter; }
    public void setRelativePosIter(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {//第一个为自己，也是相对坐标原点
        RelativePosIter = getRelativePos(BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ), pos);
        this.syncData();
    }
    public void setRelativePosIter(BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        RelativePosIter = getRelativePos(BlockPos.betweenClosed(minPos, maxPos), pos);
        this.syncData();
    }
    //public void setRelativePosIter(AABB aabb) { RelativePosIter = BlockPos.betweenClosed(aabb); }

    /**
     * @param absolutePositions 坐标范围的闭区间迭代器
     * @param pos 原点坐标
     * @return 相对坐标的 Iterable
     * */
    private static Iterable<BlockPos> getRelativePos(Iterable<BlockPos> absolutePos, BlockPos pos){
        //使用 Stream 将绝对坐标转换为相对坐标，并设置为Immutable
        return StreamSupport.stream(absolutePos.spliterator(), false)
                        //相对坐标 = 绝对坐标 - 原点坐标 (pos)
                        .map(absPos -> absPos.subtract(pos).immutable())
                        //收集为不可变列表，防止 DFU 序列化或后续遍历时数据出错
                        .collect(ImmutableList.toImmutableList());
    }

    //同步数据
    private void syncData() {
        //标记数据改变
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState currentState = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, currentState, currentState, 3);//发送同步
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.RelativePosIter != null) {
            output.storeNullable("RelativePosIter", BlockPos.CODEC.listOf(), ImmutableList.copyOf(this.RelativePosIter));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.RelativePosIter = input.read("RelativePosIter", BlockPos.CODEC.listOf()).orElse(Collections.emptyList());
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @NonNull
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
