package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.init.ModItems;
import org.confluence.terraria_boulders.events.custom.IBlockBreakable;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BoulderBlock extends Block implements IBlockBreakable {
    private static final VoxelShape SHAPE = Shapes.or(
            box(1.9, -0.1, 1.9, 14.1, 16.1, 14.1),
            box(-0.1, 1.9, 1.9, 16.1, 14.1, 14.1),
            box(1.9, 1.9, -0.1, 14.1, 14.1, 16.1));
    private final BoulderFactory factory;

    public BoulderBlock(Properties properties) {
        this(properties, BoulderEntity::new);
    }

    public BoulderBlock(Properties properties, BoulderFactory factory) {
        super(properties.noLootTable());
        this.factory = factory;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return true;
    }

    //手中是否有手套
    public boolean hasGloveInHand(LivingEntity entity){
        return entity != null && (entity.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.BOULDER_GLOVE) || entity.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.BOULDER_GLOVE));
    }

    /**
     * 移除方块+生成巨石（by事件）
     * @param level 世界
     * @param state 方块状态
     * @param pos 方块坐标
     * @param trigger 触发者，可 null，如果为 null 永远自动索敌
     * */
    @Override
    public void onRemove(Level level, BlockState state, BlockPos pos, @Nullable LivingEntity trigger) {
        if(level instanceof ServerLevel serverLevel){
            level.removeBlock(pos, false);
            this.summonBoulder(state, serverLevel, pos, trigger, !this.hasGloveInHand(trigger));//如果拿着手套就不索敌
        }
    }

    //移除方块（方法）+生成方块（by事件）
//    public void onExcuse(BlockState state, ServerLevel level, BlockPos pos, boolean targetToNearestPlayer) {
//        level.removeBlock(pos, false);
//        //this.summonBoulder(state, level, pos, targetToNearestPlayer);
//    }
//    public void onExcuse(BlockState state, ServerLevel level, BlockPos pos) {
//        //this.onExcuse(state, level, pos, true);
//        level.removeBlock(pos, false);
//        //this.summonBoulder(state, level, pos, targetToNearestPlayer);
//    }

//    @Override
//    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
//        if(level instanceof ServerLevel serverLevel) {
//            this.onExcuse(state, serverLevel, pos);
//        }
//    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        //统一调用触发逻辑
        if (level instanceof ServerLevel serverLevel) {
            this.onRemove(serverLevel, state, hit.getBlockPos(), null);
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit) {
        this.onRemove(level, state, pos, null);
        super.onExplosionHit(state, level, pos, explosion, onHit);
    }

//    @Override
//    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
//        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
//        summonBoulder(state, level, pos);
//    }
//
//    //可能不需要填充
//    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston, boolean summon) {
//        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
//        if (summon) summonBoulder(state, level, pos);
//    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        BlockState below = level.getBlockState(pos.below());

        if (below.isAir() && level instanceof ServerLevel serverLevel) {
            this.onRemove(serverLevel, state, pos, null);
        }
    }

    /**
    * @param trigger 触发者
    * @param targetToNearestPlayer 实体化时是否有目标
    * */
    protected void summonBoulder(BlockState state, ServerLevel level, BlockPos pos, LivingEntity trigger, boolean targetToNearestPlayer) {
        if (targetToNearestPlayer) {
            this.summonBoulder(level, pos, state, trigger, entity -> level.getNearestPlayer(entity, BoulderEntity.SEARCH_RANGE));
        }
        else{
            this.summonBoulder(level, pos, state, trigger, _ -> null);
        }
    }
    public void summonBoulder(BlockState state, ServerLevel level, BlockPos pos, LivingEntity trigger) {
        this.summonBoulder(state, level, pos, trigger, true);
    }

    public void summonBoulder(Level level, BlockPos pos, BlockState blockState, LivingEntity trigger, Function<BoulderEntity, Player> function) {
        //调用工厂方法，如果是子类方块，会动态触发子类重写的方法
        BoulderEntity entity = this.createBoulderEntity(level, pos.getBottomCenter(), blockState);
        entity.interactedWithGlove = this.hasGloveInHand(trigger);//如果想取消这个可以去覆写hasGloveInHand
        this.onBoulderSummon(level, pos, blockState, trigger, function, entity);//触发钩子
        level.addFreshEntity(entity);
    }

    //创建一个钩子，便于子类自定义
    protected void onBoulderSummon(Level level, BlockPos pos, BlockState blockState, LivingEntity trigger, Function<BoulderEntity, Player> function, BoulderEntity boulderEntity) {
        Player player = function.apply(boulderEntity);
        if (player != null && !level.getBlockState(pos.below()).isAir()) {
            boulderEntity.targetTo(player);
        }
    }

    public BoulderEntity createBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        return factory.create(level, pos, blockState);
    }

    @Override
    @NonNull
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @FunctionalInterface
    public interface BoulderFactory {
        BoulderEntity create(Level level, Vec3 position, BlockState blockState);
    }
}
