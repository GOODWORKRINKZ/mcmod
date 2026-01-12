package com.strangerthings.mod.block;

import com.strangerthings.mod.effect.ModEffects;
import com.strangerthings.mod.network.PacketHandler;
import com.strangerthings.mod.network.SyncEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import com.strangerthings.mod.StrangerThingsMod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class UpsideDownPortalBlock extends Block {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    
    // Кулдаун для каждого существа чтобы не было мерцания и зацикливания
    private static final Map<UUID, Long> portalCooldown = new HashMap<>();
    private static final long COOLDOWN_TICKS = 300; // 15 секунд - чтобы сущность точно ушла и не вернулась

    public UpsideDownPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true; // Включаем randomTick - вызывается автоматически Minecraft
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // randomTick вызывается автоматически для блоков с .randomTicks()
        // Не нужен onPlace() или scheduleTick() - Minecraft сам вызывает этот метод!
        LOGGER.info("[PORTAL] randomTick called at {}", pos);
        pullNearbyEntities(level, pos);
    }
    
    // Метод для притягивания сущностей к порталу
    private void pullNearbyEntities(ServerLevel level, BlockPos pos) {
        // Активно затягивает сущностей в радиусе 5 блоков
        AABB searchBox = new AABB(pos).inflate(5.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);
        
        LOGGER.info("[PULL] Searching for entities near portal at {}, found {} entities", pos, entities.size());
        
        // КЛЮЧЕВАЯ ПРОВЕРКА: В КАКОМ МИРЕ ПОРТАЛ?
        String portalDimension = level.dimension().location().toString();
        boolean portalInUpsideDown = portalDimension.equals("strangerthingsmod:upside_down");
        LOGGER.info("[PULL] Portal in dimension: {}", portalDimension);
        
        for (LivingEntity entity : entities) {
            String entityName = entity.getName().getString();
            
            UUID entityId = entity.getUUID();
            long currentTime = level.getGameTime();
            
            // ВАЖНО: Логика зависит от того, где находится портал!
            boolean entityHasEffect = entity.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
            
            if (portalInUpsideDown) {
                // Портал в ИЗНАНКЕ - вытягиваем ТОЛЬКО игрока и демогоргона (они хотят выйти)
                boolean isPlayer = entity instanceof net.minecraft.world.entity.player.Player;
                boolean isDemogorgon = entity.getClass().getSimpleName().equals("DemogorgonEntity");
                
                LOGGER.info("[PULL UPSIDE DOWN] {} isPlayer={} isDemogorgon={}", entityName, isPlayer, isDemogorgon);
                
                if (!isPlayer && !isDemogorgon) {
                    // Обычное животное - ВООБЩЕ НЕ ТРОГАЕМ! Оно остается в изнанке навсегда
                    LOGGER.info("[PULL] {} is animal - skipping completely (must stay in Upside Down)", entityName);
                    continue;
                }
                // Игрок или демогоргон - вытягиваем их!
                LOGGER.info("[PULL] {} WILL BE PULLED OUT (player or demogorgon)", entityName);
            } else {
                // Портал в ОБЫЧНОМ МИРЕ
                boolean isPlayer = entity instanceof net.minecraft.world.entity.player.Player;
                boolean isDemogorgon = entity.getClass().getSimpleName().equals("DemogorgonEntity");
                
                if (isPlayer || isDemogorgon) {
                    // Игроки и демогоргоны НЕ притягиваются - они входят сами
                    LOGGER.info("[PULL] {} is player/demo - not pulling in Overworld (enters manually)", entityName);
                    continue;
                }
                // Притягиваем только животных в Overworld
                LOGGER.info("[PULL] {} WILL BE PULLED IN (animal in Overworld)", entityName);
            }
            
            // КУЛДАУН РАБОТАЕТ ДЛЯ ВСЕХ БЕЗ ИСКЛЮЧЕНИЙ - это предотвращает зацикливание
            if (portalCooldown.containsKey(entityId)) {
                long lastUse = portalCooldown.get(entityId);
                if (currentTime - lastUse < COOLDOWN_TICKS) {
                    LOGGER.info("[PULL] {} on cooldown ({} ticks remaining) - skipping", entityName, COOLDOWN_TICKS - (currentTime - lastUse));
                    continue; // Пропускаем - сущность на кулдауне
                }
            }
            
            // Затягиваем к центру портала
            Vec3 portalCenter = Vec3.atCenterOf(pos).add(0, 0.3, 0);
            Vec3 entityPos = entity.position();
            Vec3 direction = portalCenter.subtract(entityPos);
            double distance = direction.length();
            
            LOGGER.info("[PULL] {} distance to portal: {}", entityName, String.format("%.2f", distance));
            
            // ТЕЛЕПОРТИРУЕМ если сущность очень близко к порталу
            if (distance < 1.5D) {
                LOGGER.info("[PULL] {} close enough - calling teleport!", entityName);
                teleportEntity((ServerLevel) level, pos, entity);
            } else if (distance < 5.0D) {
                // Притягиваем к порталу
                // Игроки/демогоргоны притягиваются ВСЕГДА (чтобы могли выйти из Upside Down)
                // Животные притягиваются ТОЛЬКО в Overworld (в Upside Down они пропускаются выше)
                double pullStrength = 0.08 * (1.0 - distance / 5.0);
                Vec3 pull = direction.normalize().scale(pullStrength);
                entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
                entity.hurtMarked = true;
                LOGGER.info("[PULL] {} being pulled with strength {}", entityName, String.format("%.4f", pullStrength));
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
            // ПРОВЕРКА КУЛДАУНА - если сущность на кулдауне, вообще ничего не делаем
            UUID entityId = livingEntity.getUUID();
            long currentTime = level.getGameTime();
            
            if (portalCooldown.containsKey(entityId)) {
                long lastUseTime = portalCooldown.get(entityId);
                long ticksPassed = currentTime - lastUseTime;
                if (ticksPassed < COOLDOWN_TICKS) {
                    // На кулдауне - полностью игнорируем
                    return;
                }
            }
            
            // НЕ трогаем сущностей которые уже имеют эффект!
            boolean hasEffect = livingEntity.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
            if (hasEffect) {
                // Сущность с эффектом - уже была телепортирована
                // НИЧЕГО НЕ ДЕЛАЕМ - просто игнорируем
                return;
            }
            teleportEntity(level, pos, livingEntity);
        }
    }
    
    // Метод телепортации сущности - ФИЗИЧЕСКАЯ телепортация между измерениями!
    private void teleportEntity(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        
        UUID entityId = livingEntity.getUUID();
        long currentTime = level.getGameTime();
        
        // СТРОГАЯ ПРОВЕРКА КУЛДАУНА - работает для ВСЕХ без исключений
        if (portalCooldown.containsKey(entityId)) {
            long lastUse = portalCooldown.get(entityId);
            if (currentTime - lastUse < COOLDOWN_TICKS) {
                LOGGER.info("[TELEPORT] {} still on cooldown - rejecting teleport", livingEntity.getName().getString());
                return; // Еще на кулдауне - отказываем в телепортации
            }
        }
        
        // Обновляем время последнего использования
        portalCooldown.put(entityId, currentTime);
        
        // Определяем ТЕКУЩЕЕ измерение сущности
        String currentDimension = level.dimension().location().toString();
        boolean inUpsideDown = currentDimension.equals("strangerthingsmod:upside_down");
        
        String entityName = livingEntity.getName().getString();
        LOGGER.info("=== PORTAL TELEPORT ===");
        LOGGER.info("{} in dimension: {}", entityName, currentDimension);
        
        if (level instanceof ServerLevel serverLevel) {
            if (inUpsideDown) {
                // Сущность В ИЗНАНКЕ - пытается выйти
                boolean isPlayer = livingEntity instanceof net.minecraft.world.entity.player.Player;
                boolean isDemogorgon = livingEntity.getClass().getSimpleName().equals("DemogorgonEntity");
                
                if (isPlayer || isDemogorgon) {
                    // РЕАЛЬНАЯ ТЕЛЕПОРТАЦИЯ в обычный мир!
                    ResourceKey<Level> overworldKey = Level.OVERWORLD;
                    ServerLevel overworld = serverLevel.getServer().getLevel(overworldKey);
                    
                    if (overworld != null) {
                        // Убираем эффект - выходим из "изнанки"
                        livingEntity.removeEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
                        LOGGER.info("REMOVED effect from {} (exiting to Overworld)", entityName);
                        
                        // СИНХРОНИЗИРУЕМ эффект на клиент
                        PacketHandler.sendToAllPlayers(new SyncEffectPacket(livingEntity.getId(), false));
                        
                        level.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        
                        // ФИЗИЧЕСКАЯ ТЕЛЕПОРТАЦИЯ В OVERWORLD
                        Vec3 safePos = Vec3.atCenterOf(pos);
                        Entity teleportedEntity = livingEntity.changeDimension(overworld, new net.minecraftforge.common.util.ITeleporter() {
                            @Override
                            public Entity placeEntity(Entity entity, ServerLevel currentLevel, ServerLevel destLevel, float yaw, Function<Boolean, Entity> repositionEntity) {
                                entity = repositionEntity.apply(false);
                                entity.setPos(safePos.x, safePos.y, safePos.z);
                                entity.setDeltaMovement(Vec3.ZERO);
                                return entity;
                            }
                        });
                        
                        if (teleportedEntity != null) {
                            LOGGER.info("TELEPORTED {} to OVERWORLD at {}", entityName, pos);
                        }
                    }
                } else {
                    // Обычное животное - НЕ может выйти из изнанки
                    LOGGER.info("BLOCKED: {} cannot exit Upside Down (not player/demogorgon)", entityName);
                }
            } else {
                // Сущность В ОБЫЧНОМ МИРЕ - затягивается в изнанку
                // Даем эффект - попадаем в "изнанку"
                livingEntity.addEffect(new MobEffectInstance(
                    ModEffects.UPSIDE_DOWN_EFFECT.get(), 
                    999999, // Бесконечный эффект (пока не выйдет через портал)
                    0, 
                    false, 
                    false, 
                    true
                ));
                LOGGER.info("ADDED effect to {}", entityName);
                
                // СИНХРОНИЗИРУЕМ эффект на клиент
                PacketHandler.sendToAllPlayers(new SyncEffectPacket(livingEntity.getId(), true));
                
                level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 0.8F);
                
                // РЕАЛЬНАЯ ТЕЛЕПОРТАЦИЯ В ИЗНАНКУ!
                ResourceKey<Level> upsideDownKey = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    new ResourceLocation("strangerthingsmod", "upside_down")
                );
                ServerLevel upsideDown = serverLevel.getServer().getLevel(upsideDownKey);
                
                if (upsideDown != null) {
                    // ФИЗИЧЕСКАЯ ТЕЛЕПОРТАЦИЯ В UPSIDE DOWN
                    Vec3 targetPos = Vec3.atCenterOf(pos);
                    Entity teleportedEntity = livingEntity.changeDimension(upsideDown, new net.minecraftforge.common.util.ITeleporter() {
                        @Override
                        public Entity placeEntity(Entity entity, ServerLevel currentLevel, ServerLevel destLevel, float yaw, Function<Boolean, Entity> repositionEntity) {
                            entity = repositionEntity.apply(false);
                            entity.setPos(targetPos.x, targetPos.y, targetPos.z);
                            entity.setDeltaMovement(Vec3.ZERO);
                            return entity;
                        }
                    });
                    
                    if (teleportedEntity != null) {
                        LOGGER.info("TELEPORTED {} to UPSIDE DOWN at {}", entityName, pos);
                    }
                }
                
                // Визуальный эффект
                for (int i = 0; i < 50; i++) {
                    double x = pos.getX() + level.random.nextDouble();
                    double y = pos.getY() + level.random.nextDouble();
                    double z = pos.getZ() + level.random.nextDouble();
                    serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 1,
                        (level.random.nextDouble() - 0.5) * 2.0, 
                        -level.random.nextDouble(), 
                        (level.random.nextDouble() - 0.5) * 2.0, 0.1);
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // Добавляем частицы портала
        if (random.nextInt(100) == 0) {
            level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, 
                    (double)pos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, 
                    SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        // Частицы портала (фиолетовые)
        for(int i = 0; i < 4; ++i) {
            double x = (double)pos.getX() + random.nextDouble();
            double y = (double)pos.getY() + random.nextDouble();
            double z = (double)pos.getZ() + random.nextDouble();
            double vx = ((double)random.nextFloat() - 0.5D) * 0.5D;
            double vy = ((double)random.nextFloat() - 0.5D) * 0.5D;
            double vz = ((double)random.nextFloat() - 0.5D) * 0.5D;
            
            level.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL, 
                    x, y, z, vx, vy, vz);
        }
        
        // Темные частицы пепла
        for(int i = 0; i < 2; ++i) {
            double x = (double)pos.getX() + random.nextDouble();
            double y = (double)pos.getY() + random.nextDouble();
            double z = (double)pos.getZ() + random.nextDouble();
            
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ASH, 
                    x, y, z, 0.0D, -0.05D, 0.0D);
        }
    }
}
