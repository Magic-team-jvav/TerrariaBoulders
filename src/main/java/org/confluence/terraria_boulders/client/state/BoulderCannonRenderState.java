package org.confluence.terraria_boulders.client.state;


import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.util.Mth;

public class BoulderCannonRenderState extends BlockEntityRenderState {
    public float yaw;
    public float yawO;
    public float pitch;
    public float pitchO;
    public boolean isEmpty;
    public float partialTicks;

    public float getLerpYaw() {
        return Mth.rotLerp(partialTicks, yawO, yaw);
    }

    public float getLerpPitch() {
        return Mth.lerp(partialTicks, pitchO, pitch);
    }
}