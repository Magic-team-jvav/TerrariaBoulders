package org.confluence.terraria_boulders.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @WrapMethod(method = "isFaceSturdy(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/SupportType;)Z")
    private boolean terraria_boulders$isFaceSturdy(
            BlockGetter level,
            BlockPos pos,
            Direction direction,
            SupportType supportType,
            Operation<Boolean> original
    ) {
        if (!(terraria_boulders$getBlockStateBase().getBlock() instanceof CamouflagedBoulderBlock)) {
            return original.call(level, pos, direction, supportType);
        }
        return CamouflagedBoulderBlock.getCamouflageState(level, pos).isFaceSturdy(level, pos, direction, supportType);
    }

    @WrapMethod(method = "isSuffocating")
    private boolean terraria_boulders$isSuffocating(BlockGetter level, BlockPos pos, Operation<Boolean> original) {
        if (!(terraria_boulders$getBlockStateBase().getBlock() instanceof CamouflagedBoulderBlock)) {
            return original.call(level, pos);
        }
        return CamouflagedBoulderBlock.getCamouflageState(level, pos).isSuffocating(level, pos);
    }

    @WrapMethod(method = "isViewBlocking")
    private boolean terraria_boulders$isViewBlocking(BlockGetter level, BlockPos pos, Operation<Boolean> original) {
        if (!(terraria_boulders$getBlockStateBase().getBlock() instanceof CamouflagedBoulderBlock)) {
            return original.call(level, pos);
        }
        return CamouflagedBoulderBlock.getCamouflageState(level, pos).isViewBlocking(level, pos);
    }

    @Unique
    private BlockBehaviour.@NonNull BlockStateBase terraria_boulders$getBlockStateBase() {
        return (BlockBehaviour.BlockStateBase) (Object) this;
    }
}
