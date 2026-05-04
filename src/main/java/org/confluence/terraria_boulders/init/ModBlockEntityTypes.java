package org.confluence.terraria_boulders.init;

import com.mojang.datafixers.types.Type;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;

import java.util.function.Supplier;

public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = TerrariaBoulders.modRegister(Registries.BLOCK_ENTITY_TYPE);

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, Type<?> type, Supplier<BlockEntityType.Builder<T>> builder) {
        return REGISTER.register(name, () -> builder.get().build(type));
    }
}
