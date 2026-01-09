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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpsideDownPortalBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    
    // Кулдаун для каждого существа чтобы не было мерцания
    private static final Map<UUID, Long> portalCooldown = new HashMap<>();
    private static final long COOLDOWN_TICKS = 40; // 2 секунды

    public UpsideDownPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
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
            
            if (hasEffect) {
                // Убираем эффект - возвращаемся в обычный мир
                livingEntity.removeEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
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
