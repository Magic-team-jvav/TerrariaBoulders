package org.confluence.terraria_boulders.init;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBlock;
import org.confluence.terraria_boulders.common.block.boulder.BoulderBreadBlock;
import org.confluence.terraria_boulders.common.block.boulder.ContactEffectBoulderBlock;
import org.confluence.terraria_boulders.common.block.boulder.FullCollisionBoulderBlock;
import org.confluence.terraria_boulders.common.entity.boulder.*;
import xiaojin.terraria_boulders.common.block.boulder.*;
import xiaojin.terraria_boulders.common.entity.boulder.*;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(TerrariaBoulders.ID);

    public static final DeferredBlock<BoulderBlock> BOULDER = registerWithItem("boulder",
            BoulderBlock::new);
    public static final DeferredBlock<FullCollisionBoulderBlock> OAK_LOG_BOULDER = registerWithItem("oak_log_boulder",
            FullCollisionBoulderBlock::new);
    public static final DeferredBlock<BoulderBlock> FOLLOWER_BOULDER = registerWithItem("follower_boulder", () ->
            new BoulderBlock(FollowerBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> EXPLODE_BOULDER = registerWithItem("explode_boulder", () ->
            new BoulderBlock(ExplodeBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> ROLLING_CACTUS_BOULDER = registerWithItem("rolling_cactus_boulder", () ->
            new ContactEffectBoulderBlock(RollingCactusBoulderEntity::new, ContactEffectBoulderBlock.ContactEffect.createHurt(
                    (entity) -> entity instanceof Player ? 19.0F : 1.5F,
                    (level) -> level.damageSources().cactus())));
    public static final DeferredBlock<BoulderBlock> BOUNCY_BOULDER = registerWithItem("bouncy_boulder", () ->
            new BoulderBlock(BouncyBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> GHOULDER = registerWithItem("ghoulder", () ->
            new BoulderBlock(GhoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> LAVA_BOULDER = registerWithItem("lava_boulder", () ->
            new BoulderBlock(LavaBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> SPIDER_BOULDER = registerWithItem("spider_boulder", () ->
            new BoulderBlock(SpiderBoulderEntity::new));
    public static final DeferredBlock<BoulderBlock> RAINBOW_BOULDER = registerWithItem("rainbow_boulder", () ->
            new BoulderBlock(RainbowBoulderEntity::new));

    public static final DeferredBlock<BoulderBreadBlock> BOULDER_BREAD_BLOCK = registerWithItem("boulder_bread_block", BoulderBreadBlock::new);

    private static <B extends Block> DeferredBlock<B> register(String id, Supplier<B> supplier) {
        DeferredBlock<B> block = REGISTER.register(id, supplier);
        return block;
    }

    private static <B extends Block> DeferredBlock<B> registerWithItem(String id, Supplier<B> supplier) {
        DeferredBlock<B> block = REGISTER.register(id, supplier);
        ModItems.REGISTER.registerSimpleBlockItem(block);
        return block;
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
