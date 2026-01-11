package com.strangerthings.mod.block;

import com.strangerthings.mod.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.strangerthings.mod.StrangerThingsMod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class UpsideDownPortalBlock extends Block {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    
    // Кулдаун для каждого существа чтобы не было мерцания
    private static final Map<UUID, Long> portalCooldown = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100; // 5 секунд - чтобы животное успело уйти

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
        
        for (LivingEntity entity : entities) {
            String entityName = entity.getName().getString();
            // Логируем только для коров и демогоргонов
            if (!(entity instanceof net.minecraft.world.entity.animal.Cow) && 
                !(entity instanceof com.strangerthings.mod.entity.DemogorgonEntity)) {
                continue;
            }
            
            UUID entityId = entity.getUUID();
            long currentTime = level.getGameTime();
            
            // НЕ ПРИТЯГИВАЕМ если животное УЖЕ В ИЗНАНКЕ!
            boolean inUpsideDown = entity.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
            if (inUpsideDown) {
                LOGGER.info("[PULL] {} already in Upside Down - skipping", entityName);
                continue; // Животное уже в изнанке - не трогаем
            }
            
            // НЕ ПРИТЯГИВАЕМ животных на кулдауне!
            if (portalCooldown.containsKey(entityId)) {
                long lastUse = portalCooldown.get(entityId);
                if (currentTime - lastUse < COOLDOWN_TICKS) {
                    LOGGER.info("[PULL] {} on cooldown - skipping", entityName);
                    continue; // Пропускаем - животное на кулдауне
                }
            }
            
            // Затягиваем к центру портала
            Vec3 portalCenter = Vec3.atCenterOf(pos).add(0, 0.3, 0);
            Vec3 entityPos = entity.position();
            Vec3 direction = portalCenter.subtract(entityPos);
            double distance = direction.length();
            
            LOGGER.info("[PULL] {} distance to portal: {}", entityName, String.format("%.2f", distance));
            
            // ТЕЛЕПОРТИРУЕМ если животное очень близко к порталу
            if (distance < 1.5D) {
                LOGGER.info("[PULL] {} close enough - calling teleport!", entityName);
                teleportEntity(level, pos, entity);
            } else if (distance < 5.0D) {
                // Притягиваем к порталу
                double pullStrength = 0.08 * (1.0 - distance / 5.0);
                Vec3 pull = direction.normalize().scale(pullStrength);
                entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
                entity.hurtMarked = true;
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
            teleportEntity(level, pos, livingEntity);
        }
    }
    
    // Метод телепортации сущности
    private void teleportEntity(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        
        UUID entityId = livingEntity.getUUID();
        long currentTime = level.getGameTime();
        
        // Проверяем кулдаун
        if (portalCooldown.containsKey(entityId)) {
            long lastUse = portalCooldown.get(entityId);
            if (currentTime - lastUse < COOLDOWN_TICKS) {
                return; // Еще на кулдауне
            }
        }
        
        // Обновляем время последнего использования
        portalCooldown.put(entityId, currentTime);
        
        // Переключаем эффект изнанки ДЛЯ ВСЕХ существ (не только игроков)
        boolean hasEffect = livingEntity.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
        
        String entityName = livingEntity.getName().getString();
        LOGGER.info("=== PORTAL TELEPORT ===");
        LOGGER.info("{} has effect: {}", entityName, hasEffect);
        
        if (hasEffect) {
            // Убираем эффект - возвращаемся в обычный мир
            livingEntity.removeEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
            LOGGER.info("REMOVED effect from {}", entityName);
            level.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // Визуальный эффект (на сервере)
            if (level instanceof ServerLevel serverLevel) {
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
        } else {
            // Даем эффект - попадаем в Изнанку
            livingEntity.addEffect(new MobEffectInstance(
                ModEffects.UPSIDE_DOWN_EFFECT.get(), 
                999999, // Бесконечный эффект (пока не выйдет через портал)
                0, 
                false, 
                false, 
                true
            ));
            LOGGER.info("ADDED effect to {}", entityName);
            // ПРОВЕРЯЕМ ЧТО ЭФФЕКТ ДОБАВИЛСЯ
            boolean checkEffect = livingEntity.hasEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
            LOGGER.info("CHECK: {} now has effect: {}", entityName, checkEffect);
            level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 0.8F);
            
            // Визуальный эффект (на сервере)
            if (level instanceof ServerLevel serverLevel) {
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
        
        // ОТТАЛКИВАЕМ животное от портала после телепортации чтобы оно не застряло
        Vec3 entityPos = livingEntity.position();
        Vec3 portalCenter = Vec3.atCenterOf(pos);
        Vec3 awayFromPortal = entityPos.subtract(portalCenter).normalize().scale(6.0); // СИЛЬНО отталкиваем на 6 блоков (больше радиуса притягивания 5)
        livingEntity.setDeltaMovement(awayFromPortal);
        livingEntity.hurtMarked = true;
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
