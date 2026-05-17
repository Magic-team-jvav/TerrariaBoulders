package org.confluence.terraria_boulders.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;

public class BoulderCannonModel {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	//public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "boulder_cannon"), "main");
	public final ModelPart root;
	public final ModelPart turntable;
	public final ModelPart body;
	public final ModelPart lead;
	private final ModelPart side_1;
	private final ModelPart side_2;
	public final ModelPart bb_main;

	public BoulderCannonModel(ModelPart root) {
		this.root = root.getChild("root");
		this.turntable = this.root.getChild("turntable");
		this.body = this.turntable.getChild("body");
		this.lead = this.body.getChild("lead");
		this.side_1 = this.turntable.getChild("side_1");
		this.side_2 = this.turntable.getChild("side_2");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -3.0F, -11.0F, 22.0F, 3.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition turntable = root.addOrReplaceChild("turntable", CubeListBuilder.create().texOffs(110, 44).addBox(-4.0F, -3.7538F, -1.4315F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(108, 93).addBox(-4.0F, 4.2462F, -4.4315F, 8.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(74, 93).addBox(-9.0F, 0.2462F, -6.0315F, 5.0F, 7.0F, 12.0F, new CubeDeformation(0.01F))
				.texOffs(74, 93).mirror().addBox(4.0F, 0.2462F, -6.0315F, 5.0F, 7.0F, 12.0F, new CubeDeformation(0.01F)).mirror(false)
				.texOffs(108, 105).addBox(-9.0F, -5.7538F, -3.0315F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(108, 105).addBox(4.0F, -5.7538F, -3.0315F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.2462F, 0.0315F));

		PartDefinition cube_r1 = turntable.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(58, 47).addBox(-11.0F, -2.0F, -2.0F, 22.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.7538F, -0.0315F, -0.7854F, 0.0F, 0.0F));

		PartDefinition body = turntable.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-8.0F, -11.0385F, -4.1154F, 16.0F, 17.0F, 13.0F, new CubeDeformation(0.0F))
				.texOffs(88, 0).addBox(-7.0F, -10.0385F, 8.8846F, 14.0F, 15.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(58, 25).addBox(-7.0F, -10.0385F, -11.1154F, 14.0F, 15.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(38, 93).addBox(-6.5F, -9.0385F, -32.1154F, 13.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 55).addBox(-4.0F, -8.6385F, -28.1154F, 8.0F, 2.0F, 17.0F, new CubeDeformation(0.02F))
				.texOffs(50, 55).addBox(-4.0F, 1.5615F, -28.1154F, 8.0F, 2.0F, 17.0F, new CubeDeformation(0.02F))
				.texOffs(0, 74).mirror().addBox(-6.0F, -6.5385F, -28.1154F, 2.0F, 8.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 74).addBox(4.0F, -6.5385F, -28.1154F, 2.0F, 8.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.7154F, 0.0838F, -0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(78, 74).mirror().addBox(0.6293F, -1.2293F, -2.0F, 3.0F, 2.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-6.0F, 0.4615F, -26.1154F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(78, 74).addBox(-3.6293F, -1.2293F, -2.0F, 3.0F, 2.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(6.0F, 0.4615F, -26.1154F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(38, 74).addBox(-3.6293F, -0.7707F, -2.0F, 3.0F, 2.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(6.0F, -5.5385F, -26.1154F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(38, 74).mirror().addBox(0.6293F, -0.7707F, -2.0F, 3.0F, 2.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-6.0F, -5.5385F, -26.1154F, 0.0F, 0.0F, -0.7854F));

		PartDefinition lead = body.addOrReplaceChild("lead", CubeListBuilder.create().texOffs(100, 19).addBox(0.0F, -9.5F, -3.0F, 0.0F, 13.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.5385F, 11.8846F));

		PartDefinition side_1 = turntable.addOrReplaceChild("side_1", CubeListBuilder.create().texOffs(0, 99).addBox(-0.9F, -2.7F, -11.1F, 2.0F, 10.0F, 12.0F, new CubeDeformation(-0.2F))
				.texOffs(40, 111).addBox(-0.7F, -11.9F, -7.1F, 2.0F, 10.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offset(8.5F, 1.1462F, 5.0685F));

		PartDefinition cube_r6 = side_1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 111).mirror().addBox(-1.0F, -12.0F, -2.0F, 2.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -10.2F, -0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r7 = side_1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(28, 111).addBox(-1.0F, -12.0F, -2.0F, 2.0F, 20.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition side_2 = turntable.addOrReplaceChild("side_2", CubeListBuilder.create().texOffs(0, 99).mirror().addBox(-1.1F, -2.9F, -11.1F, 2.0F, 10.0F, 12.0F, new CubeDeformation(-0.2F)).mirror(false)
				.texOffs(40, 111).mirror().addBox(-1.3F, -11.9F, -7.1F, 2.0F, 10.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offset(-8.5F, 1.1462F, 5.0685F));

		PartDefinition cube_r8 = side_2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(28, 111).mirror().addBox(-1.0F, -12.0F, -2.0F, 2.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -10.2F, -0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r9 = side_2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(28, 111).mirror().addBox(-1.0F, -12.0F, -2.0F, 2.0F, 20.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r10 = bb_main.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(100, 55).addBox(-7.0F, -8.0F, -1.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -20.0F, -34.0F, -0.2618F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

//	@Override
//	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//
//	}
//
//	@Override
//	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//	}
}