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
    private final BoulderFactory factory;
    private static final VoxelShape SHAPE = Shapes.or(
            box(1.9, -0.1, 1.9, 14.1, 16.1, 14.1),
            box(-0.1, 1.9, 1.9, 16.1, 14.1, 14.1),
            box(1.9, 1.9, -0.1, 14.1, 14.1, 16.1));

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
        level.removeBlock(hit.getBlockPos(), false);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        summon(level, pos, state, entity -> level.getNearestPlayer(entity, BoulderEntity.SEARCH_RANGE));
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
        if (below.isAir()) onExecute(state, (ServerLevel) level, pos);
    }

    public void onExecute(BlockState state, ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
    }

    protected void summon(Level level, BlockPos pos, BlockState blockState, Function<BoulderEntity, Player> function) {
        BoulderEntity entity = factory.create(level, pos.getCenter(), blockState);
        if (!level.getBlockState(pos.below()).isAir()) {
            entity.targetTo(function.apply(entity));
        }
        level.addFreshEntity(entity);
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
