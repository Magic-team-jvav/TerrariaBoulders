package org.confluence.terraria_boulders.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.block.boulder.*;
import org.confluence.terraria_boulders.common.entity.boulder.*;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(TerrariaBoulders.ID);

    public static final DeferredBlock<BoulderBlock> BOULDER = register("boulder",
            BoulderBlock::new);
    public static final DeferredBlock<FullCollisionBoulderBlock> OAK_LOG_BOULDER = register("oak_log_boulder",
            FullCollisionBoulderBlock::new);
    public static final DeferredBlock<BoulderBlock> FOLLOWER_BOULDER = register("follower_boulder", p ->
            new BoulderBlock(p, FollowerBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> EXPLODE_BOULDER = register("explode_boulder", p ->
            new BoulderBlock(p, ExplodeBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> ROLLING_CACTUS_BOULDER = register("rolling_cactus_boulder", p ->
            new ContactEffectBoulderBlock(p, RollingCactusBoulderEntity::new, ContactEffectBoulderBlock.ContactEffect.createHurt(
                    (entity) -> entity instanceof Player ? 19.0F : 1.5F,
                    (level) -> level.damageSources().cactus())));
    public static final DeferredBlock<BoulderBlock> BOUNCY_BOULDER = register("bouncy_boulder", p ->
            new BoulderBlock(p, BouncyBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> GHOULDER = register("ghoulder", p ->
            new BoulderBlock(p, GhoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> LAVA_BOULDER = register("lava_boulder", p ->
            new BoulderBlock(p, LavaBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> SPIDER_BOULDER = register("spider_boulder", p ->
            new BoulderBlock(p, SpiderBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> RAINBOW_BOULDER = register("rainbow_boulder", p ->
            new BoulderBlock(p, RainbowBoulderEntity::new));

    public static final DeferredBlock<BoulderBreadBlock> BOULDER_BREAD_BLOCK = register("boulder_bread_block", BoulderBreadBlock::new);
    //伪装巨石
//    public static final DeferredBlock<CamouflagedBoulderBlock> CAMOUFLAGED_BOULDER = register("camouflaged_boulder",
//            (props) -> new CamouflagedBoulderBlock(props.noOcclusion())
//    );
    public static final DeferredBlock<CamouflagedBoulderBlock> CAMOUFLAGED_BOULDER = register("camouflaged_boulder", CamouflagedBoulderBlock::new);

    private static <B extends Block> DeferredBlock<B> register(String id, Function<BlockBehaviour.Properties, B> supplier) {
        return REGISTER.registerBlock(id, supplier);
    }

    /// 基于黑曜石的爆炸抗性，汇流来世的方块设置爆炸抗性时，应当使用这个方法
    /// 对于泰拉爆炸，小于黑曜石爆炸抗性的方块都会被炸掉
    ///
    /// @param delta 偏差值
    /// @return 爆炸抗性
    @SuppressWarnings("deprecation")
    public static float getObsidianBasedExplosionResistance(float delta) {
        return Blocks.OBSIDIAN.getExplosionResistance() + delta;
    }
}
