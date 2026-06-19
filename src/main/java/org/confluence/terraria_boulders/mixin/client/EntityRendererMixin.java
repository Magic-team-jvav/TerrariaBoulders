package org.confluence.terraria_boulders.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.confluence.terraria_boulders.mixed.IEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin implements IEntityRenderer {
    @Unique
    private EntityRendererProvider.Context terraria_boulders$context;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void terraria_boulders$init(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.terraria_boulders$context = context;
    }

    @Override
    public EntityRendererProvider.Context getTerraria_boulders$context() {
        return terraria_boulders$context;
    }
}
