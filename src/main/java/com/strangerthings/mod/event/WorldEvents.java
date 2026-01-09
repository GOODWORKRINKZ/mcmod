package com.strangerthings.mod.event;

import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.entity.DemogorgonEntity;
import com.strangerthings.mod.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StrangerThingsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
    
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        StrangerThingsMod.LOGGER.info("Level loaded: {}", event.getLevel());
    }
    
    // Спавним демогорганов каждые 10 секунд (200 тиков)
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        tickCounter++;
        if (tickCounter < 200) return;  // 10 секунд
        tickCounter = 0;
        
        // Пытаемся заспавнить демогоргана рядом с каждым игроком
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension() != Level.OVERWORLD) continue;
            
            for (ServerPlayer player : level.players()) {
                // Шанс 20% на спавн
                if (level.random.nextInt(100) < 20) {
                    // Случайная позиция в радиусе 32-64 блоков от игрока
                    int distance = 32 + level.random.nextInt(32);
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    
                    BlockPos playerPos = player.blockPosition();
                    BlockPos spawnPos = playerPos.offset(
                        (int)(Math.cos(angle) * distance),
                        0,
                        (int)(Math.sin(angle) * distance)
                    );
                    
                    // Находим поверхность
                    spawnPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);
                    
                    // Проверяем что можно заспавнить
                    if (DemogorgonEntity.canDemogorgonSpawn(
                        ModEntities.DEMOGORGON.get(),
                        level,
                        MobSpawnType.NATURAL,
                        spawnPos,
                        level.random)) {
                        
                        DemogorgonEntity demogorgon = ModEntities.DEMOGORGON.get().create(level);
                        if (demogorgon != null) {
                            demogorgon.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                            demogorgon.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
                            level.addFreshEntity(demogorgon);
                            
                            StrangerThingsMod.LOGGER.info("=== SPAWNED DEMOGORGON at {} near player {} ===", spawnPos, player.getName().getString());
                        }
                    }
                }
            }
        }
    }
    
    // Этот метод вызывается КАЖДЫЙ раз когда игра пытается заспавнить моба
    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
        // Если это демогорган - всегда разрешаем спавн (кроме Peaceful)
        if (event.getEntity() instanceof DemogorgonEntity) {
            boolean canSpawn = DemogorgonEntity.canDemogorgonSpawn(
                ModEntities.DEMOGORGON.get(),
                event.getLevel(),
                event.getSpawnType(),
                event.getEntity().blockPosition(),
                event.getLevel().getRandom()
            );
            
            if (canSpawn) {
                event.setResult(Event.Result.ALLOW);
                StrangerThingsMod.LOGGER.info("SPAWN ALLOWED via PositionCheck at {}", event.getEntity().blockPosition());
            } else {
                event.setResult(Event.Result.DENY);
                StrangerThingsMod.LOGGER.info("SPAWN DENIED via PositionCheck at {}", event.getEntity().blockPosition());
            }
        }
    }
}
