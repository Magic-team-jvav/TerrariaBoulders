package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderEntity;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CamouflagedBoulderBlock extends FullCollisionBoulderBlock implements EntityBlock {
    //public static final Supplier<BlockState> DEFAULT_CAMOUFLAGE = () -> ModBlocks.CAMOUFLAGED_BOULDER.get().defaultBlockState();
    public static final Supplier<BlockState> DEFAULT_CAMOUFLAGE = Blocks.STONE::defaultBlockState;
    private BlockState tempMimic = Blocks.STONE.defaultBlockState();

    public CamouflagedBoulderBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public void onExecute(BlockState state, ServerLevel level, BlockPos pos) {
        saveMimic(state, level, pos);
        //summon(level, pos, state, entity -> level.getNearestPlayer(entity, BoulderEntity.SEARCH_RANGE));
        super.onExecute(state, level, pos);
    }

    public void saveMimic(BlockState state, ServerLevel level, BlockPos pos) {
        //在父类移除方块前先把数据存入临时变量
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            this.tempMimic = be.getMimicState();
        } else {
            this.tempMimic = DEFAULT_CAMOUFLAGE.get();
        }
    }

    @Override
    @NonNull
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            saveMimic(state, serverLevel, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit) {
        onExecute(state, level, pos);
        super.onExplosionHit(state, level, pos, explosion, onHit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new CamouflagedBoulderBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends BoulderEntity> void summon(Level level, BlockPos pos, BlockState blockState, Function<T, Player> function) {
        if (level.isClientSide()) return;
        //数据
//        BlockState savedMimic = Blocks.STONE.defaultBlockState();
//        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
//            savedMimic = be.getMimicState();
//        }

        //创建实体
        CamouflagedBoulderEntity entity = new CamouflagedBoulderEntity(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), level);
        entity.moveOrInterpolateTo(pos.getBottomCenter());
        //把抓取的数据塞给实体
        entity.setMimicState(tempMimic);
        //设置目标并生成
        if (!level.getBlockState(pos.below()).isAir()) {
            entity.targetTo(function.apply((T) entity));
        }
        level.addFreshEntity(entity);
    }

    //中键拾取
    @Override
    @NonNull
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(this.asItem());//拿到巨石物品
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimic = be.getMimicState();
            if (mimic != null && !mimic.isAir()) {
                //把伪装数据塞进物品的Component里
                stack.set(ModDataComponents.MIMIC_STATE.get(), mimic);
            }
        }
        return stack;
    }

    //放置方块
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimic = stack.get(ModDataComponents.MIMIC_STATE.get());
            if (mimic != null) {
                be.setMimicState(mimic);
            }
        }
    }

    public void updateAndChangeState(Level level, BlockPos pos, BlockState state, CamouflagedBoulderBlockEntity be) {
        //标记需要保存到硬盘
        be.setChanged();

        if (!level.isClientSide()) {
            //通知客户端方块数据已更新
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    //没涂蜜脾的情况下空手shift+右键解除伪装
    @Override
    @NonNull
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity cbbe) {
            //是否满足交互条件
            BlockState currentMimic = cbbe.getMimicState();
            BlockState defaultMimic = DEFAULT_CAMOUFLAGE.get();
            if (!cbbe.isLocked() && player.isShiftKeyDown() && !currentMimic.equals(defaultMimic)) {
                if (!level.isClientSide()) {
                    cbbe.setMimicState(defaultMimic);
                    level.levelEvent(null, 2001, pos, Block.getId(currentMimic));
                    level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                updateAndChangeState(level, pos, state, cbbe);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    //用斧右键去除蜜脾，解锁锁定状态
    //未锁定状态下手持方块shift+右键进行伪装
    @Override
    @NonNull
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be){
            Item item = stack.getItem();
            BlockState currentMimic = be.getMimicState();

            //伪装逻辑
            if(player.isShiftKeyDown() && item instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof CamouflagedBoulderBlock)){
                BlockState nextMimic = blockItem.getBlock().defaultBlockState();
                if(!currentMimic.equals(nextMimic) && !be.isLocked()){
                    if(!level.isClientSide()){
                        be.setMimicState(blockItem.getBlock().defaultBlockState());
                        level.levelEvent(null, 2001, pos, Block.getId(state));
                        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
                        updateAndChangeState(level, pos, state, be);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            //涂蜡逻辑
            if(stack.is(Items.HONEYCOMB) && !be.isLocked()){
                if(!level.isClientSide()){
                    be.setLocked(true);
                    if(!player.isCreative()){
                        stack.shrink(1);//消耗
                    }
                    //音效
                    level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(null, 3003, pos, 0);//粒子效果
                    updateAndChangeState(level, pos, state, be);
                }
                return InteractionResult.SUCCESS;
            }

            //去蜡逻辑
            if(item instanceof AxeItem && be.isLocked()){
                //只有在服务端确实锁定的状态下，才执行去蜡操作
                if(!level.isClientSide()){
                    be.setLocked(false);
                    level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);//刮蜡音效
                    level.levelEvent(null, 3004, pos, 0);//斧头除蜡音效
                    stack.hurtAndBreak(1, (ServerLevel) level, player instanceof ServerPlayer sp ? sp : null, item2 -> {});
                    updateAndChangeState(level, pos, state, be);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
