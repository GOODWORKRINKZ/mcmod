package com.strangerthings.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.entity.DemogorgonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DemogorgonRenderer extends MobRenderer<DemogorgonEntity, DemogorgonModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(StrangerThingsMod.MOD_ID, 
            "textures/entity/demogorgon.png");

    public DemogorgonRenderer(EntityRendererProvider.Context context) {
        super(context, new DemogorgonModel(context.bakeLayer(ModModelLayers.DEMOGORGON_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(DemogorgonEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(DemogorgonEntity entity, float entityYaw, float partialTicks, 
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Увеличиваем размер на 30%
        poseStack.pushPose();
        poseStack.scale(1.3f, 1.3f, 1.3f);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
