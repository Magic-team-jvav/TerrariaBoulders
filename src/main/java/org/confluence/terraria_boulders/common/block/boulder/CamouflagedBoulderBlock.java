package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.common.Tags;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.common.entity.block.CamouflagedBoulderBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderEntity;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.confluence.terraria_boulders.init.ModTags;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CamouflagedBoulderBlock extends FullCollisionBoulderBlock implements EntityBlock {
    public static final Supplier<BlockState> DEFAULT_CAMOUFLAGE = Blocks.STONE::defaultBlockState;

    public CamouflagedBoulderBlock(Properties properties) {
        super(properties
                .dynamicShape()//渲染引擎不缓存形状
                .noOcclusion(),//光照引擎请求透光形状
                CamouflagedBoulderEntity::new);
    }

    public static BlockState getBlockState(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be ? be.getMimicState() : DEFAULT_CAMOUFLAGE.get();
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

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
    }

    //中键拾取
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData, player);
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimic = be.getMimicState();
            if (mimic != null && !mimic.isAir()) {
                //把伪装数据塞进物品的Component里
                stack.set(ModDataComponents.MIMIC_STATE.get(), mimic);
            }
        }
        return stack;
    }

    @Override
    public void summonBoulder(BlockState state, ServerLevel level, BlockPos pos) {
        super.summonBoulder(state, level, pos);
    }

    @Override
    protected <T extends BoulderEntity> void summonBoulder(Level level, BlockPos pos, BlockState blockState, Function<T, Player> function) {
        super.summonBoulder(level, pos, blockState, function);
    }

    //放置方块
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be)) {
            return;
        }
        BlockState mimic = stack.get(ModDataComponents.MIMIC_STATE.get());
        if (mimic != null) {
            be.setMimicState(mimic);
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
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            Item item = stack.getItem();
            BlockState currentMimic = be.getMimicState();

            //伪装逻辑
            if (player.isShiftKeyDown() && (item instanceof BlockItem blockItem) && !(blockItem.getBlock() instanceof CamouflagedBoulderBlock)) {
                //读取玩家朝向
                BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hitResult);
                BlockState nextMimic = blockItem.getBlock().getStateForPlacement(context);

                if (nextMimic != null) be.setMimicState(nextMimic);

                if (!currentMimic.equals(nextMimic) && !be.isLocked()) {
                    if (!level.isClientSide()) {
                        be.setMimicState(nextMimic);
                        level.levelEvent(null, 2001, pos, Block.getId(state));
                        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
                        updateAndChangeState(level, pos, state, be);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            //涂蜡逻辑
            if (stack.is(Items.HONEYCOMB) && !be.isLocked()) {
                if (!level.isClientSide()) {
                    be.setLocked(true);
                    if (!player.isCreative()) {
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
            if (item instanceof AxeItem && be.isLocked()) {
                //只有在服务端确实锁定的状态下，才执行去蜡操作
                if (!level.isClientSide()) {
                    be.setLocked(false);
                    level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);//刮蜡音效
                    level.levelEvent(null, 3004, pos, 0);//斧头除蜡音效
                    stack.hurtAndBreak(1, (ServerLevel) level, player instanceof ServerPlayer sp ? sp : null, item2 -> {});
                    updateAndChangeState(level, pos, state, be);
                }
                return InteractionResult.SUCCESS;
            }

            //触发可交互方块陷阱
            if (currentMimic != null && !currentMimic.isAir()) {
                if (shouldTriggerTrap(currentMimic)) {
                    if (!level.isClientSide()) {
                        //触发巨石
                        this.onExecute(state, (ServerLevel) level, pos);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    //伪装方块是否应该触发陷阱
    private boolean shouldTriggerTrap(BlockState mimicState) {
        //是否在名单上
        if (mimicState.is(ModTags.Blocks.MANUAL_TRAP_TRIGGERS)) {
            return true;
        }

        //检查常见交互性标签，这些方块通常不带be，所以需要通过标签识别
        if (mimicState.is(BlockTags.BUTTONS) || mimicState.is(BlockTags.DOORS) || mimicState.is(BlockTags.TRAPDOORS) || mimicState.is(BlockTags.FENCE_GATES)) {
            return true;
        }

        //检查nf通用容器标签
        if (mimicState.is(Tags.Blocks.CHESTS) || mimicState.is(Tags.Blocks.BARRELS)) {
            return true;
        }

        //是否拥有be
        return mimicState.hasBlockEntity();
    }

    //代理物理碰撞箱（noPhysics问题）
    @Override
    @NonNull
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                return mimicState.getCollisionShape(level, pos, context);
            }
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    //代理外观交互箱
    @Override
    @NonNull
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                return mimicState.getShape(level, pos, context);
            }
        }
        return super.getShape(state, level, pos, context);
    }

    //代理阴影亮度
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                return mimicState.getShadeBrightness(level, pos);
            }
        }
        return super.getShadeBrightness(state, level, pos);
    }

    //放行天空光线（配合noOcclusion()）
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    //代理视觉形状
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                return mimicState.getVisualShape(level, pos, context);
            }
        }
        return super.getVisualShape(state, level, pos, context);
    }

    //代理发光属性
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                //读取目标方块发光亮度
                return mimicState.getLightEmission(level, pos);
            }
        }
        return super.getLightEmission(state, level, pos);
    }

    //代理音效
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                return mimicState.getSoundType(level, pos, entity);
            }
        }
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be) {
            BlockState mimicState = be.getMimicState();
            if (isValidMimic(mimicState)) {
                level.levelEvent(player, 2001, pos, Block.getId(mimicState));
                return;
            }
        }
        super.spawnDestroyParticles(level, player, pos, state);
    }

    //防止无限递归的检查方法
    private boolean isValidMimic(BlockState mimicState) {
        return mimicState != null && !mimicState.isAir() && !(mimicState.getBlock() instanceof CamouflagedBoulderBlock);
    }

    @Override
    @NonNull
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
