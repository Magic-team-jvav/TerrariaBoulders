package org.confluence.terraria_boulders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FenceBlock.class)
public abstract class FenceBlockMixin {
    @WrapOperation(method = "getStateForPlacement",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState terraria_boulders$getStateForPlacement(
            BlockGetter instance,
            BlockPos blockPos,
            Operation<BlockState> original
    ) {
        BlockState originalResult = original.call(instance, blockPos);
        if (originalResult.getBlock() instanceof CamouflagedBoulderBlock) {
            return CamouflagedBoulderBlock.getBlockState(instance, blockPos);
        }
        return originalResult;
    }

    @WrapOperation(method = "updateShape",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FenceBlock;connectsTo(Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/core/Direction;)Z"))
    private boolean terraria_boulders$updateShape(
            FenceBlock instance,
            BlockState state,
            boolean faceSolid,
            Direction direction,
            Operation<Boolean> original,
            @Local(argsOnly = true, name = "level") LevelReader level,
            @Local(argsOnly = true, name = "pos") BlockPos pos
    ) {
        BlockState blockState;
        if (state.getBlock() instanceof CamouflagedBoulderBlock) {
            blockState = CamouflagedBoulderBlock.getBlockState(level, pos);
        } else {
            blockState = state;
        }
        return original.call(instance, blockState, faceSolid, direction);
    }
}
