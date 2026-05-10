package org.confluence.terraria_boulders.client.model;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MiniBoulderCannonModel/*<T extends Entity> extends EntityModel<T>*/ {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	//public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new Identifier("modid", "mini_boulder_cannon"), "main");
	public final ModelPart body;
	public final ModelPart bb_main;

	public MiniBoulderCannonModel(ModelPart root) {
		this.body = root.getChild("body");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0276F, 4.1552F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(38, 32).mirror().addBox(-1.3F, -1.1F, -3.5F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(2.6F, -0.9313F, -7.9986F, -0.162F, 0.1468F, 0.7298F));

		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(38, 32).addBox(0.3F, -1.1F, -3.5F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-2.6F, -0.9313F, -7.9986F, -0.162F, -0.1468F, -0.7298F));

		PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(22, 32).mirror().addBox(1.0F, -0.5F, -3.8F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(22, 32).addBox(-4.2F, -0.5F, -3.8F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, -4.2834F, -6.9481F, -0.2182F, 0.0F, 0.0F));

		PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 30).addBox(-2.0F, 4.4F, -3.5F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(26, 24).addBox(-2.0F, -0.5F, -3.5F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.8834F, -6.9481F, -0.2182F, 0.0F, 0.0F));

		PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-0.5F, -2.96F, -10.4F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-0.3F, -2.1276F, -0.6552F, -0.1186F, -0.1836F, -0.9926F));

		PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(30, 0).addBox(-1.5F, -2.96F, -10.4F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.3F, -2.1276F, -0.6552F, -0.1186F, 0.1836F, 0.9926F));

		PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(32, 16).addBox(-2.9F, -2.9F, -0.8F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.1F))
		.texOffs(43, 51).addBox(-1.9F, -1.9F, -2.8F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(32, 16).addBox(-2.9F, -2.9F, -0.8F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -4.6048F, -11.7743F, -0.1555F, 0.1536F, 0.7734F));

		PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 30).addBox(0.5F, -7.5F, 1.5F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -4.5F, -3.5F, 7.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.5276F, -0.6552F, -0.2182F, 0.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -2.0F, 1.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-5.0F, -5.0F, 2.5F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 39).addBox(-5.05F, -6.0F, 0.0F, 0.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 39).mirror().addBox(5.05F, -6.0F, 0.0F, 0.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r9 = bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(18, 44).addBox(-5.5F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.1F, -3.0F, 4.0F, -0.7854F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

//	@Override
//	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//
//	}

	//@Override
//	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
//		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
//		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
//	}
}