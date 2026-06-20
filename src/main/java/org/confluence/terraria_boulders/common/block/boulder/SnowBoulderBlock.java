package org.confluence.terraria_boulders.common.block.boulder;

import org.confluence.terraria_boulders.common.entity.boulder.SnowBoulderEntity;

public class SnowBoulderBlock extends FullCollisionBoulderBlock {
    public SnowBoulderBlock(Properties properties) {
        super(properties, SnowBoulderEntity::new);
    }
}
