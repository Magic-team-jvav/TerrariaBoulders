package org.confluence.terraria_boulders.common.block.boulder;

import net.minecraft.world.entity.EntityType;
import org.confluence.terraria_boulders.common.entity.boulder.BoulderEntity;

public interface IBoulderMedia {
    //返回该方块对应的实体类型
    EntityType<? extends BoulderEntity> getBoulderEntityType();
}
