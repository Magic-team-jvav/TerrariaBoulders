package org.confluence.terraria_boulders.common.entity.boulder;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.confluence.terraria_boulders.common.ModDamageTypes;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.confluence.terraria_boulders.util.VectorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BoulderEntity extends Projectile {
    public static final float SEARCH_RANGE = 31.5F;
    public static final Predicate<Entity> ENTITY_PREDICATE = entity -> {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    };
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(BoulderEntity.class, EntityDataSerializers.BLOCK_STATE);
    private final Object2IntOpenHashMap<UUID> hitHistory = new Object2IntOpenHashMap<>();

    public float rotateO = 0.0F;
    public float rotate = 0.0F;

    // 可修改参数
    public float radius = 0.5F; // 半径
    public int maxRemoveTick = 1200; // 最大移除时间
    public int maxStillTick = 20; // 最大静止时间
    public double speed = 0.7; // 速度
    public double minRemoveSpeed = 0.007; // 最小移除速度
    public double bounceFactor = 0.3;
    public double frictionFactor = 0.9;
    public int generation = 0; // 分裂代数，0为原始巨石

    public int stillTickCount; // 静止刻计时
    Vec3 preMoveVelocity; // 在一刻里面移动前的速度

    public BoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        this(ModEntityTypes.BOULDER.get(), level, pos, blockState);
    }

    public BoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level, Vec3 pos, BlockState blockState) {
        super(entityType, level);
        setPos(pos);
        entityData.set(DATA_BLOCK_STATE, blockState);
    }

    protected static double getHorizontalVectorLength(Vec3 deltaMovement) {
        return Math.sqrt(deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z);
    }

    public BlockState getBlockState() {
        return entityData.get(DATA_BLOCK_STATE);
    }

    public void onRemove() {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        removeEffect(level);
        BlockPos blockPos = blockPosition();
        sendRemoveParticle(level, blockPos);
        playRemoveSound(level, blockPos);
        discard();
    }

    /// 移除前触发的效果
    protected void removeEffect(ServerLevel serverLevel) {
    }

    protected void sendRemoveParticle(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, getBlockState(), pos), getX(), getY() + radius, getZ(), 175, 0.0, 0.0, 0.0, 0.15);
    }

    protected void playRemoveSound(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.playSound(null, pos, getBlockState()
                .getSoundType(serverLevel, pos, this)
                .getBreakSound(), SoundSource.BLOCKS, 5.0F, 1.0F);
    }

    /**已重写*/
    @Override
    public void tick() {
        super.tick();

        //先施加重力，不然空中水平移动时测不到地板
        applyGravity();

        //AABB移动前的速度
        this.preMoveVelocity = getDeltaMovement();

        //进行移动（AABB会处理穿墙，并将撞墙方向的速度设为 0）
        Vec3 oldPos = this.position();//移动前的位置
        moveAndUpdateNeighbors();
        Vec3 newPos = this.position();//移动后的位置

        //方块碰撞检测
        Vec3 postMoveVelocity = getDeltaMovement();
        boolean hitX = Math.abs(postMoveVelocity.x) < Math.abs(preMoveVelocity.x) - 1.0E-5;
        boolean hitY = Math.abs(postMoveVelocity.y) < Math.abs(preMoveVelocity.y) - 1.0E-5;
        boolean hitZ = Math.abs(postMoveVelocity.z) < Math.abs(preMoveVelocity.z) - 1.0E-5;

        //分发撞击事件
        if (hitX) onHitBlock(new BlockHitResult(newPos, preMoveVelocity.x > 0 ? Direction.WEST : Direction.EAST, this.blockPosition(), false));
        if (hitY) onHitBlock(new BlockHitResult(newPos, preMoveVelocity.y > 0 ? Direction.DOWN : Direction.UP, this.blockPosition(), false));
        if (hitZ) onHitBlock(new BlockHitResult(newPos, preMoveVelocity.z > 0 ? Direction.NORTH : Direction.SOUTH, this.blockPosition(), false));

        //计算实体碰撞
        onHit(newPos.subtract(oldPos));

        //摩擦力、旋转
        applyFrictionAndRotation();

        //管理生命周期
        updateLifetime();
    }

    protected void rotate(Vec3 deltaMovement) {
        float s = (float) deltaMovement.length();
        float r = s / radius;
        if (rotate > Mth.TWO_PI) this.rotate -= Mth.TWO_PI;
        this.rotateO = rotate;
        this.rotate += r;
    }

    protected void onHit(Vec3 deltaMovement) {
        double actualSpeed = deltaMovement.length();

        //移动或滚动才触发伤害
        if (actualSpeed > 0.05D || this.rotateO - this.rotate != 0) {
            //贴合实际运动轨迹的扫描框
            AABB sweepBox = this.getBoundingBox().expandTowards(-deltaMovement.x, -deltaMovement.y, -deltaMovement.z)/*.inflate(0.01D)*/;

            //还原出移动前的位置，用于精确计算相对方向
            Vec3 oldPos = this.position().subtract(deltaMovement);

            //扫描轨迹上所有实体
            for (Entity entity : level().getEntities(this, sweepBox, ENTITY_PREDICATE)) {

                //只对巨石前方的实体造成伤害
                Vec3 toEntity = entity.position().subtract(oldPos).normalize();

                if (deltaMovement.normalize().dot(toEntity) > 0) {
                    onHitEntity(new EntityHitResult(entity));//造成伤害
                }
            }
        }
    }

    protected void moveAndUpdateNeighbors() {
        Vec3 deltaMovement = getDeltaMovement();
        setYRot((float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * Mth.RAD_TO_DEG));
        //applyGravity();

        //deltaMovement = getDeltaMovement();
        move(MoverType.SELF, deltaMovement);

        if (level().isClientSide()) return;

        Vec3 motion = getDeltaMovement();
        if (motion.x != deltaMovement.x || motion.y != deltaMovement.y || motion.z != deltaMovement.z) {
            updateNeighbors();
        }
    }

    protected void updateNeighbors() {
        for (Direction dir : Direction.values()) {
            BlockPos blockPos = blockPosition().relative(dir);
            BlockState blockState = level().getBlockState(blockPos);
            if (blockState.getBlock() instanceof BoulderBlock block) {
                block.onProjectileHit(level(), blockState, new BlockHitResult(blockPos.getCenter(), dir, blockPos, false), this);
            }
        }
    }

    private void checkBlockCollisionPrediction() {
        Vec3 start = position();
        Vec3 intendedMove = getDeltaMovement();

        //扫描方块，下一帧会不会撞墙
        HitResult blockHit = level().clip(new ClipContext(start, start.add(intendedMove), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (blockHit.getType() != HitResult.Type.MISS) {
            onHitBlock((BlockHitResult) blockHit);
        }
    }

    private void applyFrictionAndRotation() {
        //衰减速度
        Vec3 deltaMovement = getDeltaMovement().scale(0.99);
        setDeltaMovement(deltaMovement);

        //更新旋转弧度
        rotate(deltaMovement);
    }

    //管理生命周期
    protected void updateLifetime() {
        double currentSpeed = getDeltaMovement().length();

        //检查是否超时或静止太久
        if (tickCount >= maxRemoveTick || currentSpeed < minRemoveSpeed && stillTickCount == maxStillTick) {
            onRemove();
            return;
        }

        if (currentSpeed < minRemoveSpeed) {
            stillTickCount++;
        } else {
            stillTickCount = 0;
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        Direction direction = blockHitResult.getDirection();
        if (direction.getAxis() == Direction.Axis.Y) {
            verticalHitBlock(blockHitResult, direction);
        } else {
            horizontalHitBlock(blockHitResult, direction);
        }
        if (level() instanceof ServerLevel serverLevel) {
            playHitBlockSound(serverLevel);
        }
    }

    protected void playHitBlockSound(ServerLevel serverLevel) {
        serverLevel.playSound(null, blockPosition(), getBlockState().getSoundType().getFallSound(), SoundSource.BLOCKS, 5.0F, 1.0F);
    }

    protected void horizontalHitBlock(BlockHitResult blockHitResult, Direction direction) {
        onRemove();
    }

    protected void verticalHitBlock(BlockHitResult blockHitResult, Direction direction) {
        Level level = level();
        if (direction != Direction.UP) {
            return;
        }

        // 如果水平速度几乎为零则尝试添加水平向量
        if (getHorizontalVectorLength(getDeltaMovement()) < 0.0001) {
            // 先尝试获取最近的目标
            Player nearestPlayer = getNearestPlayer();
            if (nearestPlayer == null) {
                // 这里仅在服务端处理因为客户端的随机有可能于服务器的随机不同导致出现问题
                if (!level.isClientSide()) {
                    List<Direction> directions = new ArrayList<>();
                    for (Direction direction1 : Direction.Plane.HORIZONTAL) {
                        Vec3 position = position();
                        BlockHitResult clip = level.clip(new ClipContext(position, position.relative(direction1, 1), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                        if (clip.getType() == HitResult.Type.MISS) {
                            directions.add(direction1);
                        }
                    }
                    int directionsSize = directions.size();
                    if (!directions.isEmpty()) {
                        Direction direction1 = directions.get(directionsSize == 1 ? 0 : getRandom().nextIntBetweenInclusive(0, directionsSize - 1));
                        setDeltaMovement(getDeltaMovement().relative(direction1, 1).scale(speed));
                    }
                }
            } else {
                targetTo(nearestPlayer);
            }
        }

        verticalHitRebound(blockHitResult, direction);
    }

    protected void verticalHitRebound(BlockHitResult blockHitResult, Direction direction) {
        if (fallDistance > 5) {
            Vec3 motion = VectorUtils.relativeScale(getDeltaMovement(), blockHitResult.getDirection().getAxis(), -bounceFactor);
            if (Math.abs(motion.y) < 0.03) motion = new Vec3(motion.x, 0.0, motion.z);
            setDeltaMovement(motion.scale(frictionFactor));
            super.onHitBlock(blockHitResult);
            fallDistance = 0;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        UUID uuid1 = entity.getUUID();

        // TODO 需要重写
        int i = hitHistory.containsKey(uuid1) ? hitHistory.addTo(uuid1, -1) : 0;
        if (i <= 0) {
            entity.hurt(ModDamageTypes.of(entity.level(), ModDamageTypes.BOULDER, this), 100.0F);
            hitHistory.put(uuid1, 5);
        }
    }

    public void targetToPlayer() {
        targetTo(getNearestPlayer());
    }

    protected @Nullable Player getNearestPlayer() {
        return level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), SEARCH_RANGE, ENTITY_PREDICATE);
    }

    public void targetTo(@Nullable Entity entity) {
        Vec3 deltaMovement = getDeltaMovement();
        Vec3 vec3 = entity == null ? deltaMovement : entity.position().subtract(position());
        vec3 = new Vec3(vec3.x, deltaMovement.y, vec3.z).normalize();
        setYRot((float) (Mth.atan2(vec3.x, vec3.z) * Mth.RAD_TO_DEG));
        setDeltaMovement(vec3.scale(speed));
        this.yRotO = getYRot();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    //统一设置方块状态
    public void setBlockState(BlockState state) {
        this.entityData.set(DATA_BLOCK_STATE, state);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, ModBlocks.BOULDER.get().defaultBlockState());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("BlockState", BlockState.CODEC).ifPresent(state -> entityData.set(DATA_BLOCK_STATE, state));
        tickCount = input.getIntOr("Age", 0);
        stillTickCount = input.getIntOr("StillAge", 0);

        radius = input.getFloatOr("Radius", 0.5F);
        maxRemoveTick = input.getIntOr("MaxRemoveTick", 1200);
        maxStillTick = input.getIntOr("MaxStillTick", 20);
        speed = input.getDoubleOr("Speed", 0.7);
        minRemoveSpeed = input.getDoubleOr("MinRemoveSpeed", 0.007);
        generation = input.getIntOr("Generation", 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("BlockState", BlockState.CODEC, entityData.get(DATA_BLOCK_STATE));
        output.putInt("Age", tickCount);
        output.putInt("StillAge", stillTickCount);
        output.putFloat("Radius", radius);
        output.putInt("MaxRemoveTick", maxRemoveTick);
        output.putInt("MaxStillTick", maxStillTick);
        output.putDouble("Speed", speed);
        output.putDouble("MinRemoveSpeed", minRemoveSpeed);
        output.putInt("Generation", generation);
    }
}
