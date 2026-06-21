package org.confluence.terraria_boulders.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.events.custom.IEntityInteractable;
import org.confluence.terraria_boulders.init.ModItems;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 巨石手套 —— 右键抓住巨石（实体或方块），抓住后巨石会跟随玩家移动。
 */
public class BoulderGloveItem extends Item implements IEntityInteractable {
    public static String PULLING_BOULDER_ID = "PullingBoulderId";
    public double followRange = 7.0F;//7格
    public double safeDistance = 1.5D;//安全距离，巨石会在这个范围内刹车
    public double pullPower = 0.035;//恒定拉力
    public double limitResistance = 0.4D;//达到危险区域内的拉动阻力
    public double friction = 0.75;//摩擦阻力，每tick削减速度，越大越光滑
    public double maxSpeed = 0.18;//速度上限（低，挪动沉重）

    public BoulderGloveItem(Properties properties) {
        super(properties);
    }

    //右键巨石方块则将巨石实体化
    @Override
    @NonNull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() instanceof BoulderBlock boulder){
            if(level instanceof ServerLevel serverLevel){
                boulder.onRemove(serverLevel, state, pos, context.getPlayer());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;//继续下一步拉动巨石的操作
    }

    //右键空气触发
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    //最长能按多久
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    //长按时物品动作
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult interactEntity(ItemStack stack, Player player, Entity target, InteractionHand hand){

        //手中是否巨石手套
        if (stack.is(ModItems.BOULDER_GLOVE)) {
            //检查点中的是否为巨石类
            if (target instanceof BoulderEntity boulder) {
                Level level = player.level();

                //shift右键会将实体掉落物化
                if(player.isShiftKeyDown()){
                    if(!level.isClientSide()){
                        //获取掉落物
                        Item item = boulder.getBlockState().getBlock().asItem();
                        //没有物品形态
                        if(item == Items.AIR) return InteractionResult.PASS;
                        boulder.onRemove();//删除巨石
                        level.addFreshEntity(new ItemEntity(level, boulder.getX(), boulder.getY(), boulder.getZ(), new ItemStack(item)));//添加掉落物
                    }
                    return InteractionResult.CONSUME;//交互成功
                }
                else{
                    //激活长按
                    player.startUsingItem(hand);
                    if (!level.isClientSide()) {
                        //通过NBT同步数据
                        CompoundTag tag = new CompoundTag();
                        tag.putInt(BoulderGloveItem.PULLING_BOULDER_ID, boulder.getId());
                        //放入CustomData
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    }
                    return InteractionResult.CONSUME;//交互成功
                }
            }
        }
        return InteractionResult.PASS;//其他逻辑继续
    }

    //长按逻辑
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {

            //拿到实体
            if (level.getEntity(this.getBoulderId(stack)) instanceof BoulderEntity boulder) {
                this.connect(boulder);

                //牵引线太长则断开
                if (player.distanceToSqr(boulder) > this.followRange * this.followRange) {
                    this.disconnect(player, stack);
                    return;
                }

                //计算3D距离
                Vec3 toPlayer = player.position().subtract(boulder.position());
                double distance = toPlayer.length();

                //1.5格内刹车，防止惯性把玩家压死
                if (distance < this.safeDistance) {
                    //阻力消除惯性
                    boulder.setDeltaMovement(boulder.getDeltaMovement().multiply(this.limitResistance, this.limitResistance, this.limitResistance));
                } else {
                    //沉重拖拽物理
                    Vec3 pullDir = toPlayer.normalize();
                    Vec3 currentMot = boulder.getDeltaMovement();
                    double newX = currentMot.x * this.friction + pullDir.x * this.pullPower;
                    double newY = currentMot.y * this.friction + pullDir.y * this.pullPower;
                    double newZ = currentMot.z * this.friction + pullDir.z * this.pullPower;

                    Vec3 newVelocity = new Vec3(newX, newY, newZ);

                    //3D 整体限速，防止乱飞
                    if (newVelocity.lengthSqr() > this.maxSpeed * this.maxSpeed) {
                        newVelocity = newVelocity.normalize().scale(this.maxSpeed);
                    }

                    //重新赋予3D速度
                    boulder.setDeltaMovement(newVelocity.x, newVelocity.y, newVelocity.z);
                }

                //高频跨端同步
                if (level instanceof ServerLevel serverLevel) {
                    ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(boulder);
                    serverLevel.getChunkSource().sendToTrackingPlayers(boulder, packet);
                }
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingUseDuration) {
        //松开右键时把石头的拉取状态解除
        if(entity instanceof Player player) this.disconnect(player, stack);
        return super.releaseUsing(stack, level, entity, remainingUseDuration);
    }

    public void connect(BoulderEntity boulder) {
        boulder.pulling = true;
        boulder.stepHeightDenominator = 1.0F;
    }

    public void disconnect(Player player, ItemStack stack) {
        Level level = player.level();
        if (!level.isClientSide()) {
            BoulderEntity boulder = (BoulderEntity) level.getEntity(this.getBoulderId(stack));
            CustomData customData = this.getCustomData(stack);

            //复位状态
            if (boulder != null && boulder.isAlive()) {
                boulder.pulling = false;
                boulder.stepHeightDenominator = 3.0F;
            }

            //停止使用
            player.stopUsingItem();

            //动画防御，向客户端玩家发送更新实体携带物品状态的包，确保客户端的拉弓动画能立刻收回
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
                //或者是给玩家发送一个通用的状态清空包
                serverPlayer.connection.send(new ClientboundEntityEventPacket(player, (byte) 9));
            }

            //数据清理
            if (customData != null && stack != null && !stack.isEmpty()) {
                //断开则清空ID数据
                CompoundTag tag = customData.copyTag();
                //删除巨石ID
                tag.remove(BoulderGloveItem.PULLING_BOULDER_ID);

                if (tag.isEmpty()) {
                    //如果删完组件空了就删掉
                    stack.remove(DataComponents.CUSTOM_DATA);
                } else {
                    //如果里面还有别的数据就写回去
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }
        }


    }

    @Nullable
    public CustomData getCustomData(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_DATA);
    }

    public int getBoulderId(ItemStack stack) {
        CustomData data = getCustomData(stack);
        if (data == null) return 0;
        Optional<Integer> optBoulderId = data.copyTag().getInt(PULLING_BOULDER_ID);
        return optBoulderId.orElse(0);
    }
}
//public class BoulderGloveItem extends Item {
//    // 静态映射：玩家 UUID → 被抓住的巨石实体 UUID
//    private static final Map<UUID, UUID> GRABBED_MAP = new Object2ObjectOpenHashMap<>();
//    // 跟随速度系数
//    private static final double FOLLOW_SPEED = 0.4;
//
//    public BoulderGloveItem(Properties properties) {
//        super(properties);
//    }
//
//    // ========== 方块交互：右键点击巨石方块 ==========
//
//    @Override
//    public InteractionResult useOn(UseOnContext context) {
//        Level level = context.getLevel();
//        if (level.isClientSide()) {
//            return super.useOn(context);
//        }
//
//        BlockPos pos = context.getClickedPos();
//        BlockState state = level.getBlockState(pos);
//
//        if (state.getBlock() instanceof BoulderBlock boulderBlock && context.getPlayer() instanceof ServerPlayer player) {
//            ServerLevel serverLevel = (ServerLevel) level;
//
//            // 如果已经抓着东西，先释放
//            releaseGrabbed(player);
//
//            // 触发方块→实体转换（移除方块，通过 affectNeighborsAfterRemoval 生成实体）
//            boulderBlock.onExecute(state, serverLevel, pos);
//
//            // 在方块位置附近找到刚生成的巨石实体
//            AABB searchBox = new AABB(pos).inflate(0.5);
//            for (BoulderEntity be : serverLevel.getEntitiesOfClass(BoulderEntity.class, searchBox)) {
//                if (be.isAlive()) {
//                    doGrab(player, be);
//                    break;
//                }
//            }
//
//            return InteractionResult.SUCCESS;
//        }
//
//        return super.useOn(context);
//    }
//
//    // ========== 右键空气：释放 ==========
//
//    @Override
//    public InteractionResult use(Level level, Player player, InteractionHand hand) {
//        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
//            UUID uuid = getGrabbedUUID(serverPlayer);
//            if (uuid != null) {
//                ServerLevel serverLevel = serverPlayer.level();
//                Entity grabbed = serverLevel.getEntity(uuid);
//                if (grabbed instanceof BoulderEntity be) {
//                    releaseBoulder(be);
//                }
//                GRABBED_MAP.remove(serverPlayer.getUUID());
//                return InteractionResult.SUCCESS;
//            }
//        }
//        return InteractionResult.PASS;
//    }
//
//    // ========== 每 tick：让被抓住的巨石跟随玩家 ==========
//
//    @Override
//    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
//        if (!(entity instanceof ServerPlayer player)) return;
//
//        // 只检查主手或副手是否拿着该物品
//        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) return;
//
//        UUID boulderUUID = getGrabbedUUID(player);
//        if (boulderUUID == null) return;
//
//        Entity grabbed = level.getEntity(boulderUUID);
//        if (!(grabbed instanceof BoulderEntity be) || !be.isAlive() || be.isRemoved()) {
//            GRABBED_MAP.remove(player.getUUID());
//            return;
//        }
//
//        // 距离太远则释放
//        if (be.distanceToSqr(player) > 64.0) {
//            releaseBoulder(be);
//            GRABBED_MAP.remove(player.getUUID());
//            return;
//        }
//
//        // 目标位置：玩家前方偏右，略高于脚底
//        Vec3 lookVec = player.getLookAngle();
//        Vec3 rightVec = lookVec.yRot((float) (-Math.PI / 2));
//        Vec3 targetPos = player.position()
//                .add(lookVec.scale(-0.5))
//                .add(rightVec.scale(1.2))
//                .add(0, 0.3, 0);
//
//        Vec3 delta = targetPos.subtract(be.position());
//        double distance = delta.length();
//
//        if (distance > 0.1) {
//            Vec3 motion = delta.normalize().scale(Math.min(distance * FOLLOW_SPEED, 1.0));
//            be.setDeltaMovement(motion);
//        } else {
//            be.setDeltaMovement(Vec3.ZERO);
//        }
//    }
//
//    // ========== 公开工具方法 ==========
//
//    /**
//     * 抓住一个巨石实体（由 BoulderEntity.interact 调用）
//     */
//    public static void grabBoulder(Player player, BoulderEntity boulder) {
//        if (!(player instanceof ServerPlayer serverPlayer)) return;
//        releaseGrabbed(serverPlayer);
//        doGrab(serverPlayer, boulder);
//    }
//
//    // ========== 内部方法 ==========
//
//    private static void doGrab(ServerPlayer player, BoulderEntity boulder) {
//        GRABBED_MAP.put(player.getUUID(), boulder.getUUID());
//        // 被抓住时让巨石停止自主滚动
//        boulder.stillTickCount = boulder.maxStillTick;
//    }
//
//    private static void releaseBoulder(BoulderEntity boulder) {
//        if (!boulder.isRemoved()) {
//            boulder.stillTickCount = 0;
//        }
//    }
//
//    private static void releaseGrabbed(ServerPlayer player) {
//        UUID uuid = GRABBED_MAP.remove(player.getUUID());
//        if (uuid != null) {
//            ServerLevel level = player.level();
//            Entity entity = level.getEntity(uuid);
//            if (entity instanceof BoulderEntity be && be.isAlive()) {
//                releaseBoulder(be);
//            }
//        }
//    }
//
//    @Nullable
//    private static UUID getGrabbedUUID(Player player) {
//        return GRABBED_MAP.get(player.getUUID());
//    }
//}
