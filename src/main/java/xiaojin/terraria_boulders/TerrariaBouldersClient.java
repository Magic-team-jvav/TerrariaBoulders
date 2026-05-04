package xiaojin.terraria_boulders;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TerrariaBoulders.ID, value = Dist.CLIENT)
public class TerrariaBouldersClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        TerrariaBoulders.LOGGER.info("HELLO FROM CLIENT SETUP");
        TerrariaBoulders.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}