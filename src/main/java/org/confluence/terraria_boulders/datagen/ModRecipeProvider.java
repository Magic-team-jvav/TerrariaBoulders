package org.confluence.terraria_boulders.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.init.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.REDSTONE, ModItems.BOULDER)
                .define('S', Items.STONE)
                .define('I', Items.IRON_NUGGET)
                .pattern("SSS")
                .pattern("SIS")
                .pattern("SSS")
                .unlockedBy("has_iron_nugget", this.has(Items.IRON_NUGGET))
                .save(this.output, recipeKey("boulder"));

        boulderUpgrade(ModItems.OAK_LOG_BOULDER, Items.OAK_LOG);
        boulderUpgrade(ModItems.FOLLOWER_BOULDER, Items.ENDER_EYE);
        boulderUpgrade(ModItems.EXPLODE_BOULDER, Items.TNT);
        boulderUpgrade(ModItems.ROLLING_CACTUS_BOULDER, Items.CACTUS);
        boulderUpgrade(ModItems.BOUNCY_BOULDER, Items.SLIME_BALL);
        boulderUpgrade(ModItems.GHOULDER, Items.GHAST_TEAR);
        boulderUpgrade(ModItems.LAVA_BOULDER, Items.LAVA_BUCKET);
        boulderUpgrade(ModItems.SPIDER_BOULDER, Items.SPIDER_EYE);
        boulderUpgrade(ModItems.RAINBOW_BOULDER, Items.PRISMARINE_CRYSTALS);
        boulderUpgrade(ModItems.CAMOUFLAGED_BOULDER, Items.HONEYCOMB);
    }

    private void boulderUpgrade(ItemLike result, ItemLike ingredient) {
        shapeless(RecipeCategory.REDSTONE, result)
                .requires(ModItems.BOULDER)
                .requires(ingredient)
                .unlockedBy("has_boulder", this.has(ModItems.BOULDER))
                .save(this.output, recipeKey(getItemName(result)));
    }

    private static ResourceKey<Recipe<?>> recipeKey(String name) {
        return ResourceKey.create(Registries.RECIPE, TerrariaBoulders.modRl(name));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return TerrariaBoulders.ID + " recipes";
        }
    }
}
