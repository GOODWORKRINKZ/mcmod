package com.strangerthings.mod.event;

import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = StrangerThingsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockSyncEvents {
    
    // Предотвращаем бесконечную рекурсию синхронизации
    private static final ThreadLocal<Boolean> isSyncing = ThreadLocal.withInitial(() -> false);
    
    // Игнорируем синхронизацию порталов (чтобы не копировались между мирами)
    private static final Set<String> ignoredBlocks = new HashSet<>();
    
    static {
        ignoredBlocks.add("strangerthingsmod:upside_down_portal");
        ignoredBlocks.add("strangerthingsmod:temporary_portal");
    }
    
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && !isSyncing.get()) {
            syncBlockChange(serverLevel, event.getPos(), event.getPlacedBlock());
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && !isSyncing.get()) {
            syncBlockChange(serverLevel, event.getPos(), event.getLevel().getBlockState(event.getPos()));
        }
    }
    
    private static void syncBlockChange(ServerLevel sourceLevel, BlockPos pos, BlockState state) {
        // Игнорируем порталы
        String blockId = state.getBlock().toString();
        if (ignoredBlocks.stream().anyMatch(blockId::contains)) {
            return;
        }
        
        MinecraftServer server = sourceLevel.getServer();
        if (server == null) return;
        
        ServerLevel targetLevel = null;
        
        // Определяем целевое измерение
        if (sourceLevel.dimension() == Level.OVERWORLD) {
            targetLevel = server.getLevel(ModDimensions.UPSIDE_DOWN_LEVEL);
        } else if (sourceLevel.dimension() == ModDimensions.UPSIDE_DOWN_LEVEL) {
            targetLevel = server.getLevel(Level.OVERWORLD);
        }
        
        if (targetLevel != null) {
            isSyncing.set(true);
            try {
                // Синхронизируем блок в целевое измерение
                targetLevel.setBlock(pos, state, 3);
            } catch (Exception e) {
                // Игнорируем ошибки синхронизации
            } finally {
                isSyncing.set(false);
            }
        }
    }
}
