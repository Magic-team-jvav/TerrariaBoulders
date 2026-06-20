package org.confluence.terraria_boulders.client.model;// Made with Blockbench 5.1.4

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.confluence.terraria_boulders.TerrariaBoulders;

public class BoulderGloveItemModel/* extends Model<Object>*/ {
    private static final Identifier TEXTURE_LOADED = Identifier.fromNamespaceAndPath(TerrariaBoulders.ID, "textures/item/boulder_glove_model.png");
    public static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE_LOADED);
    private final ModelPart root;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public BoulderGloveItemModel(ModelPart root) {
        this.root = root.getChild("root");
        this.rightArm = this.root.getChild("rightArm");
        this.leftArm = this.root.getChild("leftArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(16, 0).mirror().addBox(-3.0F, 5.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-3.0F, 5.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offset(1.0F, -10.0F, 0.0F));

        PartDefinition leftArm = root.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 5.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.3F))
                .texOffs(16, 0).addBox(-1.0F, 5.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(-1.0F, -10.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void resetPose() {
        this.root.resetPose();
        this.rightArm.resetPose();
        this.leftArm.resetPose();
    }

    public void displayToHand(boolean isLeft) {
        this.rightArm.visible = !isLeft;
        this.leftArm.visible = isLeft;
    }

    public ModelPart getModelPart() {
        return root;
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        submitNodeCollector.submitModelPart(getModelPart(), poseStack, getRenderType(), lightCoords, OverlayTexture.NO_OVERLAY, null);
    }
}