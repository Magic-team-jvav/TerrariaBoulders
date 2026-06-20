package org.confluence.terraria_boulders.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.confluence.terraria_boulders.client.model.BoulderGloveItemModel;
import org.confluence.terraria_boulders.events.ModClientEvent;
import org.confluence.terraria_boulders.init.ModItems;
import org.confluence.terraria_boulders.mixed.IEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<S>> extends RenderLayer<S, M> {
    @Shadow
    protected abstract boolean useBabyOffset(S state);

    @Unique
    private BoulderGloveItemModel terraria_boulders$boulderGloveModel;

    public ItemInHandLayerMixin(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void terraria_boulders$init(RenderLayerParent<?, ?> renderer, CallbackInfo ci) {
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> renderer1) {
            EntityRendererProvider.Context terrariaBoulders$context = IEntityRenderer.of(renderer1).getTerraria_boulders$context();
            this.terraria_boulders$boulderGloveModel = new BoulderGloveItemModel(terrariaBoulders$context.bakeLayer(ModClientEvent.BOULDER_GLOVE_LAYER));
        }
    }

    @WrapMethod(method = "submitArmWithItem")
    private void terraria_boulders$submitArmWithItem(
            S state,
            ItemStackRenderState item,
            ItemStack itemStack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            Operation<Void> original
    ) {
        if (state.getMainHandItemStack().getItem() == ModItems.BOULDER_GLOVE.asItem()) {
            poseStack.pushPose();
            terraria_boulders$boulderGloveModel.resetPose();
            boolean isLeftHand = arm == HumanoidArm.LEFT;
            terraria_boulders$boulderGloveModel.displayToHand(isLeftHand);
            getParentModel().translateToHand(state, arm, poseStack);
            float offsetX = useBabyOffset(state) ? -2.0F : -1.0F;
            float offsetY = useBabyOffset(state) ? -13.0F : -14.0F;
            int i = isLeftHand ? -1 : 1;
            poseStack.translate(i * offsetX / 16.0F, offsetY / 16.0F, 0 / 16.0F);
            terraria_boulders$boulderGloveModel.submit(poseStack, submitNodeCollector, lightCoords);
            poseStack.popPose();
            return;
        }

        original.call(state, item, itemStack, arm, poseStack, submitNodeCollector, lightCoords);
    }
}
