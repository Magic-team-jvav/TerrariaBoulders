package org.confluence.terraria_boulders.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.confluence.terraria_boulders.TerrariaBoulders;
import org.confluence.terraria_boulders.client.renderer.RollingCactusSpikeRenderer;

public class RollingCactusSpikeModel extends EntityModel<RollingCactusSpikeRenderer.RollingCactusSpikeRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(TerrariaBoulders.modRl("rolling_cactus_spike"), "main");

    public RollingCactusSpikeModel(ModelPart root) {
        super(root.getChild("root"));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.5F, 2F, -6.0F, 6.0F, 0.0F, 11.0F, CubeDeformation.NONE)
                .texOffs(0, 11)
                .addBox(0.0F, -1.5F, -6.0F, 0.0F, 7.0F, 9.0F, CubeDeformation.NONE), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 48, 48);
    }
}