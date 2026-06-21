package org.confluence.terraria_boulders.linkage.jade;

import net.minecraft.world.level.block.state.BlockState;
import org.confluence.terraria_boulders.common.block.boulder.CamouflagedBoulderBlock;
import snownee.jade.api.*;

@WailaPlugin
public class TerrariaBouldersPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {

    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            if (!(accessor instanceof BlockAccessor blockAccessor)) {
                return accessor;
            }

            if (!(blockAccessor.getBlock() instanceof CamouflagedBoulderBlock camouflagedBoulderBlock)) {
                return accessor;
            }

            BlockState camouflageState = camouflagedBoulderBlock.getCamouflageState(blockAccessor.getBlockEntity());
            return registration.blockAccessor().from(blockAccessor).blockState(camouflageState).build();
        });
    }
}