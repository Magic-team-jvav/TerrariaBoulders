package org.confluence.terraria_boulders.mixin;

import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockPlaceContext.class)
public interface BlockPlaceContextAccessor {
    @Accessor
    boolean getReplaceClicked();

    @Accessor
    void setReplaceClicked(boolean replaceClicked);
}
