package org.confluence.terraria_boulders.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.confluence.terraria_boulders.common.entity.CannonSeatEntity;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderEntity;
import org.confluence.terraria_boulders.common.item.CamouflagedBoulderItem;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BoulderCannonBlock extends Block implements EntityBlock {
    //红石信号
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    //弹药到实体类型的映射表
    private static final Map<Item, EntityType<? extends BoulderEntity>> AMMO_MAP = new HashMap<>();

    public BoulderCannonBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    //由于Block并不关联EntityType，所以我不得不使用一个表...
    //以后的巨石必须手动加进去
    public static void initAmmoMap() {
        if (!AMMO_MAP.isEmpty()) return;//防止重复加载
        AMMO_MAP.put(ModBlocks.FOLLOWER_BOULDER.get().asItem(), ModEntityTypes.FOLLOWER_BOULDER.get());
        AMMO_MAP.put(ModBlocks.EXPLODE_BOULDER.get().asItem(), ModEntityTypes.EXPLODE_BOULDER.get());
        AMMO_MAP.put(ModBlocks.ROLLING_CACTUS_BOULDER.get().asItem(), ModEntityTypes.ROLLING_CACTUS_BOULDER.get());
        AMMO_MAP.put(ModBlocks.BOUNCY_BOULDER.get().asItem(), ModEntityTypes.BOUNCY_BOULDER.get());
        AMMO_MAP.put(ModBlocks.GHOULDER.get().asItem(), ModEntityTypes.GHOULDER.get());
        AMMO_MAP.put(ModBlocks.LAVA_BOULDER.get().asItem(), ModEntityTypes.LAVA_BOULDER.get());
        AMMO_MAP.put(ModBlocks.SPIDER_BOULDER.get().asItem(), ModEntityTypes.SPIDER_BOULDER.get());
        AMMO_MAP.put(ModBlocks.RAINBOW_BOULDER.get().asItem(), ModEntityTypes.RAINBOW_BOULDER.get());
        AMMO_MAP.put(ModBlocks.CAMOUFLAGED_BOULDER.get().asItem(), ModEntityTypes.CAMOUFLAGED_BOULDER.get());
    }

    //查表
    private EntityType<? extends BoulderEntity> getEntityTypeFromAmmo(Item item) {
        return AMMO_MAP.getOrDefault(item, ModEntityTypes.BOULDER.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new BoulderCannonBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (placer != null && level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be) {
            //获取放置者的当前视角
            float initialYaw = Mth.wrapDegrees(placer.getYRot());
            //float initialPitch = placer.getXRot();//放置时的俯仰角

            float initialPitch = 0f;//默认平射

            be.currentYaw = initialYaw;
            be.targetYaw = initialYaw;
            be.currentPitch = initialPitch;
            be.targetPitch = initialPitch;

            //标记更新，确保发给客户端
            be.setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;

        //是否有临近的红石信号
        boolean hasSignal = level.hasNeighborSignal(pos);

        //信号状态发生改变时执行逻辑
        if (hasSignal != state.getValue(POWERED)) {
            if (hasSignal) {
                //触发发射逻辑
                if (level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be) {
                    //从大炮的 BlockEntity 中获取当前装填的弹药
                    ItemStack ammo = be.getItem(0);

                    //确保弹药不为空，并且是个方块物品
                    if (!ammo.isEmpty() && ammo.getItem() instanceof BlockItem blockItem) {
                        //判断弹药是不是巨石
                        if (blockItem.getBlock() instanceof BoulderBlock boulderBlock) {
                            this.fire(level, pos, state, boulderBlock, blockItem);
                        }
                    }
                }
            }
            //更新方块状态记录，防止重复激活
            level.setBlock(pos, state.setValue(POWERED, hasSignal), 3);
        }
    }

    @Override
    public BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        //放下时检测周围信号，并设置初始POWERED
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    @NonNull
    public RenderShape getRenderShape(BlockState state) {
        //取消json渲染
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //检查当前方块实体的是否为大炮
        if (type == ModBlockEntityTypes.BOULDER_CANNON.get()) {
            return (lvl, pos, st, be) -> BoulderCannonBlockEntity.tick(lvl, pos, st, (BoulderCannonBlockEntity) be);
        }
        //如果类型对不上不需要
        return null;
    }

    @Override
    @NonNull
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult){
        if(level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be) {
            //调节角度
            if (stack.isEmpty() && !player.isShiftKeyDown()) {
                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                    //当前没有别的座位
                    List<CannonSeatEntity> existingSeats = level.getEntitiesOfClass(CannonSeatEntity.class, new net.minecraft.world.phys.AABB(pos));

                    if (existingSeats.isEmpty()) {
                        //生成大炮正中心的座位
                        CannonSeatEntity seat = ModEntityTypes.CANNON_SEAT.get().create(level, EntitySpawnReason.TRIGGERED);
                        if (seat != null) {
                            seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D);
                            serverLevel.addFreshEntity(seat);

                            //骑上
                            player.startRiding(seat);
                            serverLevel.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.3F);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
//            if(player.isShiftKeyDown()){
//                //调节模式
//                be.setAimingMode(player.getUUID());
//                if(!level.isClientSide() && level instanceof ServerLevel serverLevel){
//                    serverLevel.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.5F);
//                    level.sendBlockUpdated(pos, state, state, 3);
//                }
//                return InteractionResult.SUCCESS;
//            }
            else{
                //be.nextPitch(level, pos, state);
                Vec3 center = Vec3.atCenterOf(pos);
                //未装填状态
                if(be.getCannonAmmo().isEmpty()) {
                    if(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof BoulderBlock boulderBlock) {
                        if(!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                            be.setCannonAmmo(stack);
                            if(!player.isCreative()) stack.shrink(1);
                            serverLevel.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
                            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.5, center.z, 15, 0.2, 0.2, 0.2, 0.1);
                            serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 10, 0.1, 0.1, 0.1, 0.05);
                        }
                        return InteractionResult.SUCCESS;
                    }
                } else{//装填状态
                    if(!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                        serverLevel.addFreshEntity(new ItemEntity(level, center.x, center.y + 0.5D, center.z, be.getCannonAmmo()));
                        be.setCannonAmmo(ItemStack.EMPTY);
                        serverLevel.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        //level.playSound(null, pos, SoundEvents.DECORATED_POT_SHATTER, SoundSource.BLOCKS, 1.0F, 0.8F);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

//    @Override
//    @NonNull
//    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult){
//        return InteractionResult.PASS;
//    }

    //发射
    public void fire(Level level, BlockPos pos, BlockState state, BoulderBlock boulderBlock, BlockItem blockItem){
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be){
            ItemStack ammo = be.getCannonAmmo();
            if(ammo.isEmpty()) return;

            //获取朝向向量
            Vec3 lookVec = Vec3.directionFromRotation(be.currentPitch, be.currentYaw);

            //调整生成位置（大炮中心坐标 + 朝向向量 * 偏移距离（0.7格））
            Vec3 spawnPos = pos.getCenter().add(lookVec.scale(0.7D));

            //直接从方块获取对应的实体类型进行生成
            EntityType<? extends BoulderEntity> entityType = this.getEntityTypeFromAmmo(ammo.getItem());
            BoulderEntity entity = entityType.create(level, EntitySpawnReason.TRIGGERED);

            //防止实体类型未注册
            if(entity == null) return;

            //应用位置
            entity.setPos(spawnPos);

            //保留方块信息
            Item ammoItem = ammo.getItem();

            //一般巨石
            if (ammoItem instanceof BlockItem ammoBlockItem) {
                entity.setBlockState(ammoBlockItem.getBlock().defaultBlockState());
            }

            //伪装巨石
            if (ammoItem instanceof CamouflagedBoulderItem && entity instanceof CamouflagedBoulderEntity cbEntity) {
                //属性状态
                BlockState mimicState = ammo.getOrDefault(ModDataComponents.MIMIC_STATE.get(), CamouflagedBoulderBlock.DEFAULT_CAMOUFLAGE.get());
                //同时读取打蜡锁定状态
                //boolean isLocked = ammo.getOrDefault(ModDataComponents.IS_LOCKED.get(), false);

                //赋予实体
                cbEntity.setMimicState(mimicState);
                //cbEntity.setMimicLocked(isLocked);
            }

            //初速度
            double speed = 2D;
            //Y轴加点抛物线，防止贴地滑行
            entity.setDeltaMovement(lookVec.scale(speed));

            level.addFreshEntity(entity);

            //清空弹药
            be.setCannonAmmo(ItemStack.EMPTY);
            be.setChanged();

            //音效
            level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 0.8F);

            //炮口生成烟雾粒子
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    15,//粒子数量
                    0.2, 0.2, 0.2,//扩散范围
                    0.05//粒子速度
            );
        }
    }
}
