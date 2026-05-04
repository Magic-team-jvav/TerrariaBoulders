package xiaojin.terraria_boulders.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xiaojin.terraria_boulders.TerrariaBoulders;


public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = TerrariaBoulders.modRegister(BuiltInRegistries.SOUND_EVENT);

    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_DEATH = register("soul_death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(TerrariaBoulders.modRl(id)));
    }
}
