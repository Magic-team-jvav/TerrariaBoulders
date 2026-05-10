package org.confluence.terraria_boulders.common.entity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class BoulderCannonBlockEntity extends BlockEntity implements Container {
    //private ItemStack cannonAmmo = ItemStack.EMPTY;
//    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
//    public static final EnumProperty<CannonPitch> PITCH = EnumProperty.create("pitch", CannonPitch.class);
//
//    //Pitch有5个档位，向下、斜向下、平射、斜向上、向上
//    public enum CannonPitch implements StringRepresentable {
//        DOWN("down"),
//        DOWN_SLANT("down_slant"),
//        HORIZONTAL("horizontal"),
//        UP_SLANT("up_slant"),
//        UP("up");
//
//        private final String name;
//        CannonPitch(String name) { this.name = name; }
//
//        @Override
//        @NonNull
//        public String getSerializedName() { return this.name; }
//    }
    private final int CAPACITY = 1;
    //存储弹药（容量为1）
    private final NonNullList<ItemStack> cannonAmmo = NonNullList.withSize(this.CAPACITY, ItemStack.EMPTY);
    //当前炮管角度
    public float currentYaw = 0;
    public float currentPitch = 0;
    //目标角度，玩家指向
    public float targetYaw = 0;
    public float targetPitch = 0;
    //旋转速度（per tick）
    private static final float ROTATION_SPEED = 5.0F;
    //遥控数据
    public boolean isAimingMode = false;
    public UUID controllerId = null;
    private int soundTicks = 0;

    public BoulderCannonBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntityTypes.BOULDER_CANNON.get(), worldPosition, blockState);
    }

    //----------Getter and Setter----------

//    public ItemStack getCannonAmmo() {
//        return this.cannonAmmo;
//    }
//
//    /**此方法会自动处理复制ItemStack*/
//    public void setCannonAmmo(ItemStack cannonAmmo) {
//        this.cannonAmmo = cannonAmmo.copy();
//        this.cannonAmmo.setCount(1);
//        this.setChanged();
//    }

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

    //设置瞄准状态
    public void setAimingMode(boolean aiming, UUID controllerId) {
        if (aiming) {
            this.isAimingMode = true;
            this.controllerId = controllerId;
        } else{
            this.isAimingMode = false;
            this.controllerId = null;
        }
    }

    //智能翻转状态
    public void setAimingMode(UUID controllerId) {
        if(this.isAimingMode){
            setAimingMode(false, null);
        } else{
            setAimingMode(true, controllerId);
        }
    }

    //调节炮口高度
//    public void nextPitch(Level level, BlockPos pos, BlockState state){
//
//        //获取当前档位
//        CannonPitch currentPitch = state.getValue(PITCH);
//
//        //计算下一个档位 (循环切换)
//        CannonPitch[] values = CannonPitch.values();
//        int nextIndex = (currentPitch.ordinal() + 1) % values.length;
//        CannonPitch nextPitch = values[nextIndex];
//
//        //状态更新
//        level.setBlock(pos, state.setValue(PITCH, nextPitch), 3);
//
//        //音效
//        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.2F + (nextIndex * 0.1F));
//        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
//    }

    public static void tick(Level level, BlockPos pos, BlockState state, BoulderCannonBlockEntity be) {
        //角度没到位时执行移动和同步
        float yawDelta = Mth.degreesDifference(be.currentYaw, be.targetYaw);
        float pitchDelta = be.targetPitch - be.currentPitch; // Pitch 不需要处理 360 度环绕

        if (Math.abs(yawDelta) > 0.05f || Math.abs(pitchDelta) > 0.05f) {
            be.currentYaw = wrapAndMove(be.currentYaw, be.targetYaw, ROTATION_SPEED);
            be.currentPitch = wrapAndMove(be.currentPitch, be.targetPitch, ROTATION_SPEED);

            be.soundTicks++;
            //5tick检查一次
            if (be.soundTicks % 5 == 0) {
                level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.3F, 1.2F + level.getRandom().nextFloat() * 0.2F);//略微随机化音调
            }

            be.setChanged();
            if (!level.isClientSide()) {
                //角度变化时才发包
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        //处理调节模式逻辑
//        if (be.isAimingMode && be.controllerId != null) {
//            Player player = level.getPlayerByUUID(be.controllerId);
//
//            //防止玩家下线、死亡或跑得太远导致大炮卡死
//            if (player == null || !player.isAlive() || player.distanceToSqr(pos.getCenter()) > 256) {
//                be.setAimingMode(null);
//                be.setChanged();
//                if (!level.isClientSide()) {
//                    level.sendBlockUpdated(pos, state, state, 3);
//                }
//            }
//            else if (player.isShiftKeyDown()) {
//                be.setAimingMode(null);
//                be.setChanged();
//                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
//                    //锁定音效
//                    serverLevel.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.5F);
//                    level.sendBlockUpdated(pos, state, state, 3);
//                }
//            }
//            else {
//                //获取玩家当前的视线角度并设为大炮的目标角度
//                be.targetYaw = player.getYRot();
//                be.targetPitch = player.getXRot();
//
//                //炮口激光粒子
//                if (level instanceof ServerLevel serverLevel) {
//                    //计算当前炮口朝向向量
//                    double yawRad = Math.toRadians(be.currentYaw);
//                    double pitchRad = Math.toRadians(be.currentPitch);
//                    double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
//                    double dy = -Math.sin(pitchRad);
//                    double dz = Math.cos(yawRad) * Math.cos(pitchRad);
//
//                    //计算炮口的精确三维坐标假设炮管长度为1.2格
//                    double muzzleX = pos.getX() + 0.5D + dx * 1.2D;
//                    double muzzleY = pos.getY() + 0.5D + dy * 1.2D;
//                    double muzzleZ = pos.getZ() + 0.5D + dz * 1.2D;
//
//                    //使用红石粒子绘制一个红色的瞄准点
//                    serverLevel.sendParticles(
//                            new DustParticleOptions(0XFF0000, 0.8F),
//                            muzzleX, muzzleY, muzzleZ,
//                            1, 0, 0, 0, 0
//                    );
//
//                    //长激光线
//                    for(int i = 1; i <= 5; i++) {
//                        serverLevel.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D + dx * i, pos.getY() + 0.5D + dy * i, pos.getZ() + 0.5D + dz * i, 1, 0, 0, 0, 0);
//                    }
//                }
//            }
//        }
//
//        //平滑移动逻辑让当前角度以一定速度靠近目标角度
//        if (be.currentYaw != be.targetYaw || be.currentPitch != be.targetPitch) {
//            float speed = 4.0F; //大炮的旋转速度每tick转多少度
//            be.currentYaw = wrapAndMove(be.currentYaw, be.targetYaw, speed);
//            be.currentPitch = wrapAndMove(be.currentPitch, be.targetPitch, speed);
//
//            //当角度正在发生变化时标记更新通知客户端重绘模型
//            be.setChanged();
//            if (!level.isClientSide()) {
//                level.sendBlockUpdated(pos, state, state, 3);
//            }
//        }
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
//        if (this.cannonAmmo != ItemStack.EMPTY) {
//            output.storeNullable("CannonAmmo", ItemStack.CODEC, this.cannonAmmo);
//        }
        //保存物品数据
        ContainerHelper.saveAllItems(output, this.cannonAmmo);

        //保存遥控数据
//        output.putBoolean("IsAimingMode", this.isAimingMode);
//        if (this.controllerId != null) {
//            output.putUUID("ControllerId", this.controllerId);
//        }
        output.putFloat("CurrentYaw", this.currentYaw);
        output.putFloat("CurrentPitch", this.currentPitch);
        output.putFloat("TargetYaw", this.targetYaw);
        output.putFloat("TargetPitch", this.targetPitch);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
//        ItemStack savedAmmo = input.read("CannonAmmo", ItemStack.CODEC).orElse(ItemStack.EMPTY);
//        if (savedAmmo != ItemStack.EMPTY) {
//            this.setCannonAmmo(savedAmmo);
//        }
        //读取物品数据
        this.cannonAmmo.clear();
        ContainerHelper.loadAllItems(input, this.cannonAmmo);

        //读取遥控数据
//        this.isAimingMode = tag.getBoolean("IsAimingMode");
//        if (tag.hasUUID("ControllerId")) {
//            this.controllerId = tag.getUUID("ControllerId");
//        }

        //读取目标值
        this.targetYaw = input.getFloatOr("TargetYaw", 0);
        this.targetPitch = input.getFloatOr("TargetPitch", 0);

        //当current是初始值 0 的时候才去同步
        if (this.currentYaw == 0) {
            this.currentYaw = input.getFloatOr("CurrentYaw", this.targetYaw);
            this.currentPitch = input.getFloatOr("CurrentPitch", this.targetPitch);
        }
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
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof BoulderBlock;
    }

    @Override
    public void clearContent() {
        this.cannonAmmo.clear();
        this.setChanged();
    }
}
