package com.strangerthings.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.strangerthings.mod.entity.DemogorgonEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class DemogorgonModel extends EntityModel<DemogorgonEntity> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    
    // Лепестки головы-цветка
    private final ModelPart petal1;
    private final ModelPart petal2;
    private final ModelPart petal3;
    private final ModelPart petal4;
    private final ModelPart petal5;

    public DemogorgonModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        
        // Получаем лепестки из головы
        this.petal1 = this.head.getChild("petal1");
        this.petal2 = this.head.getChild("petal2");
        this.petal3 = this.head.getChild("petal3");
        this.petal4 = this.head.getChild("petal4");
        this.petal5 = this.head.getChild("petal5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Тело (увеличенное и худое)
        PartDefinition body = partdefinition.addOrReplaceChild("body", 
            CubeListBuilder.create()
                .texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Голова (база для цветка)
        PartDefinition head = partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        // Лепестки цветка (5 лепестков вокруг головы, направлены вперед)
        // Центральный лепесток (передний)
        head.addOrReplaceChild("petal1",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-1.0F, -3.0F, -9.0F, 2.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.3F, 0.0F, 0.0F));

        // Правый верхний лепесток
        head.addOrReplaceChild("petal2",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-1.0F, -3.0F, -9.0F, 2.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.5F, 0.628F, 0.0F)); // 36 градусов вправо

        // Левый верхний лепесток
        head.addOrReplaceChild("petal3",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-1.0F, -3.0F, -9.0F, 2.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.5F, -0.628F, 0.0F)); // 36 градусов влево

        // Правый нижний лепесток
        head.addOrReplaceChild("petal4",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-1.0F, -3.0F, -9.0F, 2.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.2F, 1.047F, 0.0F)); // 60 градусов вправо

        // Левый нижний лепесток
        head.addOrReplaceChild("petal5",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-1.0F, -3.0F, -9.0F, 2.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.2F, -1.047F, 0.0F)); // 60 градусов влево

        // Длинные руки
        partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F),
            PartPose.offset(-5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create()
                .texOffs(40, 16)
                .mirror()
                .addBox(0.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F),
            PartPose.offset(5.0F, 2.0F, 0.0F));

        // Ноги
        partdefinition.addOrReplaceChild("right_leg",
            CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create()
                .texOffs(0, 16)
                .mirror()
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(DemogorgonEntity entity, float limbSwing, float limbSwingAmount, 
                         float ageInTicks, float netHeadYaw, float headPitch) {
        // Анимация головы
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Анимация ног при ходьбе
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        // Анимация рук при ходьбе
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // Анимация атаки
        if (this.attackTime > 0.0F) {
            float attackAnim = this.attackTime;
            this.rightArm.xRot = -2.0F + 1.5F * Mth.triangleWave(attackAnim, 10.0F);
            this.leftArm.xRot = -2.0F + 1.5F * Mth.triangleWave(attackAnim, 10.0F);
        }

        // Анимация лепестков - открываются и закрываются (дыхание)
        float petalAnimation = Mth.sin(ageInTicks * 0.1F) * 0.15F;
        
        // Сохраняем базовые углы и добавляем анимацию
        this.petal1.xRot = -0.3F + petalAnimation;
        this.petal2.xRot = -0.5F + petalAnimation;
        this.petal3.xRot = -0.5F + petalAnimation;
        this.petal4.xRot = 0.2F + petalAnimation;
        this.petal5.xRot = 0.2F + petalAnimation;
        
        // Если атакует - лепестки раскрываются шире
        if (entity.isOpeningPortal() || this.attackTime > 0) {
            this.petal1.xRot = -0.6F;
            this.petal2.xRot = -0.8F;
            this.petal3.xRot = -0.8F;
            this.petal4.xRot = 0.5F;
            this.petal5.xRot = 0.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, 
                              int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
