package org.confluence.terraria_boulders.common.entity.block;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CamouflagedBoulderBlockEntity extends BlockEntity {
    public static final ModelProperty<BlockState> MIMIC_STATE_PROPERTY = new ModelProperty<>();
    private BlockState mimicState = Blocks.STONE.defaultBlockState();
    private boolean isLocked = false;

    public CamouflagedBoulderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.CAMOUFLAGED_BOULDER.get(), pos, state);
    }

    //----------Getter and Setter----------

    public BlockState getMimicState() {
        return mimicState;
    }

    public void setMimicState(BlockState mimicState) {
        this.mimicState = mimicState;
        //this.setChanged();
        //在客户端收到数据，强制重新构建ModelData并刷新渲染
        if (this.level != null && this.level.isClientSide()) {
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);//重绘
        }
    }

    public boolean isLocked() { return this.isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }

    @Override
    @NonNull
    public BlockState getBlockState() {
        return this.getMimicState();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.mimicState != null && !this.mimicState.isAir()) {
            output.storeNullable("MimicState", BlockState.CODEC, this.mimicState);
        }
        output.storeNullable("IsLocked", Codec.BOOL, this.isLocked);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        BlockState savedState = input.read("MimicState", BlockState.CODEC).orElse(null);
        if (savedState != null && !savedState.isAir()) {
            this.setMimicState(savedState);
        }
        isLocked = input.read("IsLocked", Codec.BOOL).orElse(false);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(MIMIC_STATE_PROPERTY, this.mimicState)
                .build();
    }

    //把带有组件的物品放在地上时，会自动调用这个方法把组件塞进方块
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);

        BlockState state = components.get(ModDataComponents.MIMIC_STATE.get());
        if (state != null) this.mimicState = state;
        this.isLocked = components.getOrDefault(ModDataComponents.IS_LOCKED.get(), false);
    }

    //用精准采集挖掉方块或者Ctrl中键复制方块时调用这个方法把数据塞回物品里
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        if (this.mimicState != null && !this.mimicState.isAir()) {
            components.set(ModDataComponents.MIMIC_STATE.get(), this.mimicState);
        }
        components.set(ModDataComponents.IS_LOCKED, this.isLocked);
    }

    //返回要发送到客户端的数据包
    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //方块须手动发包同步
    @Override
    @NonNull
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (!(state.getBlock() instanceof CamouflagedBoulderBlock boulderBlock)) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        boulderBlock.summonBoulder(getBlockState(), serverLevel, pos);
    }
}
