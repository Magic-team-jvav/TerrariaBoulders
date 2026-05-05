package org.confluence.terraria_boulders.configs;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.confluence.terraria_boulders.TerrariaBoulders;

import java.util.List;

public final class TCCommonConfigs {
    private static ModConfigSpec.ConfigValue<List<? extends String>> RARE_BLOCKS;
    private static ModConfigSpec.ConfigValue<List<? extends String>> RARE_CREATURES;
    public static Object2IntSortedMap<BlockState> rareBlocks = new Object2IntLinkedOpenHashMap<>();
    public static Object2IntSortedMap<EntityType<?>> rareCreatures = new Object2IntLinkedOpenHashMap<>();

    public static ModConfigSpec.BooleanValue RANDOM_ATTACK_DAMAGE;
    public static ModConfigSpec.DoubleValue RANDOM_ATTACK_DAMAGE_MIN;
    public static ModConfigSpec.DoubleValue RANDOM_ATTACK_DAMAGE_MAX;

    public static ModConfigSpec.IntValue MAX_ACCESSORIES;

    public static void onLoad() {
        Object2IntSortedMap<BlockState> blockStates = new Object2IntLinkedOpenHashMap<>();
        RARE_BLOCKS.get().forEach(s -> {
            try {
                blockStates.put(BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.freeze(), s, false).blockState(), blockStates.size());
            } catch (Exception e) {
                TerrariaBoulders.LOGGER.warn("BlockState {} not found", s);
            }
        });
        rareBlocks = blockStates;

        Object2IntSortedMap<EntityType<?>> entityTypes = new Object2IntLinkedOpenHashMap<>();
        RARE_CREATURES.get().forEach(s -> {
            Identifier id = Identifier.parse(s);
            BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresentOrElse(
                    entityType -> entityTypes.put(entityType, entityTypes.size()),
                    () -> TerrariaBoulders.LOGGER.warn("EntityType {} not found", id)
            );
        });
        rareCreatures = entityTypes;
    }

    public static void register(ModContainer container) {
        ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        RARE_BLOCKS = BUILDER.comment(
                "In order for the block to be found by the Metal Detector",
                "You need to fill the list with string like 'modid:block[state1=true]' or 'modid:block'",
                "The higher the block in the list, the higher the value"
        ).defineListAllowEmpty("rareBlocks", List.of(
                "terraria_boulders:life_fruit",
                "terraria_boulders:chlorophyte_ore",
                "terraria_boulders:deepslate_titanium_ore",
                "terraria_boulders:deepslate_adamantite_ore",
                "terraria_boulders:deepslate_orichalcum_ore",
                "terraria_boulders:deepslate_mythril_ore",
                "terraria_boulders:deepslate_palladium_ore",
                "terraria_boulders:deepslate_cobalt_ore",
                "terraria_boulders:dragonsal_ore",
                "terraria_boulders:lunartear_ore",
                "terraria_boulders:life_crystal_block",
                "terraria_boulders:sword_in_stone",
                "terraria_boulders:dungeon_chest",
                "terraria_boulders:water_chest",
                "terraria_boulders:frozen_chest",
                "terraria_boulders:sandstone_chest",
                "terraria_boulders:golden_chest",
                "terraria_boulders:shadow_chest",
                "terraria_boulders:skyware_chest",
                "terraria_boulders:living_wood_chest",
                "terraria_boulders:opal_ore",
                "terraria_boulders:gelstone_ore",
                "terraria_boulders:cold_crystal_ore",
                "terraria_boulders:crimtane_ore",
                "terraria_boulders:deepslate_crimtane_ore",
                "terraria_boulders:demonite_ore",
                "terraria_boulders:deepslate_demonite_ore",
                "minecraft:ancient_debris",
                "minecraft:diamond_ore",
                "minecraft:deepslate_diamond_ore",
                "terraria_boulders:platinum_ore",
                "terraria_boulders:deepslate_platinum_ore",
                "minecraft:gold_ore",
                "minecraft:deepslate_gold_ore",
                "terraria_boulders:forest_pot",
                "terraria_boulders:tundra_pot",
                "terraria_boulders:spider_nest_pot",
                "terraria_boulders:underground_desert_pot",
                "terraria_boulders:jungle_pot",
                "terraria_boulders:marble_cave_pot",
                "terraria_boulders:pyramid_pot",
                "terraria_boulders:corruption_pot",
                "terraria_boulders:crimson_pot",
                "terraria_boulders:dungeon_pot",
                "terraria_boulders:underworld_pot",
                "terraria_boulders:lihzahrd_pot",
                "terraria_boulders:tungsten_ore",
                "terraria_boulders:deepslate_tungsten_ore",
                "terraria_boulders:silver_ore",
                "terraria_boulders:deepslate_silver_ore",
                "terraria_boulders:lead_ore",
                "terraria_boulders:deepslate_lead_ore",
                "minecraft:iron_ore",
                "minecraft:deepslate_iron_ore",
                "terraria_boulders:tin_ore",
                "terraria_boulders:deepslate_tin_ore",
                "minecraft:copper_ore",
                "minecraft:deepslate_copper_ore"
        ), () -> "minecraft:stone", o -> true);
        RARE_CREATURES = BUILDER.comment(
                "In order for the creature to be found by the Life Form Analyzer",
                "You need to fill the list with string like 'modid:entity'",
                "The higher the creature in the list, the higher the value"
        ).defineListAllowEmpty("rareCreatures", List.of(
                "terra_entity:jungle_mimic",
                "terra_entity:corrupt_mimic",
                "terra_entity:crimson_mimic",
                "terra_entity:hallowed_mimic",
                "terra_entity:golden_mimic",
                "terra_entity:ice_mimic",
                "terra_entity:shadow_mimic",
                "terra_entity:wooden_mimic",
                "terra_entity:voodoo_demon",
                "terra_entity:dungeon_slime",
                "terra_entity:nymph",
                "terra_entity:wandering_eye_fish",
                "terra_entity:golden_slime",
                "terra_entity:pink_slime",
                "minecraft:skeleton_horse",
                "minecraft:sniffer",
                "minecraft:allay",
                "minecraft:warden",
                "minecraft:mooshroom",
                "minecraft:panda"
        ), () -> "minecraft:pig", o -> true);
        RANDOM_ATTACK_DAMAGE = BUILDER.push("Random Attack Damage").define("enable", false);
        RANDOM_ATTACK_DAMAGE_MIN = BUILDER.defineInRange("min", 0.8, 0.0, 1.0);
        RANDOM_ATTACK_DAMAGE_MAX = BUILDER.defineInRange("max", 1.2, 1.0, 2.0);
        MAX_ACCESSORIES = BUILDER.pop().defineInRange("Max Accessory Amount", 7, 6, 100);
        container.registerConfig(ModConfig.Type.COMMON, BUILDER.build());
    }
}