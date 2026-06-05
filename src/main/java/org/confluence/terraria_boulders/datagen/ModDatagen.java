package org.confluence.terraria_boulders.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDatagen {
    private ModDatagen() {
    }

    public static void gatherData(GatherDataEvent.Server event) {
        event.createProvider(ModRecipeProvider.Runner::new);
    }
}
