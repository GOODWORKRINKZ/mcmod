package com.strangerthings.mod.world.dimension;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensions {
    public static final ResourceKey<Level> UPSIDE_DOWN_LEVEL = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(StrangerThingsMod.MOD_ID, "upside_down"));
    
    public static final ResourceKey<DimensionType> UPSIDE_DOWN_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(StrangerThingsMod.MOD_ID, "upside_down"));

    public static void register() {
        StrangerThingsMod.LOGGER.info("Registering Dimensions for " + StrangerThingsMod.MOD_ID);
    }
}
