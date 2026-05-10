package org.confluence.terraria_boulders.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TerrariaBoulders.ID);

    //伪装状态
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> MIMIC_STATE = REGISTER.registerComponentType(
            "mimic_state",
            builder -> builder.persistent(BlockState.CODEC)//.networkSynchronized(BlockState.CODEC)
    );

    //是否锁定伪装状态
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_LOCKED = REGISTER.registerComponentType(
            "is_locked",
            builder -> builder.persistent(Codec.BOOL)
    );
}
