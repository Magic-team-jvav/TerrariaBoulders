package org.confluence.terraria_boulders.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.confluence.terraria_boulders.TerrariaBoulders;

public class ModTags {
    public static class Blocks {
        //方块名单：：不可交互，但也可以右键触发陷阱
        public static final TagKey<Block> MANUAL_TRAP_TRIGGERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "manual_trap_triggers"));
    }
}
