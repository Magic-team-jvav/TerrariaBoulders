//package org.confluence.terraria_boulders.mixin;
//
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.MoverType;
//import net.minecraft.world.phys.Vec3;
//import org.confluence.terraria_boulders.mixed.IMovementExtension;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(Entity.class)
//public class EntityMoveMixin implements IMovementExtension {
//    @Unique
//    private Vec3 terrariaBoulders$preCollisionMovement = Vec3.ZERO;
//    @Shadow
//    private Vec3 deltaMovement;
//
//    //在原版刚体物理碰撞箱将速度削减之前拷贝速度
//    @Inject(method = "move", at = @At("HEAD"))
//    private void terrariaBoulders$capturePreCollisionMovement(MoverType moverType, Vec3 delta, CallbackInfo ci) {
//        if (this.deltaMovement != null) {
//            this.terrariaBoulders$preCollisionMovement = this.deltaMovement;
//        }
//    }
//
//    @Override
//    public Vec3 terraria_boulders$getPreCollisionMovement() {
//        return this.terrariaBoulders$preCollisionMovement;
//    }
//
//    @Override
//    public void terraria_boulders$setPreCollisionMovement(Vec3 momentum) {
//
//    }
//}
