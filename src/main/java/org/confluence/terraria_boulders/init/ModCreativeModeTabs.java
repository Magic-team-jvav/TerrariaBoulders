package org.confluence.terraria_boulders.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = TerrariaBoulders.modRegister(BuiltInRegistries.CREATIVE_MODE_TAB);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TERRARIA_BOULDERS = register(
            "terraria_boulders", (name) ->
                    createCreativeModeTab(name, (parameters, output) ->
                            addRegistryItem(ModItems.REGISTER, output), ModBlocks.BOULDER::toStack));

    private static DeferredHolder<CreativeModeTab, CreativeModeTab> register(
            String name,
            Function<String, CreativeModeTab.Builder> builder
    ) {
        return REGISTRY.register(name, builder.apply(name)::build);
    }

    private static CreativeModeTab.Builder createCreativeModeTab(
            String name,
            CreativeModeTab.DisplayItemsGenerator displayItemsGenerator,
            Supplier<ItemStack> icon,
            ResourceKey<CreativeModeTab> withTabsBefore
    ) {
        return createCreativeModeTab(name, displayItemsGenerator, icon)
                .withTabsBefore(withTabsBefore);
    }

    private static CreativeModeTab.Builder createCreativeModeTab(
            String name,
            CreativeModeTab.DisplayItemsGenerator displayItemsGenerator,
            Supplier<ItemStack> icon
    ) {
        return createCreativeModeTab(name, displayItemsGenerator)
                .icon(icon);
    }

    private static CreativeModeTab.Builder createCreativeModeTab(
            String name,
            CreativeModeTab.DisplayItemsGenerator displayItemsGenerator
    ) {
        String key = "itemGroup." + TerrariaBoulders.ID + "." + name;
        return CreativeModeTab.builder()
                .title(Component.translatable(key))
                .displayItems(displayItemsGenerator);
    }

    private static void addRegistryItem(DeferredRegister.Items registry, CreativeModeTab.Output output) {
        registry.getEntries().forEach(entry -> output.accept(entry.get()));
    }
}
