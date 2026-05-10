package org.confluence.terraria_boulders.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.entity.block.BoulderCannonBlockEntity;
import org.confluence.terraria_boulders.common.entity.boulder.CamouflagedBoulderBlockEntity;

public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = TerrariaBoulders.modRegister(Registries.BLOCK_ENTITY_TYPE);

    //伪装巨石
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CamouflagedBoulderBlockEntity>> CAMOUFLAGED_BOULDER =
            REGISTER.register("camouflaged_boulder", () -> new BlockEntityType<>(CamouflagedBoulderBlockEntity::new, ModBlocks.CAMOUFLAGED_BOULDER.get()));

    //巨石大炮
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoulderCannonBlockEntity>> BOULDER_CANNON =
            REGISTER.register("boulder_cannon", () -> new BlockEntityType<>(BoulderCannonBlockEntity::new, ModBlocks.BOULDER_CANNON.get()));
}
