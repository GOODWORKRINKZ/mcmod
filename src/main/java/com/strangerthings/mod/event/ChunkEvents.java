package com.strangerthings.mod.event;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangerThingsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkEvents {
    // Копирование чанков отключено - вместо этого используется визуальная трансформация мира
}
