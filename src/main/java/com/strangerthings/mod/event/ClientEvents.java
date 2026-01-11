package com.strangerthings.mod.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.effect.ModEffects;
import com.strangerthings.mod.world.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = StrangerThingsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                // Проверяем есть ли у игрока эффект "В Изнанке" ИЛИ он в измерении Изнанки
                boolean inUpsideDown = mc.player.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get()) ||
                                      mc.level.dimension() == ModDimensions.UPSIDE_DOWN_LEVEL;
                
                if (inUpsideDown) {
                    Level level = mc.level;
                    
                    // МНОГО пепла и дыма
                    if (mc.player.tickCount % 1 == 0) {
                        for (int i = 0; i < 10; i++) {
                            double x = mc.player.getX() + (level.random.nextDouble() - 0.5) * 30;
                            double y = mc.player.getY() + level.random.nextDouble() * 20 + 5;
                            double z = mc.player.getZ() + (level.random.nextDouble() - 0.5) * 30;
                            
                            level.addParticle(ParticleTypes.ASH, 
                                x, y, z, 
                                0.0, -0.02, 0.0);
                        }
                        
                        // Белый дым
                        if (level.random.nextInt(2) == 0) {
                            double x = mc.player.getX() + (level.random.nextDouble() - 0.5) * 25;
                            double y = mc.player.getY() + level.random.nextDouble() * 10;
                            double z = mc.player.getZ() + (level.random.nextDouble() - 0.5) * 25;
                            
                            level.addParticle(ParticleTypes.SMOKE, 
                                x, y, z, 
                                0.0, 0.04, 0.0);
                        }
                        
                        // Красноватый дым (campfire)
                        if (level.random.nextInt(3) == 0) {
                            double x = mc.player.getX() + (level.random.nextDouble() - 0.5) * 20;
                            double y = mc.player.getY() + level.random.nextDouble() * 8;
                            double z = mc.player.getZ() + (level.random.nextDouble() - 0.5) * 20;
                            
                            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, 
                                x, y, z, 
                                0.0, 0.05, 0.0);
                        }
                    }
                }
            }
        }
    }
    
    // Красное небо и туман когда у игрока эффект "В Изнанке"
    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean inUpsideDown = mc.player.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get()) ||
                                  (mc.level != null && mc.level.dimension() == ModDimensions.UPSIDE_DOWN_LEVEL);
            
            if (inUpsideDown) {
                event.setNearPlaneDistance(5.0f);
                event.setFarPlaneDistance(40.0f); // Ближе туман для темноты
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean inUpsideDown = mc.player.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get()) ||
                                  (mc.level != null && mc.level.dimension() == ModDimensions.UPSIDE_DOWN_LEVEL);
            
            if (inUpsideDown) {
                // ЗАТЕМНЯЕМ ВСЕ БЛОКИ - уменьшаем яркость в 5 раз и добавляем красноватый оттенок
                float darkening = 0.15f; // Очень темно (85% затемнение)
                event.setRed(event.getRed() * darkening + 0.15f);   // Добавляем красный
                event.setGreen(event.getGreen() * darkening);        // Убираем зеленый
                event.setBlue(event.getBlue() * darkening);          // Убираем синий
            }
        }
    }
    
    // Скрываем существ из другого "мира" (параллельные миры в одном пространстве)
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        LivingEntity entity = event.getEntity();
        
        // Проверяем: в разных ли "измерениях" игрок и существо?
        boolean playerInUpsideDown = mc.player.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
        boolean entityInUpsideDown = entity.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
        
        // ЕСЛИ ОБА В ИЗНАНКЕ - ВСЕГДА ПОКАЗЫВАЕМ
        if (playerInUpsideDown && entityInUpsideDown) {
            return; // Оба в изнанке - видим друг друга
        }
        
        // ЕСЛИ ОБА НА ОБЫЧНОЙ СТОРОНЕ - ВСЕГДА ПОКАЗЫВАЕМ
        if (!playerInUpsideDown && !entityInUpsideDown) {
            return; // Оба на обычной стороне - видим друг друга
        }
        
        // ЕСЛИ В РАЗНЫХ ИЗМЕРЕНИЯХ - ВСЕГДА СКРЫВАЕМ
        if (playerInUpsideDown != entityInUpsideDown) {
            event.setCanceled(true);
            return;
        }
    }
}

