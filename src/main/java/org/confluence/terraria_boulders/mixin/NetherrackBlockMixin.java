package org.confluence.terraria_boulders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.NetherrackBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherrackBlock.class)
public abstract class NetherrackBlockMixin {
    @WrapOperation(method = "isValidBonemealTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;propagatesSkylightDown()Z"))
    private boolean terraria_boulders$isValidBonemealTarget(BlockState instance, Operation<Boolean> original, @Local(argsOnly = true, name = "level") LevelReader level, @Local(argsOnly = true, name = "pos") BlockPos pos) {
        if (!(instance.getBlock() instanceof CamouflagedBoulderBlock camouflagedBoulderBlock)) {
            return original.call(instance);
        }
        return CamouflagedBoulderBlock.getCamouflageState(level, pos).canOcclude();
    }
}
