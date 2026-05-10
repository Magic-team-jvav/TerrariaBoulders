package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;

import java.util.function.Function;

public class BoulderBlock extends Block {
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

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        //统一调用触发逻辑
        if (level instanceof ServerLevel serverLevel) {
            onExecute(state, serverLevel, hit.getBlockPos());
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        summonBoulder(state, level, pos);
    }

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
            onExecute(state, serverLevel, pos);
        }
    }

    /**
     * 统一的滚动触发点：负责移除方块并触发后续的召唤
     */
    public void onExecute(BlockState state, ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
    }

    protected void summonBoulder(BlockState state, ServerLevel level, BlockPos pos) {
        summonBoulder(level, pos, state, entity -> level.getNearestPlayer(entity, BoulderEntity.SEARCH_RANGE));
    }

    protected <T extends BoulderEntity> void summonBoulder(Level level, BlockPos pos, BlockState blockState, Function<T, Player> function) {
        @SuppressWarnings("unchecked")
        T entity = (T) createBoulderEntity(level, pos.getCenter(), blockState);
        if (!level.getBlockState(pos.below()).isAir()) {
            entity.targetTo(function.apply(entity));
        }
        level.addFreshEntity(entity);
    }

    public BoulderEntity createBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        return factory.create(level, pos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @FunctionalInterface
    public interface BoulderFactory {
        BoulderEntity create(Level level, Vec3 position, BlockState blockState);
    }
}
