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


    public BoulderBlock() {
        this(BoulderEntity::new);
    }

    public BoulderBlock(BoulderFactory factory) {
        this(Properties.of(), factory);
    }

    public BoulderBlock(Properties properties, BoulderFactory factory) {
        super(properties);
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        summon(level, pos, state, entity -> level.getNearestPlayer(entity, BoulderEntity.SEARCH_RANGE));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        if (pLevel.isClientSide) {
            return;
        }
        if (pLevel.hasNeighborSignal(pPos)) {
            return;
        }
        BlockState below = pLevel.getBlockState(pPos.below());
        if (below.isAir()) onExecute(pState, (ServerLevel) pLevel, pPos);
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
