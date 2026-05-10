package org.confluence.terraria_boulders.common.entity.boulder;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.confluence.terraria_boulders.init.ModEntityTypes;

public class CamouflagedBoulderEntity extends BoulderEntity {
    public CamouflagedBoulderEntity(EntityType<? extends BoulderEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CamouflagedBoulderEntity(Level level, Vec3 pos, BlockState blockState) {
        super(ModEntityTypes.CAMOUFLAGED_BOULDER.get(), level, pos, blockState);
    }
}
