package org.confluence.terraria_boulders.events.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IBlockBreakable {
    default void onRemove(Level level, BlockState state, BlockPos pos, @Nullable Player player) {}
}
