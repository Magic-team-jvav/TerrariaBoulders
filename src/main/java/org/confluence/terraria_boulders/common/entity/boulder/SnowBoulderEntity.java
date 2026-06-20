package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.init.ModEntityTypes;

public class SnowBoulderEntity extends BoulderEntity {
    private BlockPos lastStayPos = null;//记录刚刚留下/吸收顶层雪的坐标，否则多刻停在一个雪片上会抽搐
    private final float reduceRadius = 0.005f;//减少多少半径
    //专门用于在网络同步半径的频道
    private static final EntityDataAccessor<Float> SYNCED_RADIUS = SynchedEntityData.defineId(SnowBoulderEntity.class, EntityDataSerializers.FLOAT);

    //给注册用的构造函数
    public SnowBoulderEntity(EntityType<? extends BoulderEntity> type, Level level) {
        super(type, level);
        this.infiniteDurability = true;
        this.maxRemoveTick = 4000;
    }

    //给factory用的构造函数
    public SnowBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.SNOWY_BOULDER.get(), level, pos, blockState);
        this.infiniteDurability = true;
        this.maxRemoveTick = 4000;
    }

    //雪球巨石没有伤害
    @Override
    public float getDamage(EntityHitResult result){
        return 0.0F;
    }

    //动态碰撞箱
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float size = this.radius * 2.0F;
        return EntityDimensions.scalable(size, size);
    }

    //产生会减少，吸收的话会增加大小
    @Override
    public void tick() {
        super.tick();
        BlockPos pos = this.blockPosition();//当前位置
        Level level = this.level();

        //滚动的时候会持续产生雪片，如果地上有就会吸收
        if(!level.isClientSide()) {
            //产生
            if (level.isEmptyBlock(pos) && level.getBlockState(pos.below()).isSolid()) {
                if(this.lastStayPos == null || !this.lastStayPos.equals(pos)) {//没有滚过方块/下面的方块不是刚滚过的方块
                    this.lastStayPos = pos;//更新缓存坐标
                    this.radius -= this.reduceRadius;//吸收和产生会增减大小
                    this.entityData.set(SYNCED_RADIUS, this.radius);//推送到客户端
                    level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
                    this.refreshDimensions();
                }
            }//吸收
            else if(level.getBlockState(pos).is(Blocks.SNOW)) {
                //防止几刻内在一个方块上反复吸收产生抽搐
                if(this.lastStayPos == null || !this.lastStayPos.equals(pos)) {
                    int snowLayers = level.getBlockState(pos).getValue(SnowLayerBlock.LAYERS);
                    this.lastStayPos = pos;//更新缓存坐标
                    this.radius += this.reduceRadius * (float)snowLayers;//吸收和产生会增减大小，雪的层数越多增加的越多
                    this.entityData.set(SYNCED_RADIUS, this.radius);//推送到客户端
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    this.refreshDimensions();
                }
            }
        }
        else{
            //在后面加雪花粒子
            level.addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0, 0.1, 0);
        }

        //管理声明周期
        if(radius <= 0.0F) this.onRemove();
    }

    @Override
    public void onBoulderHitEntity(EntityHitResult result){
        super.onBoulderHitEntity(result);
        if(this.level().isClientSide()) return;

        //冰冻+击飞
        if (result.getEntity() instanceof LivingEntity living) {
            //冰冻
            int radomTicks = living.getRandom().nextInt(Math.max(10, (int)this.radius * 2 * 20));//(0~直径)秒（最低随机0~半秒）
            int frozenTicks = 100 + (living.getRandom().nextBoolean() ? radomTicks : -radomTicks);//5±(0~直径)秒冰冻
            living.setTicksFrozen(living.getTicksFrozen() + frozenTicks);

            //击飞
            //计算巨石冲向实体的水平方向，实体坐标减去巨石的坐标，取XZ的水平归一化（向量长1）
            double diffX = living.getX() - this.getX();
            double diffZ = living.getZ() - this.getZ();

            //防止重合导致除0错误，加一个微小的随机偏置
            if (diffX == 0.0 && diffZ == 0.0) {
                diffX = (this.random.nextDouble() - this.random.nextDouble()) * 0.01;
                diffZ = (this.random.nextDouble() - this.random.nextDouble()) * 0.01;
            }

            //基于巨石半径计算击退强度
            float baseKnockbackStrength = 1.0F;//基础为1
            float knockbackStrength = baseKnockbackStrength + (this.radius * 0.5F);

            //击退
            living.knockback(knockbackStrength, -diffX, -diffZ);//参数：击退强度 X轴方向 Z轴方向

            //同步客户端（）游戏不会自动在服务端更改被撞击ServerPlayer运动状态，发包把运动向量发到客户端）
            if (living instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SYNCED_RADIUS, 0.5F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (SYNCED_RADIUS.equals(key)) {
            //客户端覆盖本地字段
            this.radius = this.entityData.get(SYNCED_RADIUS);
            //客户端手动刷新碰撞箱
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }
}
