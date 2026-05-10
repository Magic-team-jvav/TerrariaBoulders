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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.confluence.terraria_boulders.common.entity.CannonSeatEntity;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.common.item.CamouflagedBoulderItem;
import org.confluence.terraria_boulders.init.ModBlockEntityTypes;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class BoulderCannonBlock extends Block implements EntityBlock {
    //红石信号
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BoulderCannonBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.METAL)
                .strength(3.5F, 3.5F)
                //.requiresCorrectToolForDrops()
                .noCollision()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
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
    @NonNull
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (placer == null || !(level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be)) {
            return;
        }

        //获取放置者的当前视角
        float initialYaw = Mth.wrapDegrees(placer.getYRot());
        //float initialPitch = placer.getXRot();//放置时的俯仰角
        float initialPitch = 0f;//默认平射

        be.setCurrentYaw(initialYaw);
        be.targetYaw = initialYaw;
        be.setCurrentPitch(initialPitch);
        be.setTargetPitch(initialPitch);

        //标记更新，确保发给客户端
        be.setChanged();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;

        //是否有临近的红石信号
        boolean hasSignal = level.hasNeighborSignal(pos);

        //信号状态发生改变时执行逻辑
        if (hasSignal == state.getValue(POWERED)) {
            return;
        }

        launch(state, level, pos, hasSignal);

        //更新方块状态记录，防止重复激活
        level.setBlock(pos, state.setValue(POWERED, hasSignal), 3);
    }

    protected void launch(BlockState state, Level level, BlockPos pos, boolean hasSignal) {
        if (!hasSignal) {
            return;
        }

        //触发发射逻辑
        if (!(level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be)) {
            return;
        }

        //从大炮的 BlockEntity 中获取当前装填的弹药
        ItemStack itemStack = be.getItem(0);
        Item item = itemStack.getItem();

        //确保弹药不为空，并且是个方块物品
        if (itemStack.isEmpty() || !(item instanceof BlockItem blockItem)) {
            return;
        }

        //判断弹药是不是巨石
        if (!(blockItem.getBlock() instanceof BoulderBlock boulderBlock)) {
            return;
        }

        this.fire(level, pos, itemStack, blockItem, state, boulderBlock);
    }

    @Override
    public BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        //放下时检测周围信号，并设置初始POWERED
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

//    @Override
//    @NonNull
//    public RenderShape getRenderShape(BlockState state) {
//        //取消json渲染
//        return RenderShape.MODEL;
//    }

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
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be)) {
            return InteractionResult.PASS;
        }
        //调节角度
        if (stack.isEmpty() && !player.isShiftKeyDown()) {
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS;
            }
            //当前没有别的座位
            List<CannonSeatEntity> existingSeats = level.getEntitiesOfClass(CannonSeatEntity.class, new net.minecraft.world.phys.AABB(pos));

            if (!existingSeats.isEmpty()) {
                return InteractionResult.SUCCESS;
            }

            //生成大炮正中心的座位
            CannonSeatEntity seat = ModEntityTypes.CANNON_SEAT.get().create(level, EntitySpawnReason.TRIGGERED);
            if (seat == null) {
                return InteractionResult.SUCCESS;
            }

            seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D);
            serverLevel.addFreshEntity(seat);

            //骑上
            player.startRiding(seat);
            serverLevel.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.3F);
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

        //be.nextPitch(level, pos, state);

        Vec3 center = Vec3.atCenterOf(pos);
        if (!be.getCannonAmmo().isEmpty()) {
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS;
            }
            //装填状态
            serverLevel.addFreshEntity(new ItemEntity(level, center.x, center.y + 0.5D, center.z, be.getCannonAmmo()));
            be.setCannonAmmo(ItemStack.EMPTY);
            serverLevel.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
            //level.playSound(null, pos, SoundEvents.DECORATED_POT_SHATTER, SoundSource.BLOCKS, 1.0F, 0.8F);
            return InteractionResult.SUCCESS;
        }

        //未装填状态
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof BoulderBlock boulderBlock)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        be.setCannonAmmo(stack);
        if (!player.isCreative()) stack.shrink(1);
        serverLevel.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.5, center.z, 15, 0.2, 0.2, 0.2, 0.1);
        serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 10, 0.1, 0.1, 0.1, 0.05);
        return InteractionResult.SUCCESS;
    }

//    @Override
//    @NonNull
//    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult){
//        return InteractionResult.PASS;
//    }

    /**
     * 具体的发射方法
     */
    protected void fire(Level level, BlockPos pos, ItemStack boulderItemStack, BlockItem boulderBlockItem, BlockState blockState, BoulderBlock boulderBlock) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof BoulderCannonBlockEntity be)) {
            return;
        }

        BlockState mimicState = boulderBlock.defaultBlockState();

        //伪装巨石
        if (boulderBlockItem instanceof CamouflagedBoulderItem) {
            mimicState = boulderItemStack.getOrDefault(ModDataComponents.MIMIC_STATE.get(), CamouflagedBoulderBlock.DEFAULT_CAMOUFLAGE.get());
        }

        //获取朝向向量
        Vec3 lookVec = Vec3.directionFromRotation(be.getCurrentPitch(), be.getCurrentYaw());

        //调整生成位置（大炮中心坐标 + 朝向向量 * 偏移距离（0.7格））
        Vec3 spawnPos = pos.getCenter().add(lookVec.scale(0.7D));

        BoulderEntity entity = boulderBlock.createBoulderEntity(level, spawnPos, mimicState);

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
