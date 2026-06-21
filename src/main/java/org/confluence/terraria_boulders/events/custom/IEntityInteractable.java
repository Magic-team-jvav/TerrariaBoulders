package org.confluence.terraria_boulders.events.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//探测实体
public interface IEntityInteractable {
    /**
     * 当玩家与实体交互时触发
     * @return 返回交互结果
     */
    default InteractionResult interactEntity(ItemStack stack, Player player, Entity target, InteractionHand hand){
        return InteractionResult.PASS;
    }
}