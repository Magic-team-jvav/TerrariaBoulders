package org.confluence.terraria_boulders.events.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 修复手持物品潜行状态下无法触发useItem方法的问题。
 * 得益于新版Minecraft的设计，你无法在潜行时触发useItemOn，并没有接口能解除这一限制，故手动实现本接口。
 * */
public interface IUseItemOnBlock {
    default InteractionResult useItemOnBlock(ItemStack stack, BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //玩家为空或者没有潜行就交回控制权
        if(player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        return InteractionResult.PASS;//这里不再默认手动触发方块逻辑，因为太多了，BlockState里，Block里，ItemStack里都有，靠子类覆写
        //手动触发方块逻辑
        //return state.useItemOn(stack, level, player, hand, hitResult);
    }
}
