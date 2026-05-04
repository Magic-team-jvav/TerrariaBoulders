package org.confluence.terraria_boulders.init;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.common.item.BaseFoodItem;
import org.confluence.terraria_boulders.common.item.ModFoodPropertiesBuilder;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(TerrariaBoulders.ID);
}
