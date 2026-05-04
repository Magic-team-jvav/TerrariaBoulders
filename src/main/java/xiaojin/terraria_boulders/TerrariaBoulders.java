package xiaojin.terraria_boulders;

import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xiaojin.terraria_boulders.common.network.IPacket;
import xiaojin.terraria_boulders.configs.ModConfigs;
import xiaojin.terraria_boulders.init.*;

@Mod(TerrariaBoulders.ID)
public class TerrariaBoulders {
    public static final String ID = "terraria_boulders";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public TerrariaBoulders(IEventBus modEventBus, ModContainer modContainer) {
        ModConfigs.register(modContainer);
        NeoForge.EVENT_BUS.register(this);
        ModEffects.REGISTER.register(modEventBus);
        ModEntityTypes.REGISTER.register(modEventBus);
        ModBlockEntityTypes.REGISTER.register(modEventBus);
        ModBlocks.REGISTER.register(modEventBus);
        ModItems.REGISTER.register(modEventBus);
        ModCreativeModeTabs.REGISTRY.register(modEventBus);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation modRl(final String name) {
        return ResourceLocation.fromNamespaceAndPath(ID, name);
    }

    @Contract(pure = true)
    public static @NotNull String modRlText(final String name) {
        return ID + ":" + name;
    }

    public static <T> @NotNull DeferredRegister<T> modRegister(Registry<T> registry) {
        return DeferredRegister.create(registry, ID);
    }

    public static <T> @NotNull DeferredRegister<T> modRegister(ResourceKey<Registry<T>> registry) {
        return DeferredRegister.create(registry, ID);
    }

    public static <T> ResourceKey<T> modResourceKey(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, modRl(path));
    }

    public static <T> ResourceKey<Registry<T>> modResourceKey(String path) {
        return ResourceKey.createRegistryKey(modRl(path));
    }

    public static <P extends IPacket> CustomPacketPayload.Type<P> modType(String id) {
        return new CustomPacketPayload.Type<>(modRl(id));
    }
}
