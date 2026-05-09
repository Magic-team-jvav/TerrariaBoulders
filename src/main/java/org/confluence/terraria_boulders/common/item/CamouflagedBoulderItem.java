package org.confluence.terraria_boulders.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.confluence.terraria_boulders.init.ModDataComponents;
import org.jspecify.annotations.NonNull;

public class CamouflagedBoulderItem extends BlockItem {
    public CamouflagedBoulderItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    @NonNull
    public Component getName(ItemStack stack) {
        BlockState mimicState = stack.get(ModDataComponents.MIMIC_STATE.get());

        if (mimicState != null && mimicState.getBlock() != null && mimicState.getBlock() != Blocks.AIR) {
            //有数据 传入目标方块的名字
            return Component.translatable(this.getDescriptionId(), mimicState.getBlock().getName());
        }
        //没有数据
        return Component.translatable(this.getDescriptionId(), Component.empty());
    }
}
