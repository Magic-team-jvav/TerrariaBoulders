package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;
import org.confluence.terraria_boulders.common.entity.block.CamouflagedBoulderBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderEntity;
import org.confluence.terraria_boulders.events.custom.IUseItemOnBlock;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.confluence.terraria_boulders.init.ModItems;
import org.confluence.terraria_boulders.init.ModTags;
import org.confluence.terraria_boulders.mixin.BlockAccessor;
import org.confluence.terraria_boulders.mixin.BlockItemAccessor;
import org.confluence.terraria_boulders.mixin.BlockPlaceContextAccessor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 伪装巨石方块类
 * <p>
 * 该方块可以伪装成其他方块的外观和行为,支持锁定机制防止伪装被改变,
 * 并且可以作为陷阱触发器使用。方块的实际属性会委托给伪装状态的方块。
 * </p>
 */
@SuppressWarnings("NullableProblems")
public class CamouflagedBoulderBlock extends FullCollisionBoulderBlock implements EntityBlock, IUseItemOnBlock {
    /**
     * 默认伪装状态 supplier
     */
    public static final Supplier<BlockState> DEFAULT_CAMOUFLAGE = () -> ModBlocks.BOULDER.get().defaultBlockState();

    /**
     * 构造伪装巨石方块
     *
     * @param properties 方块属性
     */
    public CamouflagedBoulderBlock(Properties properties) {
        super(properties, CamouflagedBoulderEntity::new);
    }

    /**
     * 获取指定位置的方块状态(从方块实体中读取伪装状态)
     *
     * @param level 游戏世界
     * @param pos   方块位置
     * @return 伪装状态,如果不存在则返回默认状态
     */
    public static BlockState getBlockState(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be ? be.getMimicState() : DEFAULT_CAMOUFLAGE.get();
    }

    /**
     * 获取方块实体的伪装状态
     *
     * @param blockEntity 方块实体
     * @return 伪装状态,如果无效则返回默认状态
     */
    public static BlockState getCamouflageState(BlockEntity blockEntity) {
        if (!(blockEntity instanceof CamouflagedBoulderBlockEntity be)) {
            return DEFAULT_CAMOUFLAGE.get();
        }
        BlockState mimicState = be.getMimicState();
        if (mimicState == null || mimicState.isAir()) {
            return DEFAULT_CAMOUFLAGE.get();
        }
        return mimicState;
    }

    /**
     * 设置指定位置的伪装状态
     *
     * @param level      方块访问器
     * @param pos        方块位置
     * @param mimicState 要设置的伪装状态
     * @return 旧的伪装状态
     */
    public static BlockState setCamouflageState(BlockGetter level, BlockPos pos, BlockState mimicState) {
        return setCamouflageState(level.getBlockEntity(pos), mimicState);
    }

    /**
     * 设置方块实体的伪装状态
     *
     * @param blockEntity 方块实体
     * @param mimicState  要设置的伪装状态
     * @return 旧的伪装状态
     */
    public static BlockState setCamouflageState(BlockEntity blockEntity, BlockState mimicState) {
        if (!(blockEntity instanceof CamouflagedBoulderBlockEntity be)) {
            return DEFAULT_CAMOUFLAGE.get();
        }
        var old = be.getMimicState();
        be.setMimicState(mimicState);
        return old;
    }

    /**
     * 从物品堆栈中获取保存的伪装状态
     *
     * @param stack 物品堆栈
     * @return 保存的伪装状态,如果无效则返回默认状态
     */
    public static BlockState getBlockState(ItemStack stack) {
        BlockState blockState = stack.get(ModDataComponents.MIMIC_STATE.get());
        if (blockState == null || blockState.isAir()) {
            return DEFAULT_CAMOUFLAGE.get();
        }
        return blockState;
    }

    /**
     * 获取指定位置的伪装状态
     *
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 伪装状态
     */
    public static BlockState getCamouflageState(BlockGetter level, BlockPos pos) {
        return getCamouflageState(level.getBlockEntity(pos));
    }

    /**
     * 获取方块的地图颜色
     *
     * @param state        方块状态
     * @param level        方块访问器
     * @param pos          方块位置
     * @param defaultColor 默认颜色
     * @return 地图颜色
     */
    @Override
    public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        return getCamouflageState(level, pos).getMapColor(level, pos);
    }

    /**
     * 判断方块是否可以被水化
     *
     * @param state    方块状态
     * @param getter   方块访问器
     * @param pos      方块位置
     * @param fluid    流体状态
     * @param fluidPos 流体位置
     * @return 是否可以被水化
     */
    @Override
    public boolean canBeHydrated(BlockState state, BlockGetter getter, BlockPos pos, FluidState fluid, BlockPos fluidPos) {
        return getCamouflageState(getter, pos).canBeHydrated(getter, pos, fluid, fluidPos);
    }

    /**
     * 判断是否隐藏相邻方块的面
     *
     * @param level         方块访问器
     * @param pos           方块位置
     * @param state         当前方块状态
     * @param neighborState 邻居方块状态
     * @param dir           方向
     * @return 是否隐藏相邻面
     */
    @Override
    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        return getCamouflageState(level, pos).hidesNeighborFace(level, pos, state, dir);
    }

    /**
     * 判断是否可以连接红石
     *
     * @param state     方块状态
     * @param level     方块访问器
     * @param pos       方块位置
     * @param direction 方向
     * @return 是否可以连接红石
     */
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Direction direction) {
        return getCamouflageState(level, pos).getBlock().canConnectRedstone(getCamouflageState(level, pos), level, pos, direction);
    }

    /**
     * 判断是否为脚手架方块
     *
     * @param state  方块状态
     * @param level  关卡读取器
     * @param pos    方块位置
     * @param entity 实体
     * @return 是否为脚手架
     */
    @Override
    public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return getCamouflageState(level, pos).isScaffolding(entity);
    }

    /**
     * 获取工具修改后的方块状态
     *
     * @param state       方块状态
     * @param context     使用上下文
     * @param itemAbility 物品能力
     * @param simulate    是否模拟
     * @return 修改后的方块状态
     */
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        setCamouflageState(context.getLevel(), context.getClickedPos(), getCamouflageState(context.getLevel(), context.getClickedPos()).getToolModifiedState(context, itemAbility, simulate));
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }

    /**
     * 获取直接红石信号强度
     *
     * @param state     方块状态
     * @param level     方块访问器
     * @param pos       方块位置
     * @param direction 方向
     * @return 信号强度
     */
    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockState camouflageState = getCamouflageState(level, pos);
        if (camouflageState.getBlock() == Blocks.REDSTONE_WIRE) {
            return super.getDirectSignal(state, level, pos, direction);
        }
        return getCamouflageState(level, pos).getDirectSignal(level, pos, direction);
    }

    /**
     * 获取红石信号强度
     *
     * @param state     方块状态
     * @param level     方块访问器
     * @param pos       方块位置
     * @param direction 方向
     * @return 信号强度
     */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockState camouflageState = getCamouflageState(level, pos);
        if (camouflageState.getBlock() == Blocks.REDSTONE_WIRE) {
            return super.getSignal(state, level, pos, direction);
        }
        return camouflageState.getSignal(level, pos, direction);
    }

    /**
     * 判断碰撞形状是否为完整方块
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 是否为完整方块
     */
    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).isCollisionShapeFullBlock(level, pos);
    }

    /**
     * 获取实体内部的碰撞形状
     *
     * @param state  方块状态
     * @param level  方块访问器
     * @param pos    方块位置
     * @param entity 实体
     * @return 碰撞形状
     */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getCamouflageState(level, pos).getEntityInsideCollisionShape(level, pos, entity);
    }

    /**
     * 获取方块支撑形状
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 支撑形状
     */
    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).getBlockSupportShape(level, pos);
    }

    /**
     * 获取交互形状
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 交互形状
     */
    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).getInteractionShape(level, pos);
    }

    /**
     * 判断方块是否可以存活
     *
     * @param state 方块状态
     * @param level 关卡读取器
     * @param pos   方块位置
     * @return 是否可以存活
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return getCamouflageState(level, pos).canSurvive(level, pos);
    }

    /**
     * 判断是否应该显示流体覆盖层
     *
     * @param state      方块状态
     * @param level      方块和光源访问器
     * @param pos        方块位置
     * @param fluidState 流体状态
     * @return 是否显示流体覆盖层
     */
    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndLightGetter level, BlockPos pos, FluidState fluidState) {
        return getCamouflageState(level, pos).shouldDisplayFluidOverlay(level, pos, fluidState);
    }

    /**
     * 判断碰撞是否垂直扩展
     *
     * @param state           方块状态
     * @param level           方块访问器
     * @param pos             方块位置
     * @param collidingEntity 碰撞实体
     * @return 是否垂直扩展
     */
    @Override
    public boolean collisionExtendsVertically(BlockState state, BlockGetter level, BlockPos pos, Entity collidingEntity) {
        return getCamouflageState(level, pos).collisionExtendsVertically(level, pos, collidingEntity);
    }

    /**
     * 判断是否为火源
     *
     * @param state     方块状态
     * @param level     关卡读取器
     * @param pos       方块位置
     * @param direction 方向
     * @return 是否为火源
     */
    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return getCamouflageState(level, pos).isFireSource(level, pos, direction);
    }

    /**
     * 获取相邻方块的路径类型
     *
     * @param state        方块状态
     * @param level        方块访问器
     * @param pos          方块位置
     * @param mob          生物实体
     * @param originalType 原始路径类型
     * @return 路径类型
     */
    @Override
    @Nullable
    public PathType getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, PathType originalType) {
        return getCamouflageState(level, pos).getAdjacentBlockPathType(level, pos, mob, originalType);
    }

    /**
     * 获取方块的路径类型
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @param mob   生物实体
     * @return 路径类型
     */
    @Override
    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return getCamouflageState(level, pos).getBlockPathType(level, pos, mob);
    }

    /**
     * 获取视点处的方块状态
     *
     * @param state     方块状态
     * @param level     方块访问器
     * @param pos       方块位置
     * @param viewpoint 视点位置
     * @return 视点处的方块状态
     */
    @Override
    public BlockState getStateAtViewpoint(BlockState state, BlockGetter level, BlockPos pos, Vec3 viewpoint) {
        return getCamouflageState(level, pos).getStateAtViewpoint(level, pos, viewpoint);
    }

    /**
     * 获取信标颜色乘数
     *
     * @param state     方块状态
     * @param level     关卡读取器
     * @param pos       方块位置
     * @param beaconPos 信标位置
     * @return 颜色乘数,如果不影响信标则返回null
     */
    @Override
    @Nullable
    public Integer getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        return getCamouflageState(level, pos).getBeaconColorMultiplier(level, pos, beaconPos);
    }

    /**
     * 判断是否应该检查弱能量
     *
     * @param state 方块状态
     * @param level 信号访问器
     * @param pos   方块位置
     * @param side  方向
     * @return 是否应该检查弱能量
     */
    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        BlockState camouflageState = getCamouflageState(level, pos);
        if (camouflageState.getBlock() == Blocks.REDSTONE_WIRE) {
            return super.shouldCheckWeakPower(state, level, pos, side);
        }
        return getCamouflageState(level, pos).shouldCheckWeakPower(level, pos, side);
    }

    /**
     * 创建新的方块实体
     *
     * @param pos   方块位置
     * @param state 方块状态
     * @return 方块实体
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new CamouflagedBoulderBlockEntity(pos, state);
    }

    /**
     * 移除后影响邻居(空实现)
     *
     * @param state         方块状态
     * @param level         服务端世界
     * @param pos           方块位置
     * @param movedByPiston 是否由活塞移动
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    /**
     * 获取克隆物品堆栈(中键拾取)
     *
     * @param level       关卡读取器
     * @param pos         方块位置
     * @param state       方块状态
     * @param includeData 是否包含数据
     * @param player      玩家
     * @return 克隆的物品堆栈
     */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData, player);
        if (!(level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be)) {
            return stack;
        }
        stack.set(ModDataComponents.MIMIC_STATE.get(), getCamouflageState(be));
        return stack;
    }

    /**
     * 获取外观方块状态
     *
     * @param state      方块状态
     * @param level      方块和光源访问器
     * @param pos        方块位置
     * @param side       方向
     * @param queryState 查询状态
     * @param queryPos   查询位置
     * @return 外观方块状态
     */
    @Override
    public BlockState getAppearance(BlockState state, BlockAndLightGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        return getCamouflageState(level, pos).getAppearance(level, pos, side, queryState, queryPos);
    }

    /**
     * 旋转方块
     *
     * @param state     方块状态
     * @param level     关卡访问器
     * @param pos       方块位置
     * @param direction 旋转方向
     * @return 旋转后的方块状态
     */
    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        setCamouflageState(level, pos, getCamouflageState(level, pos).rotate(level, pos, direction));
        return super.rotate(state, level, pos, direction);
    }

    /**
     * 获取附魔能量加成
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 附魔能量加成值
     */
    @Override
    public float getEnchantPowerBonus(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).getEnchantPowerBonus((LevelReader) level, pos);
    }

    /**
     * 判断是否为传送门框架
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 是否为传送门框架
     */
    @Override
    public boolean isPortalFrame(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).isPortalFrame(level, pos);
    }

    /**
     * 判断是否为潮涌核心框架
     *
     * @param state   方块状态
     * @param level   关卡读取器
     * @param pos     方块位置
     * @param conduit 潮涌核心位置
     * @return 是否为潮涌核心框架
     */
    @Override
    public boolean isConduitFrame(BlockState state, LevelReader level, BlockPos pos, BlockPos conduit) {
        return getCamouflageState(level, pos).isConduitFrame(level, pos, conduit);
    }

    /**
     * 判断是否为肥沃土地
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 是否肥沃
     */
    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).isFertile(level, pos);
    }

    /**
     * 树木生长时的处理
     *
     * @param state         方块状态
     * @param level         世界生成关卡
     * @param placeFunction 放置函数
     * @param randomSource  随机源
     * @param pos           方块位置
     * @param config        树木配置
     * @return 是否拦截树木生长
     */
    @Override
    public boolean onTreeGrow(BlockState state, WorldGenLevel level, BiConsumer<BlockPos, BlockState> placeFunction, RandomSource randomSource, BlockPos pos, TreeConfiguration config) {
        return getCamouflageState(level, pos).onTreeGrow(level, placeFunction, randomSource, pos, config);
    }

    /**
     * 判断是否可以维持植物生长
     *
     * @param state        方块状态
     * @param level        方块访问器
     * @param soilPosition 土壤位置
     * @param facing       朝向
     * @param plant        植物状态
     * @return 三态结果(TRUE/FALSE/DEFAULT)
     */
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return getCamouflageState(level, soilPosition).canSustainPlant(level, soilPosition, facing, plant);
    }

    /**
     * 播放脚步声
     *
     * @param state            方块状态
     * @param level            游戏世界
     * @param pos              方块位置
     * @param entity           实体
     * @param volumeMultiplier 音量倍数
     * @param pitchMultiplier  音调倍数
     */
    @Override
    public void playStepSound(BlockState state, Level level, BlockPos pos, Entity entity, float volumeMultiplier, float pitchMultiplier) {
        getCamouflageState(level, pos).playStepSound(level, pos, entity, volumeMultiplier, pitchMultiplier);
    }

    /**
     * 播放掉落声音
     *
     * @param state  方块状态
     * @param level  游戏世界
     * @param pos    方块位置
     * @param entity 生物实体
     */
    @Override
    public void playFallSound(BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        getCamouflageState(level, pos).playFallSound(level, pos, entity);
    }

    /**
     * 添加奔跑粒子效果
     *
     * @param state  方块状态
     * @param level  游戏世界
     * @param pos    方块位置
     * @param entity 实体
     * @return 是否添加了效果
     */
    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return getCamouflageState(level, pos).addRunningEffects(level, pos, entity);
    }

    /**
     * 添加落地粒子效果
     *
     * @param state1            方块状态1
     * @param level             服务端世界
     * @param pos               方块位置
     * @param state2            方块状态2
     * @param entity            生物实体
     * @param numberOfParticles 粒子数量
     * @return 是否添加了效果
     */
    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return getCamouflageState(level, pos).addLandingEffects(level, pos, state2, entity, numberOfParticles);
    }

    /**
     * 获取爆炸抗性
     *
     * @param state     方块状态
     * @param level     方块访问器
     * @param pos       方块位置
     * @param explosion 爆炸对象
     * @return 爆炸抗性值
     */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getCamouflageState(level, pos).getExplosionResistance(level, pos, explosion);
    }

    /**
     * 获取床的方向
     *
     * @param state 方块状态
     * @param level 关卡读取器
     * @param pos   方块位置
     * @return 床的方向
     */
    @Override
    public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        return getCamouflageState(level, pos).getBedDirection(level, pos);
    }

    /**
     * 召唤巨石
     *
     * @param state 方块状态
     * @param level 服务端世界
     * @param pos   方块位置
     */
    @Override
    public void summonBoulder(BlockState state, ServerLevel level, BlockPos pos) {
        super.summonBoulder(state, level, pos);
    }

    /**
     * 判断是否可以采集方块
     *
     * @param state  方块状态
     * @param level  方块访问器
     * @param pos    方块位置
     * @param player 玩家
     * @return 永远返回false,不能直接采集
     */
    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return false;
    }

    /**
     * 判断是否在燃烧
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 是否在燃烧
     */
    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).isBurning(level, pos);
    }

    /**
     * 判断打开的活板门上方是否为可攀爬方块
     *
     * @param state         方块状态
     * @param level         关卡读取器
     * @param pos           方块位置
     * @param trapdoorState 活板门状态
     * @return 是否为可攀爬
     */
    @Override
    public boolean makesOpenTrapdoorAboveClimbable(BlockState state, LevelReader level, BlockPos pos, BlockState trapdoorState) {
        return getCamouflageState(level, pos).getBlock().makesOpenTrapdoorAboveClimbable(getCamouflageState(level, pos), level, pos, trapdoorState);
    }

    /**
     * 判断是否为梯子
     *
     * @param state  方块状态
     * @param level  关卡读取器
     * @param pos    方块位置
     * @param entity 生物实体
     * @return 是否为梯子
     */
    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return getCamouflageState(level, pos).isLadder(level, pos, entity);
    }

    /**
     * 判断是否可以被岩浆点燃
     *
     * @param state     方块状态
     * @param level     方块访问器
     * @param pos       方块位置
     * @param direction 方向
     * @return 是否可以被点燃
     */
    @Override
    public boolean ignitedByLava(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getCamouflageState(level, pos).ignitedByLava(level, pos, direction);
    }

    /**
     * 判断是否有动态光源发射
     *
     * @param state 方块状态
     * @return 永远返回true,支持动态光源
     */
    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    /**
     * 处理方块放置时的事件,保存伪装状态到物品数据
     *
     * @param level  游戏世界
     * @param pos    方块位置
     * @param state  方块状态
     * @param placer 放置者
     * @param stack  使用的物品堆栈
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        setCamouflageState(level, pos, getBlockState(stack));
    }

    /**
     * 获取摩擦系数
     *
     * @param state  方块状态
     * @param level  关卡读取器
     * @param pos    方块位置
     * @param entity 实体
     * @return 摩擦系数
     */
    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getCamouflageState(level, pos).getFriction(level, pos, entity);
    }

    /**
     * 更新间接邻居形状
     *
     * @param state       方块状态
     * @param level       关卡访问器
     * @param pos         方块位置
     * @param updateFlags 更新标志
     * @param updateLimit 更新限制
     */
    @Override
    protected void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, @UpdateFlags int updateFlags, int updateLimit) {
        super.updateIndirectNeighbourShapes(state, level, pos, updateFlags, updateLimit);
        getCamouflageState(level, pos).updateIndirectNeighbourShapes(level, pos, updateFlags, updateLimit);
    }

    /**
     * 更新形状
     *
     * @param state                方块状态
     * @param level                关卡读取器
     * @param ticks                计划刻访问器
     * @param pos                  方块位置
     * @param directionToNeighbour 邻居方向
     * @param neighbourPos         邻居位置
     * @param neighbourState       邻居状态
     * @param random               随机源
     * @return 更新后的方块状态
     */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        setCamouflageState(level, pos, getCamouflageState(level, pos).updateShape(level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random));
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    /**
     * 处理邻居方块变化
     *
     * @param state         方块状态
     * @param level         游戏世界
     * @param pos           方块位置
     * @param block         变化的方块
     * @param orientation   方向
     * @param movedByPiston 是否由活塞移动
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @org.jetbrains.annotations.Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        getCamouflageState(level, pos).handleNeighborChanged(level, pos, block, orientation, movedByPiston);
    }

    /**
     * 空手右键交互处理
     * <p>
     * 在未锁定状态下,玩家按住Shift键右键可以解除伪装,恢复为默认状态
     * </p>
     *
     * @param state     方块状态
     * @param level     游戏世界
     * @param pos       方块位置
     * @param player    玩家
     * @param hitResult 命中结果
     * @return 交互结果
     */
    @Override
    @Nullable
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity cbbe)) {
            return InteractionResult.PASS;
        }

        BlockState currentMimic = cbbe.getMimicState();
        BlockState defaultMimic = DEFAULT_CAMOUFLAGE.get();

        if (cbbe.isLocked() || !player.isShiftKeyDown() || (currentMimic != null && currentMimic.equals(defaultMimic))) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            cbbe.setMimicState(defaultMimic);
            level.levelEvent(null, 2001, pos, Block.getId(currentMimic));
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        updateAndChangeState(level, pos, cbbe);
        return InteractionResult.SUCCESS;
    }

    /**
     * 使用物品右键交互处理
     * <p>
     * 支持以下功能:
     * <ul>
     * <li>未锁定时,按住Shift+右键使用方块进行伪装</li>
     * <li>使用蜜脾涂蜡锁定方块</li>
     * <li>使用斧头刮蜡解锁方块</li>
     * <li>触发伪装方块的陷阱效果</li>
     * </ul>
     * </p>
     *
     * @param stack     使用的物品堆栈
     * @param state     方块状态
     * @param level     游戏世界
     * @param pos       方块位置
     * @param player    玩家
     * @param hand      使用的手
     * @param hitResult 命中结果
     * @return 交互结果
     */
    @Override
    @NonNull
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //super.useItemOn(stack, state, level, pos, player, hand, hitResult);

        if (!(level.getBlockEntity(pos) instanceof CamouflagedBoulderBlockEntity be)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Item item = stack.getItem();
        BlockState mimicState = be.getMimicState();

        //伪装逻辑
        if (player.isShiftKeyDown() && (item instanceof BlockItem blockItem) && !(blockItem.getBlock() instanceof CamouflagedBoulderBlock)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            BlockPlaceContext context = new BlockPlaceContext(level, player, hand, stack, hitResult);
            ((BlockPlaceContextAccessor) context).setReplaceClicked(true);
            BlockState nextMimic = ((BlockItemAccessor) blockItem).callGetPlacementState(context);
            if (nextMimic != null && !be.isLocked() && (mimicState == null || !mimicState.equals(nextMimic))) {
                be.setMimicState(nextMimic);
                level.levelEvent(null, 2001, pos, Block.getId(state));
                level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
                updateAndChangeState(level, pos, be);
                return InteractionResult.SUCCESS;
            }
        }

        //涂蜡逻辑
        if (stack.is(Items.HONEYCOMB) && !be.isLocked()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            be.setLocked(true);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(null, 3003, pos, 0);
            updateAndChangeState(level, pos, be);
            return InteractionResult.SUCCESS;
        }

        //去蜡逻辑
        if (item instanceof AxeItem && be.isLocked()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            be.setLocked(false);
            level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(null, 3004, pos, 0);
            stack.hurtAndBreak(1, (ServerLevel) level, player instanceof ServerPlayer sp ? sp : null, _ -> {
            });
            updateAndChangeState(level, pos, be);
            return InteractionResult.SUCCESS;
        }

        //触发可交互方块陷阱
        if (mimicState != null && !mimicState.isAir() && !stack.is(ModItems.BOULDER_GLOVE)) {//有手套不触发陷阱
            if (this.shouldTriggerTrap(mimicState)) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                this.onRemove(level, mimicState, pos, player);
                updateAndChangeState(level, pos, be);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public InteractionResult useItemOnBlock(ItemStack itemStack, BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hitResult) {

        return InteractionResult.PASS;
    }

    /**
     * 更新方块状态并通知客户端(重载方法)
     *
     * @param level 游戏世界
     * @param pos   方块位置
     * @param be    方块实体
     */
    public static void updateAndChangeState(Level level, BlockPos pos, CamouflagedBoulderBlockEntity be) {
        be.setChanged();

        if (level.isClientSide()) {
            return;
        }
        BlockState blockState = level.getBlockState(pos);
        level.markAndNotifyBlock(pos, level.getChunkAt(pos), blockState, blockState, 3, 512);
    }

    /**
     * 判断伪装方块是否应该触发陷阱
     * <p>
     * 检查以下条件:
     * <ul>
     * <li>是否在手动陷阱触发器标签中</li>
     * <li>是否是按钮、门、活板门、栅栏门等交互性方块</li>
     * <li>是否是箱子或桶容器</li>
     * <li>是否拥有方块实体</li>
     * </ul>
     * </p>
     *
     * @param mimicState 伪装状态
     * @return 是否应该触发陷阱
     */
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

    /**
     * 获取碰撞形状
     *
     * @param state   方块状态
     * @param level   方块访问器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 碰撞形状
     */
    @Override
    @NonNull
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCamouflageState(level, pos).getCollisionShape(level, pos, context);
    }

    /**
     * 获取形状(代理外观交互箱)
     *
     * @param state   方块状态
     * @param level   方块访问器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 形状
     */
    @Override
    @NonNull
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCamouflageState(level, pos).getShape(level, pos, context);
    }

    /**
     * 获取阴影亮度(代理阴影亮度)
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 阴影亮度值
     */
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).getShadeBrightness(level, pos);
    }

    /**
     * 获取视觉形状(代理视觉形状)
     *
     * @param state   方块状态
     * @param level   方块访问器
     * @param pos     方块位置
     * @param context 碰撞上下文
     * @return 视觉形状
     */
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCamouflageState(level, pos).getVisualShape(level, pos, context);
    }

    /**
     * 获取发光等级(代理发光属性)
     *
     * @param state 方块状态
     * @param level 方块访问器
     * @param pos   方块位置
     * @return 发光等级
     */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return getCamouflageState(level, pos).getLightEmission(level, pos);
    }

    /**
     * 获取声音类型(代理音效)
     *
     * @param state  方块状态
     * @param level  关卡读取器
     * @param pos    方块位置
     * @param entity 实体
     * @return 声音类型
     */
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getCamouflageState(level, pos).getSoundType(level, pos, entity);
    }

    /**
     * 生成破坏粒子效果
     *
     * @param level  游戏世界
     * @param player 玩家
     * @param pos    方块位置
     * @param state  方块状态
     */
    @Override
    public void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        ((BlockAccessor) getCamouflageState(level, pos).getBlock()).callSpawnDestroyParticles(level, player, pos, getCamouflageState(level, pos));
    }

    /**
     * 获取渲染形状
     *
     * @param state 方块状态
     * @return 永远返回INVISIBLE,本体不可见
     */
    @Override
    @NonNull
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
