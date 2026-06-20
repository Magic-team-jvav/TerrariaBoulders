package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.terraria_boulders.common.entity.block.GiantBoulderBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;
import org.confluence.terraria_boulders.common.entity.boulder.GiantBoulderEntity;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.util.LevelUtil;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GiantBoulderBlock extends BoulderBlock implements EntityBlock {
    private boolean isSelfDestructing = false;
    private int Size = 3;// 3×3×3 体积

    private double scale(double value) {
        return 8.0 + (value - 8.0) * this.Size;
    }

    private final VoxelShape Shape = Shapes.or(
            box(scale(1.9), scale(-0.1), scale(1.9), scale(14.1), scale(16.1), scale(14.1)),
            box(scale(-0.1), scale(1.9), scale(1.9), scale(16.1), scale(14.1), scale(14.1)),
            box(scale(1.9), scale(1.9), scale(-0.1), scale(14.1), scale(14.1), scale(16.1))
    );
    //保底模型防止掉虚空
    private static final VoxelShape SEPARATE_COLLISION_SHAPE = Shapes.or(
            box(1.9, 0.0, 1.9, 14.1, 16.0, 14.1),
            box(0.0, 1.9, 1.9, 16.0, 14.1, 14.1),
            box(1.9, 1.9, 0.0, 14.1, 14.1, 16.0)
    );

    public GiantBoulderBlock(Properties properties) {
        super(properties, GiantBoulderEntity::new);
    }

    public GiantBoulderBlock(Properties properties, int size) {
        super(properties, GiantBoulderEntity::new);
        this.Size = size;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new GiantBoulderBlockEntity(pos, state);
    }

    public int getSize() {
        return this.Size;
    }

    public VoxelShape getShape() {
        return this.Shape;
    }

    //将世界切分为多个3x3网格，获取当前点所在的3x3网格
//    public static Iterable<BlockPos> getVolume(BlockPos pos, int size) {
//        int minX = Math.floorDiv(pos.getX(), size) * size;//向下取最近的三的倍数为min点，如1在[0,2]范围min取0
//        int minY = Math.floorDiv(pos.getY(), size) * size;//乘Size（除数）是因为floorDiv()返回的是两个整数相除并向下取整的结果（这个函数考虑了负数情况），所以要乘回来拿到min点
//        int minZ = Math.floorDiv(pos.getZ(), size) * size;
//        int maxX = minX + (size - 1);//减一是因为min点本身占1
//        int maxY = minY + (size - 1);
//        int maxZ = minZ + (size - 1);
//        return BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ);
//    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos centerPos = context.getClickedPos().above(Size / 2);//以放置中心为巨型方块中心
        BlockPosData data = getBetweenClosed(centerPos);
        //碰撞扫描框
        AABB checkBox = new AABB(
                data.minPos.getX(), data.minPos.getY(), data.minPos.getZ(),
                data.maxPos.getX() + 1.0, data.maxPos.getY() + 1.0, data.maxPos.getZ() + 1.0
        ).deflate(0.01);//略微收缩防边缘误判
        //空间内不能有其他东西
        if (!LevelUtil.noCollision(context.getLevel(), this.Size >= 5 ? null : context.getPlayer(), checkBox))
            return null;//巨石过大就不管会不会卡你了，手都不够长的
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            BlockPosData data = getBetweenClosed(pos.above(Size / 2));

            Iterable<BlockPos> posIterable = data.iterablePos;
            //填充方块
            for (BlockPos iterPos : posIterable) {
                //放置方块，跳过自己
                if (!iterPos.equals(pos))
                    level.setBlockAndUpdate(iterPos, ModBlocks.GIANT_BOULDER.get().defaultBlockState());
                //更新相对坐标数据
                if (level.getBlockEntity(iterPos) instanceof GiantBoulderBlockEntity be) {
                    be.setRelativePosIter(iterPos, data.minPos, data.maxPos);
                }
            }
        }
    }

    private BlockPosData getBetweenClosed(BlockPos pos) {
        int minDistance = this.Size / 2;
        BlockPos minPos;
        BlockPos maxPos;
        //奇偶数适配
        if (this.Size % 2 != 0) {
            //奇数完全对称
            //中心点往左往右各minDistance格
            minPos = new BlockPos(pos.getX() - minDistance, pos.getY() - minDistance, pos.getZ() - minDistance);
            maxPos = new BlockPos(pos.getX() + minDistance, pos.getY() + minDistance, pos.getZ() + minDistance);
        } else {
            //偶数偏一格
            minPos = new BlockPos(pos.getX() - minDistance, pos.getY() - minDistance, pos.getZ() - minDistance);
            maxPos = new BlockPos(minPos.getX() + this.Size - 1, minPos.getY() + this.Size - 1, minPos.getZ() + this.Size - 1);
        }
        return new BlockPosData(minPos, maxPos, BlockPos.betweenClosed(minPos, maxPos));
    }

    @Override
    @NonNull
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!isSelfDestructing) this.destroyGiantBlock(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit) {
        if (!isSelfDestructing) this.destroyGiantBlock(level, pos);
        super.onExplosionHit(state, level, pos, explosion, onHit);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!isSelfDestructing) this.destroyGiantBlock(level, hit.getBlockPos());
        super.onProjectileHit(level, state, hit, projectile);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston, false);
        if (!isSelfDestructing) this.destroyGiantBlock(level, pos);
    }

    //摧毁并生成巨型巨石实体
    private void destroyGiantBlock(Level level, BlockPos pos) {
        isSelfDestructing = true;
        if (level instanceof ServerLevel serverLevel) {
            if (level.getBlockEntity(pos) instanceof GiantBoulderBlockEntity giantBoulderBE) {//有方块实体
                Iterable<BlockPos> relativePosIter = giantBoulderBE.getRelativePosIter();
                //遍历迭代器
                for (BlockPos iterRelativePos : relativePosIter) {
                    BlockPos actualPos = pos.offset(iterRelativePos);//实际坐标
                    if (level.getBlockState(actualPos).getBlock() instanceof GiantBoulderBlock) {//保证是巨石方块
                        level.removeBlock(actualPos, false);//移除方块
                    }
                }
                //生成巨型巨石实体
                BlockPos centerPos = getCenterPos(relativePosIter, pos);
                this.summonBoulder(this.defaultBlockState(), serverLevel, centerPos);
            }
        }
        isSelfDestructing = false;
    }

    //用钩子确定entity的大小
    @Override
    protected void onBoulderSummon(Level level, BlockPos centerPos, BlockState blockState, Function<BoulderEntity, Player> function, BoulderEntity entity) {
        //应用大小
        if (entity instanceof GiantBoulderEntity gbEntity) {
            gbEntity.setSize(this.Size);
        }
        BlockPosData data = this.getBetweenClosed(centerPos);
        Iterable<BlockPos> iterablePos = data.iterablePos;
        for (BlockPos iterPos : iterablePos) {
            if (iterPos.getY() != data.minPos.getY()) continue;//只检查最下方的方块
            //任意一格有实体方块支撑即可
            if (!level.getBlockState(iterPos.below(this.Size / 2)).isAir()) {//由于巨石生成在中间，故需要获取实际最下方
                entity.targetTo(function.apply(entity));
                return;
            }
        }
    }

    /**
     * 获取该坐标所属的多方块结构的中心店相对坐标
     *
     * @param relativePosIter 存储在 BE 中的纯相对坐标集合
     * @return 相对坐标 Vec3
     */
    public static Vec3 getRelativeCenterVec3(Iterable<BlockPos> relativePosIter) {
        //System.out.println("relativePosIter: " + (relativePosIter == null ? "null" : relativePosIter));

        //为空的话返回null
        if (relativePosIter == null || !relativePosIter.iterator().hasNext()) {
            return null;//pos.getCenter();
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        //在相对坐标集合（局部空间）里找出最大和最小的边界
        for (BlockPos relPos : relativePosIter) {
            if (relPos.getX() < minX) minX = relPos.getX();
            if (relPos.getY() < minY) minY = relPos.getY();
            if (relPos.getZ() < minZ) minZ = relPos.getZ();

            if (relPos.getX() > maxX) maxX = relPos.getX();
            if (relPos.getY() > maxY) maxY = relPos.getY();
            if (relPos.getZ() > maxZ) maxZ = relPos.getZ();
        }

        //计算相对空间的中点（最大值 + 1.0 以完美包裹住整格方块）
        double relCenterX = (minX + (maxX + 1.0)) / 2.0;
        double relCenterY = (minY + (maxY + 1.0)) / 2.0;
        double relCenterZ = (minZ + (maxZ + 1.0)) / 2.0;

        return new Vec3(relCenterX, relCenterY, relCenterZ);
    }

    //获取中心点相对坐标
    public static BlockPos getRelativeCenterPos(Iterable<BlockPos> iterPos) {
        Vec3 relCenterPos = getRelativeCenterVec3(iterPos);
        if (relCenterPos == null) return null;
        return BlockPos.containing(relCenterPos);
    }

    /**
     * 获取该坐标所属的多方块结构在世界中的几何中心点
     *
     * @param relativePosIter 存储在 BE 中的纯相对坐标集合
     * @param pos             主方块（核心）在世界中的绝对方块坐标 BlockPos
     * @return 世界坐标 Vec3
     */
    public static Vec3 getCenterVec3(Iterable<BlockPos> relativePosIter, BlockPos pos) {

        Vec3 relCenterPos = getRelativeCenterVec3(relativePosIter);
        if (relCenterPos == null) return pos.getCenter();//为空的话pos的中点就是中点

        //将局部相对中心，叠加到主方块的世界坐标上
        double worldCenterX = pos.getX() + relCenterPos.x();
        double worldCenterY = pos.getY() + relCenterPos.y();
        double worldCenterZ = pos.getZ() + relCenterPos.z();

        return new Vec3(worldCenterX, worldCenterY, worldCenterZ);
    }

    //获取中心点世界坐标
    public static BlockPos getCenterPos(Iterable<BlockPos> iterPos, BlockPos pos) {
        return BlockPos.containing(getCenterVec3(iterPos, pos));
    }

    @Override
    public @NonNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NonNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCoreShape(level, pos);
    }

    @Override
    public @NonNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCoreShape(level, pos);
    }

    @Override
    protected @NonNull VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getCoreShape(level, pos);
    }

    /**
     * 返回偏移后的多方块（巨型方块）结构的中心点 Shape
     *
     * @param level 世界纬度
     * @param pos   方块坐标
     *              //* @param centerOnly 是否只返回中心点，其余返回空
     *
     */
    private VoxelShape getCoreShape(BlockGetter level, BlockPos pos) {

        //健壮检查
        if (!(level.getBlockEntity(pos) instanceof GiantBoulderBlockEntity be)) return Shapes.empty();
        Iterable<BlockPos> rel = be.getRelativePosIter();
        if (rel == null || !rel.iterator().hasNext()) return SEPARATE_COLLISION_SHAPE;

        //计算与中心点偏移距离，由于自己是0 0 0所以直接使用即可
        Vec3 relCenterPos = getRelativeCenterVec3(rel);

        return Shape.move(relCenterPos.x - 0.5, relCenterPos.y - 0.5, relCenterPos.z - 0.5);//进行偏移
    }

    /// 是否为相对坐标中心
    public static boolean isCenter(Iterable<BlockPos> relPoses) {
        //传入pos相对结构中心距离
        BlockPos relCenterPos1 = getRelativeCenterPos(relPoses);
        if (relCenterPos1 != null) {
            return relCenterPos1.getX() == 0 && relCenterPos1.getY() == 0 && relCenterPos1.getZ() == 0;
        }
        return false;
    }

    /**
     * 巨型方块相关数据
     *
     * @param minPos      角1坐标
     * @param maxPos      角2坐标
     * @param iterablePos 世界坐标集合
     *
     */
    public record BlockPosData(BlockPos minPos, BlockPos maxPos, Iterable<BlockPos> iterablePos) {
    }
}