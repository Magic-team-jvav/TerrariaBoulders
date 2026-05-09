package org.confluence.terraria_boulders.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderBlockEntity;

public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = TerrariaBoulders.modRegister(Registries.BLOCK_ENTITY_TYPE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CamouflagedBoulderBlockEntity>> CAMOUFLAGED_BOULDER =
            REGISTER.register(
                    "camouflaged_boulder",
                    () -> new BlockEntityType<>(
                            CamouflagedBoulderBlockEntity::new,
                            ModBlocks.CAMOUFLAGED_BOULDER.get()
                    )
            );
}
