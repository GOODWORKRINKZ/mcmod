package com.strangerthings.mod.item;

import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, StrangerThingsMod.MOD_ID);

    public static final RegistryObject<Item> DEMOGORGON_SPAWN_EGG = ITEMS.register("demogorgon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DEMOGORGON, 0x3d1f1f, 0x8b0000,
                    new Item.Properties()));

    public static final RegistryObject<Item> DEMOGORGON_SLAYER = ITEMS.register("demogorgon_slayer",
            () -> new DemogorgonSlayerItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
