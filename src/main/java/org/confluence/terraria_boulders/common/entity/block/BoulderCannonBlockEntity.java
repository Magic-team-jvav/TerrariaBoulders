package org.confluence.terraria_boulders.common.entity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.jspecify.annotations.NonNull;

public class BoulderCannonBlockEntity extends BlockEntity implements Container {
    private final int CAPACITY = 1;
    //存储弹药（容量为1）
    private final NonNullList<ItemStack> cannonAmmo = NonNullList.withSize(this.CAPACITY, ItemStack.EMPTY);
    //当前炮管角度
    private float currentYaw = 0;
    public float currentYawO = 0;
    private float currentPitch = 0;
    public float currentPitchO = 0;
    //目标角度，玩家指向
    public float targetYaw = 0;
    public float targetPitch = 0;
    //旋转速度（per tick）
    private static final float ROTATION_SPEED = 5.0F;
    //仰角限制
    public static final float MIN_PITCH = -45.0F;
    public static final float MAX_PITCH = 10.0F;
    //遥控数据
    //public boolean isAimingMode = false;
    //public UUID controllerId = null;
    private int soundTicks = 0;

    public BoulderCannonBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntityTypes.BOULDER_CANNON.get(), worldPosition, blockState);
    }

    //----------Getter and Setter----------

    public ItemStack getCannonAmmo() {
        return this.cannonAmmo.getFirst();
    }

    /**此方法会自动处理复制ItemStack*/
    public void setCannonAmmo(ItemStack cannonAmmo) {
        ItemStack stack = cannonAmmo.copy();
        stack.setCount(1);
        this.cannonAmmo.set(0, stack);
        this.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BoulderCannonBlockEntity be) {
        be.currentYawO = be.getCurrentYaw();
        be.currentPitchO = be.getCurrentPitch();

        //角度没到位时执行移动和同步
        float yawDelta = Mth.degreesDifference(be.getCurrentYaw(), be.targetYaw);
        float pitchDelta = be.targetPitch - be.getCurrentPitch(); // Pitch 不需要处理 360 度环绕

        if (Math.abs(yawDelta) > 0.05f || Math.abs(pitchDelta) > 0.05f) {
            be.setCurrentYaw(wrapAndMove(be.getCurrentYaw(), be.targetYaw, ROTATION_SPEED));
            be.setCurrentPitch(wrapAndMove(be.getCurrentPitch(), be.targetPitch, ROTATION_SPEED));

            be.soundTicks++;
            //5tick检查一次
            if (!level.isClientSide() && be.soundTicks % 5 == 0) {
                level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.3F, 1.2F + level.getRandom().nextFloat() * 0.2F);//略微随机化音调
            }

            be.setChanged();
//            if (!level.isClientSide()) {
//                //角度变化时才发包
//                level.sendBlockUpdated(pos, state, state, 3);
//            }
        }
    }

    //辅助方法处理角度的平滑逼近并解决360度跨界问题
    private static float wrapAndMove(float current, float target, float speed) {
        //float delta = Mth.wrapDegrees(target - current);
        float delta = Mth.degreesDifference(current, target);
        if (Math.abs(delta) < speed) return target;
        //return current + Math.signum(delta) * speed;
        return Mth.wrapDegrees(current + Mth.clamp(delta, -speed, speed));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        //保存物品数据
        ContainerHelper.saveAllItems(output, this.cannonAmmo);
        output.putFloat("CurrentYaw", this.getCurrentYaw());
        output.putFloat("CurrentPitch", this.getCurrentPitch());
        output.putFloat("TargetYaw", this.targetYaw);
        output.putFloat("TargetPitch", this.targetPitch);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        //读取物品数据
        this.cannonAmmo.clear();
        ContainerHelper.loadAllItems(input, this.cannonAmmo);
        //读取目标值
        //this.targetYaw = input.getFloatOr("TargetYaw", 0);
        //this.setTargetPitch(input.getFloatOr("TargetPitch", 0));
        this.setTarget(input.getFloatOr("TargetYaw", 0), input.getFloatOr("TargetPitch", 0));

        //当current是初始值0的时候才去同步
        if (this.getCurrentYaw() == 0) {
            this.setCurrentYaw(input.getFloatOr("CurrentYaw", this.targetYaw));
            this.setCurrentPitch(input.getFloatOr("CurrentPitch", this.targetPitch));
        }
    }

    //获取更新标签
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        //存进去发给客户端
//        tag.putFloat("CurrentYaw", this.currentYaw);
//        tag.putFloat("CurrentPitch", this.currentPitch);
        tag.putFloat("TargetYaw", this.targetYaw);
        tag.putFloat("TargetPitch", this.targetPitch);
        return tag;
    }

    //获取更新数据包
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getContainerSize() {
        return this.cannonAmmo.size();
    }

    @Override
    public boolean isEmpty() {
        return this.cannonAmmo.getFirst().isEmpty();
    }

    @Override
    @NonNull
    public ItemStack getItem(int slot) {
        return this.cannonAmmo.get(slot);
    }

    @Override
    @NonNull
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.cannonAmmo, slot, amount);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }

    @Override
    @NonNull
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.cannonAmmo, slot);
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        this.cannonAmmo.set(slot, stack);
        //塞进来的数量超过限制，强制修正
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();

        //物品变动时同步给客户端
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int getMaxStackSize() {
        return this.CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;//没有GUI
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        //只有巨石才允许进入
        boolean isBoulder = stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;

        //检查是否已满
        boolean isSlotEmpty = this.getItem(slot).isEmpty();

        return isBoulder && isSlotEmpty;
    }

    @Override
    public void clearContent() {
        this.cannonAmmo.clear();
        this.setChanged();
    }

    public float getCurrentYaw() {
        return currentYaw;
    }

    public void setCurrentYaw(float currentYaw) {
        this.currentYaw = currentYaw;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }

    public void setCurrentPitch(float currentPitch) {
        this.currentPitch = Mth.clamp(currentPitch, MIN_PITCH, MAX_PITCH);
    }

//    public void setTarget(float newTargetYaw, float newTargetPitch){
//        this.targetYaw = newTargetYaw;
//        this.targetPitch = newTargetPitch;
//        this.setChanged();
//        //只有收到新指令才给客户端发包
//        if (this.level != null && !this.level.isClientSide()) {
//            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
//        }
//    }

    public void setTarget(float newTargetYaw, float newTargetPitch) {
        //将目标角度也限制在合法区间内，防止产生永远达不到的终点
        newTargetPitch = Mth.clamp(newTargetPitch, MIN_PITCH, MAX_PITCH);

        //只有当玩家视角变化超过1度时，才更新目标并发送数据包
        if (Math.abs(Mth.degreesDifference(this.targetYaw, newTargetYaw)) > 1.0f || Math.abs(this.targetPitch - newTargetPitch) > 1.0f) {
            this.targetYaw = newTargetYaw;
            this.targetPitch = newTargetPitch;
            this.setChanged();

            //发包
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

//    public void setTargetPitch(float targetPitch) {
//        this.targetPitch = Mth.clamp(targetPitch, -45, 10);
//    }
}
