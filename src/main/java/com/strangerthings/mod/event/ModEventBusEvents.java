package com.strangerthings.mod.event;

import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.entity.DemogorgonEntity;
import com.strangerthings.mod.entity.ModEntities;
import com.strangerthings.mod.entity.client.DemogorgonModel;
import com.strangerthings.mod.entity.client.DemogorgonRenderer;
import com.strangerthings.mod.entity.client.ModModelLayers;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangerThingsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DEMOGORGON.get(), DemogorgonEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DEMOGORGON.get(), DemogorgonRenderer::new);
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.DEMOGORGON_LAYER, DemogorgonModel::createBodyLayer);
    }
    
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
            ModEntities.DEMOGORGON.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            DemogorgonEntity::canDemogorgonSpawn,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        StrangerThingsMod.LOGGER.info("Registered Demogorgon spawn placement!");
    }
}
