package org.confluence.terraria_boulders.common.entity.boulder;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import org.confluence.terraria_boulders.common.ModDamageTypes;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.init.ModBlocks;
import org.confluence.terraria_boulders.init.ModEntityTypes;
import org.confluence.terraria_boulders.init.ModItems;
import org.confluence.terraria_boulders.util.ModUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Predicate;

public class BoulderEntity extends Projectile {
    public static final BlockState DEFAULTED_BLOCK_STATE = ModBlocks.BOULDER.get().defaultBlockState();
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
    public static final Predicate<Entity> ENTITY_NORMAL = Entity::isAlive;
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
    public Vec3 preMoveVelocity; // 在一刻里面移动前的速度
    //属性：损坏值
    protected float breakLimit = 5.0f;//耐久值，默认5
    protected float breakValue = 0.0f;//损坏度，达到breakLimit后损坏
    protected boolean unbreakable = false;//无限损坏度

    //public boolean pulling = false;//是否正在被拉动
    public boolean interactedWithGlove = false;//是否被手套动过

    public float stepHeightDenominator = 3.0f;//上坡高度分母

    public boolean horizontalCollision;//override父类字段
    public boolean verticalCollision;

    //推巨石
    public double boulderMassFactor = 5.0D;//质量乘数
    public double pushScaleFactor = 0.40D;//形变推力变比系数，数值越小推的越慢

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

    public void onRemove() {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        discard();
        removeEffect(level);
        BlockPos blockPos = blockPosition();
        sendRemoveParticle(level, blockPos);
        playRemoveSound(level, blockPos);
    }

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

    @Override
    public void tick() {
        super.tick();

        if (isRemoved()) return;

        //先施加环境影响（重力+上一刻的摩擦力衰减），不应用重力空中水平移动时测不到地板
        this.applyGravity();
        this.applyFrictionAndRotation();

        //记录AABB移动前的速度
        this.preMoveVelocity = getDeltaMovement();

        //进行移动（AABB会处理穿墙，并将撞墙方向的速度设为 0）
        Vec3 oldPos = this.position();//移动前的位置
        moveAndUpdateNeighbors();
        Vec3 newPos = this.position();//移动后的位置

        //计算实际位移
        Vec3 actualMovement = newPos.subtract(oldPos);

        //实体碰撞检测
        this.hitDetector(newPos.subtract(oldPos));

        //碰撞拦截器：
        //水平碰撞，有真正的X/Z移动意图才算撞墙
        this.horizontalCollision = super.horizontalCollision && (Math.abs(this.preMoveVelocity.x) > 0.001D || Math.abs(this.preMoveVelocity.z) > 0.001D);
        //垂直碰撞：只有速度明显时才是垂直撞击
        //过小阈值拒绝放行
        boolean isSevereVertical = Math.abs(this.preMoveVelocity.y) > 0.08D || Math.abs(actualMovement.y) > 0.08D;
        this.verticalCollision = super.verticalCollision && isSevereVertical;

        // 方块碰撞检测
        if (this.horizontalCollision || this.verticalCollision) {
            Direction hitDir = this.getHitDirection();
            this.onBoulderHitBlock(new BlockHitResult(newPos, hitDir, this.blockPosition(), false));
        }
        // 原版认为撞击了地面，但因没有势能被拦截
        else if (super.verticalCollision && !this.verticalCollision) {

            // 如果是在平地上静止，且有向下的重力假速度（如经典的-0.08）
            if (this.getDeltaMovement().y <= 0.0D) {
                //把Y轴速度和任何微小的水平滑行速度清零
                this.setDeltaMovement(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
            }

            //归零原版坠落高度累积，防止在平地上突然移动
            this.fallDistance = 0.0F;
        }

        //管理生命周期
        updateLifetime();
    }

    private @NonNull Direction getHitDirection() {
        Direction hitDir = Direction.UP; // 兜底方向

        // 根据撞击前的真实速度，推算出到底是撞了哪一面墙/地
        if (this.verticalCollision) {
            // Y轴撞击：往下掉撞地就是 UP（地面朝上），往上飞撞天花板就是 DOWN
            hitDir = this.preMoveVelocity.y > 0 ? Direction.DOWN : Direction.UP;
        } else if (this.horizontalCollision) {
            // X/Z轴撞击：比较X和Z哪个速度大，判定主要撞击面
            if (Math.abs(this.preMoveVelocity.x) > Math.abs(this.preMoveVelocity.z)) {
                hitDir = this.preMoveVelocity.x > 0 ? Direction.WEST : Direction.EAST;
            } else {
                hitDir = this.preMoveVelocity.z > 0 ? Direction.NORTH : Direction.SOUTH;
            }
        }
        return hitDir;
    }

    protected void onBoulderHitBlock(BlockHitResult blockHitResult) {
        Direction direction = blockHitResult.getDirection();

        // 水平撞墙
        if (this.horizontalCollision) {
            this.horizontalHitBlock(blockHitResult, direction);
        }

        // 垂直撞地/天花板
        if (this.verticalCollision) {
            this.verticalHitBlock(blockHitResult, direction);
        }
    }

    //因为原版的检测是射线，而巨石跟弹射物不太一样，并且已经自己实现碰撞检测，所以不依靠原版射线检测
    @Override
    protected final void onHitBlock(BlockHitResult result) {}
    @Override
    protected final void onHitEntity(EntityHitResult result) {}

    // 水平撞墙
    protected void horizontalHitBlock(BlockHitResult blockHitResult, Direction direction) {
        Vec3 postMoveVelocity = getDeltaMovement();
        double newMotionX = postMoveVelocity.x;
        double newMotionZ = postMoveVelocity.z;
        boolean bounced = false;

        //如果移动后X轴速度变小了，说明卡住了X轴东西向的墙
        if (Math.abs(postMoveVelocity.x) <= Math.abs(this.preMoveVelocity.x) - 0.0001) {
            newMotionX = -this.preMoveVelocity.x * bounceFactor;
            bounced = true;
        }

        //如果Z轴速度变小了，说明卡住了Z轴南北向的墙
        //如果是撞到墙角，X 和 Z 会同时成立，实现斜向对角反弹
        if (Math.abs(postMoveVelocity.z) <= Math.abs(this.preMoveVelocity.z) - 0.0001) {
            newMotionZ = -this.preMoveVelocity.z * bounceFactor;
            bounced = true;
        }

        if (bounced) {
            setDeltaMovement(newMotionX, postMoveVelocity.y, newMotionZ);
            //if(!this.pulling) this.breakValue += 1.0f;//拉动时不增加损坏值
            if(!this.interactedWithGlove) this.breakValue += 1.0f;
            playHitBlockSound(level());
        }
    }

    // 垂直落地
    protected void verticalHitBlock(BlockHitResult blockHitResult, Direction direction) {
        Vec3 postMoveVelocity = getDeltaMovement();
        Level level = level();

        // 撞到地面 (Direction.UP)
        if (direction == Direction.UP) {
            // 下落速度够大（防止平地滚动的细微高低差触发跳跃）
            if (this.preMoveVelocity.y < -0.1) {
                // 结算沉重的 Y 轴反弹
                double bounceY = -this.preMoveVelocity.y * bounceFactor;

                // 默认继承原本的水平运动
                double motionX = postMoveVelocity.x;
                double motionZ = postMoveVelocity.z;

                //如果没有水平速度，则弱追踪玩家（检测tick数防止刚出来就乱锁）
                if (getHorizontalVectorLength(this.preMoveVelocity) < 0.0001) {
                    Player nearestPlayer = this.getNearestPlayer();
                    if (nearestPlayer != null) {
                        // 发现玩家，赋予弱追踪的水平初速度
                        Vec3 toPlayer = nearestPlayer.position().subtract(this.position());
                        // 抹除Y轴，只取水平方向并乘上巨石的 speed
                        Vec3 horizontalToPlayer = new Vec3(toPlayer.x, 0, toPlayer.z).normalize().scale(speed);

                        motionX = horizontalToPlayer.x;
                        motionZ = horizontalToPlayer.z;

                        // 对着玩家滚过去
                        this.setYRot((float) (Mth.atan2(motionX, motionZ) * Mth.RAD_TO_DEG));
                        this.yRotO = this.getYRot();

                    } else //被手套放下的巨石有一个免疫期，这个期间不会触发随机弹跳
                        if (!level.isClientSide() && !(this.interactedWithGlove && this.stillTickCount < this.maxStillTick)) {
                        // 没有玩家，随机弹跳
                        Vec3 pos = this.position();
                        Vec3 validMotion = null;

                        //射线探路尝试次数，找到最空旷的地方滚
                        int attempts = 8;
                        for (int i = 0; i < attempts; i++) {
                            //在0到360之间生成一个弧度
                            float randomYaw = this.random.nextFloat() * Mth.TWO_PI;

                            //将弧度转换为水平移动的单位向量
                            double dirX = -Mth.sin(randomYaw);
                            double dirZ = Mth.cos(randomYaw);

                            //探测距离须大于等于巨石自身的直径，防止体型较大的巨石穿模算错
                            double probeDistance = Math.max(1.5, this.radius * 2.5F);
                            Vec3 targetProbePos = pos.add(dirX * probeDistance, 0, dirZ * probeDistance);

                            //发射一条射线，看看这个角度会不会撞墙
                            BlockHitResult clip = level.clip(new ClipContext(pos, targetProbePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

                            //找到了
                            if (clip.getType() == HitResult.Type.MISS) {
                                validMotion = new Vec3(dirX * speed, 0, dirZ * speed);
                                break;//找到就退出循环
                            }
                        }

                        //如果8随机未找到
                        if (validMotion == null) {
                            //全随机转动
                            float fallbackYaw = this.random.nextFloat() * Mth.TWO_PI;
                            validMotion = new Vec3(-Mth.sin(fallbackYaw) * speed, 0, Mth.cos(fallbackYaw) * speed);
                        }

                        //应用速度
                        motionX = validMotion.x;
                        motionZ = validMotion.z;

                        //更新巨石朝向和渲染旋转角
                        this.setYRot((float) (Mth.atan2(motionX, motionZ) * Mth.RAD_TO_DEG));
                        this.yRotO = this.getYRot();
                    }
                }

                // 将水平 AI 速度与 Y 轴物理反弹一起赋予巨石
                setDeltaMovement(motionX, bounceY, motionZ);

                this.fallDistance = 0;
                playHitBlockSound(level);
            }
        }
        // 撞到天花板 (Direction.DOWN)
        else if (direction == Direction.DOWN) {
            if (this.preMoveVelocity.y > 0) {
                setDeltaMovement(postMoveVelocity.x, -this.preMoveVelocity.y * bounceFactor, postMoveVelocity.z);
            }
        }
    }

    protected void rotate(Vec3 deltaMovement) {
        float s = (float) deltaMovement.length();
        if (onGround()) {
            s = new Vec2((float) deltaMovement.x, (float) deltaMovement.z).length();
        }
        float r = s / radius;
        if (rotate > Mth.TWO_PI) this.rotate -= Mth.TWO_PI;
        this.rotateO = rotate;
        this.rotate += r;
    }

    //碰撞检测器
    protected void hitDetector(Vec3 actualMovement) {
        //使用预期的物理速度防止刚体造成的一些问题
        Vec3 damageVelocity = this.preMoveVelocity != null ? this.preMoveVelocity : actualMovement;
        double speed = damageVelocity.length();
        //有速度或旋转时才有伤害
        boolean hasDamage = (speed > 0.05D) || !ModUtils.MathUtil.equal(this.rotateO - this.rotate, 0);

        //贴合实际预期运动轨迹的扫描框
        AABB sweepBox = this.getBoundingBox().expandTowards(-damageVelocity.x, -damageVelocity.y, -damageVelocity.z).inflate(0.05D);
        // 还原出移动前的位置，用于精确计算相对方向
        Vec3 oldPos = this.position().subtract(actualMovement);

        // 扫描轨迹上所有实体
        for (Entity entity : this.level().getEntities(this, sweepBox, ENTITY_NORMAL)) {
            if (entity == this) continue;

            //获取从巨石中心指向实体的平面方向向量
            Vec3 toEntity = entity.position().subtract(oldPos);
            toEntity = new Vec3(toEntity.x, 0, toEntity.z).normalize();

            //判断是谁撞谁，自己碰巨石不算伤害
            Vec3 pushNormal = new Vec3(this.getX() - entity.getX(), 0, this.getZ() - entity.getZ()).normalize();
            //巨石预期运动方向是否与玩家推的方向相同，点积>0.7D说明巨石此时正在顺着玩家推的方向前进
            boolean isBeingPushedForward = speed > 0.001D && damageVelocity.normalize().dot(pushNormal) > 0.7D;
            //计算正面碰撞，防除0
            double dotProduct = speed > 0.001D ? damageVelocity.normalize().dot(toEntity) : 0.0D;//巨石完全静止为0

            //向量点积大于90度视为正面碰撞
            if (dotProduct > 0.0D) {
                if (hasDamage && ModUtils.EntityUtil.isSurvivalOrMob(entity) && !isBeingPushedForward) {
                    // 触发正面碰撞事件（造成碾压伤害）
                    this.onBoulderHitEntity(new EntityHitResult(entity));
                }
            }
            else {
                //非正面接触尝试推动
                if (entity instanceof LivingEntity livingEntity) {
                    this.onPushBoulder(livingEntity);
                }
            }
        }
    }

    protected void onBoulderHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        UUID uuid = entity.getUUID();

        // TODO 需要重写
        int i = hitHistory.containsKey(uuid) ? hitHistory.addTo(uuid, -1) : 0;
        if (i > 0) {
            return;
        }

        float damage = this.getDamage(entityHitResult);
        if(damage >= 0.0F){
            entity.hurt(ModDamageTypes.of(entity.level(), ModDamageTypes.BOULDER, this), damage);
        } else{
            //（未来可能出一个治愈巨石？）
        }

        hitHistory.put(uuid, 5);
    }

    /**
     * 推巨石
     */
    protected void onPushBoulder(LivingEntity entity) {
        if (entity.level().isClientSide() && !(entity instanceof Player)) return;

        //有手套即可
        if (entity.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.BOULDER_GLOVE) || entity.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.BOULDER_GLOVE)) {

            //this.interactedWithGlove = true;

            //基础物理属性
            double m = 1.0;//玩家质量
            double M = Math.max(0.5, this.radius * 2.0 * this.boulderMassFactor);//巨石质量
            double e = 0.5;//恢复系数e（0.0~1.0），石头弹性低，钝击感强

            //获取双方AABB
            AABB boulderBox = this.getBoundingBox();
            AABB entityBox = entity.getBoundingBox();

            //计算重叠相交，模拟形变
            AABB intersect = boulderBox.intersect(entityBox);

            //没碰上或未发生形变
            if (intersect.getXsize() <= 0.0 && intersect.getZsize() <= 0.0) return;

            //获取平面法向量，从生物中心指向巨石中心
            Vec3 normal = this.position().subtract(entity.position());
            normal = new Vec3(normal.x, 0, normal.z).normalize();

            //利用挤压形变的轴向投影，推导出碰撞方向上的穿透深度
            double overlapX = intersect.getXsize();
            double overlapZ = intersect.getZsize();

            //计算速度
            Vec3 boulderVel = this.getDeltaMovement();//巨石速度向量
            double V0n = boulderVel.x * normal.x + boulderVel.z * normal.z;
            //将重合体积（形变）折算为法向等效运动速度标量
            double v0n = Math.max(overlapX, overlapZ) * this.pushScaleFactor;

            //生物推力小于石头速度，说明石头快
            if (v0n <= V0n) return;

            //速度继承与质量衰减模型，巨石继承大部分速度，巨石质量M仅作为衰减系数
            double massFactor = m / (m + M * 0.3);

            //一维弹性碰撞公式
            double Vn = V0n + (v0n - V0n) * (1.0 + e) * 0.5 * massFactor;

            //还原为三维向量
            Vec3 newBoulderVel = boulderVel.add(normal.scale(Vn - V0n));

            //应用计算结果
            this.setDeltaMovement(newBoulderVel);

            //跨端同步
            if (this.level() instanceof ServerLevel serverLevel) {
                ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(this);
                serverLevel.getChunkSource().sendToTrackingPlayers(this, packet);
            }
        }
    }

    protected void moveAndUpdateNeighbors() {
        Vec3 deltaMovement = getDeltaMovement();
        setYRot((float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * Mth.RAD_TO_DEG));

        move(MoverType.SELF, deltaMovement);

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

    protected void applyFrictionAndRotation() {
        //衰减速度
        Vec3 deltaMovement = getDeltaMovement();
        Vec3 deltaMovementNew = new Vec3(deltaMovement.x * 0.99f, deltaMovement.y, deltaMovement.z * 0.99f);
        setDeltaMovement(deltaMovementNew);

        //更新旋转弧度
        rotate(deltaMovementNew);
    }

    //管理生命周期
    protected void updateLifetime() {
        //正在被拉动的巨石无视生命周期
//        if(this.pulling) {
//            this.stillTickCount = 0;   //不触发maxStillTick判定
//            //this.tickCount = 0;        //冻结生长时间，防止触发maxRemoveTick判定
//            return;
//        }

        //计时器
        double currentSpeed = getDeltaMovement().length();
        if (currentSpeed < minRemoveSpeed) {
            stillTickCount++;
        } else {
            stillTickCount = 0;
        }

        //被手套交互过的巨石无视生命周期
        if(this.interactedWithGlove) return;

        //检查是否超时或静止太久
        if (tickCount >= maxRemoveTick || currentSpeed < minRemoveSpeed && stillTickCount == maxStillTick) {
            onRemove();
            return;
        }

        //检查是否已损坏
        if (!this.unbreakable && this.breakValue >= this.breakLimit){
            this.onRemove();
        }
    }

    //碰撞箱是否相交（原版playerTouch检测没碰到就触发）
//    public boolean hasHitboxTouch(/*BoulderEntity boulder, */LivingEntity toucher) {
//        return this.getBoundingBox().inflate(0.05D).intersects(toucher.getBoundingBox());
//    }

    //刚体化
    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return this.isAlive();
    }

//    @Override
//    public void playerTouch(Player player) {
//        super.playerTouch(player);
//        if (!this.level().isClientSide() && this.isAlive() && this.hasHitboxTouch(player)) {
//            //用手套触发推石头逻辑
//            this.onPushBoulder(player);
//        }
//    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    protected void playHitBlockSound(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, blockPosition(), getBlockState().getSoundType().getFallSound(), SoundSource.BLOCKS, 5.0F, 1.0F);
    }

    public float getDamage(EntityHitResult entityHitResult) {
        return 100.0F * Mth.clamp((float) getDeltaMovement().length() * 3.0F, 0.0F, 1.0F);
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
        //noinspection SuspiciousNameCombination
        setYRot((float) (Mth.atan2(vec3.x, vec3.z) * Mth.RAD_TO_DEG));
        setDeltaMovement(vec3.scale(speed));
        this.yRotO = getYRot();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public float maxUpStep() {
        return this.radius * 2.0f / this.stepHeightDenominator;//可以上自己1/3大小的坡
    }

    @Override
    public boolean isPickable() {
        return true;//开启鼠标选中/右键检测权
    }

    public BlockState getBlockState() {
        return entityData.get(DATA_BLOCK_STATE);
    }

    public EntityDataAccessor<BlockState> getBlockStateAccessor() {
        return DATA_BLOCK_STATE;
    }

    //统一设置方块状态
    public void setBlockState(BlockState state) {
        //noinspection ConstantValue
        if (state == null || state.getBlock() == null) {
            state = DEFAULTED_BLOCK_STATE;
        }
        this.entityData.set(DATA_BLOCK_STATE, state);
    }

    public float getBreakLimit() {return breakLimit;}

    public void setBreakLimit(float breakLimit) {this.breakLimit = breakLimit;}

    public float getBreakValue() {return breakValue;}

    public void setBreakValue(float breakValue) {this.breakValue = breakValue;}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, DEFAULTED_BLOCK_STATE);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("BlockState", BlockState.CODEC).ifPresent(state -> entityData.set(DATA_BLOCK_STATE, state));
        // 兜底：读取到的 BlockState 可能为空（旧存档/损坏数据）
        BlockState current = entityData.get(DATA_BLOCK_STATE);
        //noinspection ConstantValue
        if (current == null || current.getBlock() == null) {
            entityData.set(DATA_BLOCK_STATE, DEFAULTED_BLOCK_STATE);
        }
        tickCount = input.getIntOr("Age", 0);
        stillTickCount = input.getIntOr("StillAge", 0);

        radius = input.getFloatOr("Radius", 0.5F);
        maxRemoveTick = input.getIntOr("MaxRemoveTick", 1200);
        maxStillTick = input.getIntOr("MaxStillTick", 20);
        speed = input.getDoubleOr("Speed", 0.7);
        minRemoveSpeed = input.getDoubleOr("MinRemoveSpeed", 0.007);
        generation = input.getIntOr("Generation", 0);
        this.breakValue = input.getFloatOr("DamageValue", 0.0F);
        this.breakLimit = input.getFloatOr("Durability", 5.0F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        BlockState state = entityData.get(DATA_BLOCK_STATE);
        //noinspection ConstantValue
        if (state == null || state.getBlock() == null) {
            state = DEFAULTED_BLOCK_STATE;
        }
        output.store("BlockState", BlockState.CODEC, state);
        output.putInt("Age", tickCount);
        output.putInt("StillAge", stillTickCount);
        output.putFloat("Radius", radius);
        output.putInt("MaxRemoveTick", maxRemoveTick);
        output.putInt("MaxStillTick", maxStillTick);
        output.putDouble("Speed", speed);
        output.putDouble("MinRemoveSpeed", minRemoveSpeed);
        output.putInt("Generation", generation);
        output.putFloat("DamageValue", breakValue);
        output.putFloat("Durability", breakLimit);
    }
}
