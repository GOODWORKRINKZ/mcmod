package com.strangerthings.mod.block;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, StrangerThingsMod.MOD_ID);

    public static final RegistryObject<Block> UPSIDE_DOWN_PORTAL = BLOCKS.register("upside_down_portal",
            () -> new UpsideDownPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .lightLevel((state) -> 5)
                    .sound(SoundType.GLASS)
                    .randomTicks())); // Добавили randomTicks() для активного затягивания
    
    public static final RegistryObject<Block> TEMPORARY_PORTAL = BLOCKS.register("temporary_portal",
            () -> new TemporaryPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .lightLevel((state) -> 5)
                    .sound(SoundType.GLASS)
                    .randomTicks())); // Временный портал от Демогоргана

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
