package com.strangerthings.mod.effect;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = 
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, StrangerThingsMod.MOD_ID);

    public static final RegistryObject<MobEffect> UPSIDE_DOWN_EFFECT = MOB_EFFECTS.register("upside_down",
        () -> new UpsideDownEffect(MobEffectCategory.NEUTRAL, 0x8B0000)); // Темно-красный цвет

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
