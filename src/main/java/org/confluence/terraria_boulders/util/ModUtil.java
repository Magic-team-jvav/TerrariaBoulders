package org.confluence.terraria_boulders.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModUtil {
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> a, BlockEntityType<E> b, BlockEntityTicker<? super E> ticker) {
        return a == b ? (BlockEntityTicker<A>) ticker : null;
    }
    public static boolean isSingleplayerOwner(ServerPlayer player) {
        return player.server.isSingleplayerOwner(player.nameAndId());
    }
}
