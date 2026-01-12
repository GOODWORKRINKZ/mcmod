package com.strangerthings.mod.sound;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StrangerThingsMod.MOD_ID);

    public static final RegistryObject<SoundEvent> DEMOGORGON_AMBIENT =
            registerSoundEvent("entity.demogorgon.ambient");

    public static final RegistryObject<SoundEvent> DEMOGORGON_SCREAM =
            registerSoundEvent("entity.demogorgon.scream");

    public static final RegistryObject<SoundEvent> DEMOGORGON_HURT =
            registerSoundEvent("entity.demogorgon.hurt");

    public static final RegistryObject<SoundEvent> DEMOGORGON_DEATH =
            registerSoundEvent("entity.demogorgon.death");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(StrangerThingsMod.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
