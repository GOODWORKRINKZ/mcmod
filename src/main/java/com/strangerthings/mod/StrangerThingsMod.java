package com.strangerthings.mod;

import com.strangerthings.mod.effect.ModEffects;
import com.strangerthings.mod.entity.ModEntities;
import com.strangerthings.mod.world.dimension.ModDimensions;
import com.strangerthings.mod.block.ModBlocks;
import com.strangerthings.mod.item.ModItems;
import com.strangerthings.mod.network.PacketHandler;
import com.strangerthings.mod.sound.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(StrangerThingsMod.MOD_ID)
public class StrangerThingsMod {
    public static final String MOD_ID = "strangerthingsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(StrangerThingsMod.class);

    public StrangerThingsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрация всех компонентов мода
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModDimensions.register();
        
        // Регистрируем пакеты для сетевой синхронизации
        PacketHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Stranger Things Mod initialized!");
    }
}
