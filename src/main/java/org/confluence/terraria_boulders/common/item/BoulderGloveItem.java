package org.confluence.terraria_boulders.common.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * 巨石手套 —— 右键抓住巨石（实体或方块），抓住后巨石会跟随玩家移动。
 */
public class BoulderGloveItem extends Item {
    // 静态映射：玩家 UUID → 被抓住的巨石实体 UUID
    private static final Map<UUID, UUID> GRABBED_MAP = new Object2ObjectOpenHashMap<>();
    // 跟随速度系数
    private static final double FOLLOW_SPEED = 0.4;

    public BoulderGloveItem(Properties properties) {
        super(properties);
    }

    // ========== 方块交互：右键点击巨石方块 ==========

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return super.useOn(context);
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof BoulderBlock boulderBlock && context.getPlayer() instanceof ServerPlayer player) {
            ServerLevel serverLevel = (ServerLevel) level;

            // 如果已经抓着东西，先释放
            releaseGrabbed(player);

            // 触发方块→实体转换（移除方块，通过 affectNeighborsAfterRemoval 生成实体）
            boulderBlock.onExecute(state, serverLevel, pos);

            // 在方块位置附近找到刚生成的巨石实体
            AABB searchBox = new AABB(pos).inflate(0.5);
            for (BoulderEntity be : serverLevel.getEntitiesOfClass(BoulderEntity.class, searchBox)) {
                if (be.isAlive()) {
                    doGrab(player, be);
                    break;
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    // ========== 右键空气：释放 ==========

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            UUID uuid = getGrabbedUUID(serverPlayer);
            if (uuid != null) {
                ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
                Entity grabbed = serverLevel.getEntity(uuid);
                if (grabbed instanceof BoulderEntity be) {
                    releaseBoulder(be);
                }
                GRABBED_MAP.remove(serverPlayer.getUUID());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    // ========== 每 tick：让被抓住的巨石跟随玩家 ==========

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof ServerPlayer player)) return;

        // 只检查主手或副手是否拿着该物品
        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) return;

        UUID boulderUUID = getGrabbedUUID(player);
        if (boulderUUID == null) return;

        Entity grabbed = level.getEntity(boulderUUID);
        if (!(grabbed instanceof BoulderEntity be) || !be.isAlive() || be.isRemoved()) {
            GRABBED_MAP.remove(player.getUUID());
            return;
        }

        // 距离太远则释放
        if (be.distanceToSqr(player) > 64.0) {
            releaseBoulder(be);
            GRABBED_MAP.remove(player.getUUID());
            return;
        }

        // 目标位置：玩家前方偏右，略高于脚底
        Vec3 lookVec = player.getLookAngle();
        Vec3 rightVec = lookVec.yRot((float) (-Math.PI / 2));
        Vec3 targetPos = player.position()
                .add(lookVec.scale(-0.5))
                .add(rightVec.scale(1.2))
                .add(0, 0.3, 0);

        Vec3 delta = targetPos.subtract(be.position());
        double distance = delta.length();

        if (distance > 0.1) {
            Vec3 motion = delta.normalize().scale(Math.min(distance * FOLLOW_SPEED, 1.0));
            be.setDeltaMovement(motion);
        } else {
            be.setDeltaMovement(Vec3.ZERO);
        }
    }

    // ========== 公开工具方法 ==========

    /**
     * 抓住一个巨石实体（由 BoulderEntity.interact 调用）
     */
    public static void grabBoulder(Player player, BoulderEntity boulder) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        releaseGrabbed(serverPlayer);
        doGrab(serverPlayer, boulder);
    }

    // ========== 内部方法 ==========

    private static void doGrab(ServerPlayer player, BoulderEntity boulder) {
        GRABBED_MAP.put(player.getUUID(), boulder.getUUID());
        // 被抓住时让巨石停止自主滚动
        boulder.stillTickCount = boulder.maxStillTick;
    }

    private static void releaseBoulder(BoulderEntity boulder) {
        if (!boulder.isRemoved()) {
            boulder.stillTickCount = 0;
        }
    }

    private static void releaseGrabbed(ServerPlayer player) {
        UUID uuid = GRABBED_MAP.remove(player.getUUID());
        if (uuid != null) {
            ServerLevel level = (ServerLevel) player.level();
            Entity entity = level.getEntity(uuid);
            if (entity instanceof BoulderEntity be && be.isAlive()) {
                releaseBoulder(be);
            }
        }
    }

    @Nullable
    private static UUID getGrabbedUUID(Player player) {
        return GRABBED_MAP.get(player.getUUID());
    }
}
