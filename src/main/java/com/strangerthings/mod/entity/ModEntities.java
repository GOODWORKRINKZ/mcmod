package com.strangerthings.mod.entity;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StrangerThingsMod.MOD_ID);

    public static final RegistryObject<EntityType<DemogorgonEntity>> DEMOGORGON =
            ENTITY_TYPES.register("demogorgon", () -> EntityType.Builder.of(DemogorgonEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 2.5f)
                    .build("demogorgon"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
