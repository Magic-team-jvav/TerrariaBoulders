package org.confluence.terraria_boulders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin {
    @WrapOperation(method = "hasDifferentLightProperties(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useShapeForLightOcclusion()Z"))
    private static boolean terraria_boulders$hasDifferentLightProperties$useShapeForLightOcclusion(
            BlockState instance,
            Operation<Boolean> original,
            @Local(argsOnly = true, name = "level") BlockGetter level,
            @Local(argsOnly = true, name = "pos") BlockPos pos) {
        if (!(instance.getBlock() instanceof CamouflagedBoulderBlock)) {
            return original.call(instance);
        }
        return CamouflagedBoulderBlock.getCamouflageState(level, pos).useShapeForLightOcclusion();
    }

    @WrapOperation(method = "hasDifferentLightProperties(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightDampening()I"))
    private static int terraria_boulders$hasDifferentLightProperties$getLightDampening(
            BlockState instance,
            Operation<Integer> original,
            @Local(argsOnly = true, name = "level") BlockGetter level,
            @Local(argsOnly = true, name = "pos") BlockPos pos) {
        if (!(instance.getBlock() instanceof CamouflagedBoulderBlock)) {
            return original.call(instance);
        }
        return CamouflagedBoulderBlock.getCamouflageState(level, pos).getLightDampening();
    }
}
