package com.strangerthings.mod.entity;

import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.block.ModBlocks;
import com.strangerthings.mod.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class DemogorgonEntity extends Monster {
    
    // Кастомные правила спавна - ОЧЕНЬ мягкие, демогорганы появляются почти всегда
    public static boolean canDemogorgonSpawn(EntityType<DemogorgonEntity> entityType, 
                                            ServerLevelAccessor level, 
                                            MobSpawnType spawnType, 
                                            BlockPos pos, 
                                            RandomSource random) {
        StrangerThingsMod.LOGGER.info("=== DEMOGORGON SPAWN CHECK === Pos: {}, Difficulty: {}, Dimension: {}", 
            pos, level.getDifficulty(), level.getLevel().dimension().location());
        
        // Не спавнится ТОЛЬКО на Peaceful
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            StrangerThingsMod.LOGGER.info("SPAWN DENIED: Peaceful difficulty");
            return false;
        }
        
        // В Изнанке спавнится всегда (если не Peaceful)
        if (level.getLevel().dimension() == ModDimensions.UPSIDE_DOWN_LEVEL) {
            StrangerThingsMod.LOGGER.info("SPAWN ALLOWED: Upside Down dimension");
            return true;
        }
        
        // В обычном мире - спавнится ВСЕГДА (даже днем!) если не Peaceful
        // Убраны все ограничения по свету
        StrangerThingsMod.LOGGER.info("SPAWN ALLOWED: Overworld");
        return true;
    }
    private static final EntityDataAccessor<Boolean> OPENING_PORTAL = 
            SynchedEntityData.defineId(DemogorgonEntity.class, EntityDataSerializers.BOOLEAN);
    
    private int portalOpeningTicks = 0;
    private static final int PORTAL_OPENING_TIME = 60; // 3 секунды

    public DemogorgonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenPortalGoal(this));
        // В обычном мире - преследует и толкает к порталу
        this.goalSelector.addGoal(2, new PushToPortalGoal(this, 1.3D));
        // Атакует только в Изнанке!
        this.goalSelector.addGoal(3, new DimensionAwareMeleeGoal(this, 1.2D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // Охота на всех животных
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.Pig.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.Cow.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.Sheep.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.Chicken.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.Rabbit.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.PolarBear.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.horse.Horse.class, true));
        // Охота на враждебных мобов
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Zombie.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Skeleton.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Creeper.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Spider.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.EnderMan.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OPENING_PORTAL, false);
    }

    public boolean isOpeningPortal() {
        return this.entityData.get(OPENING_PORTAL);
    }

    public void setOpeningPortal(boolean opening) {
        this.entityData.set(OPENING_PORTAL, opening);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        
        if (isOpeningPortal()) {
            portalOpeningTicks++;
            
            // Эффект открытия портала
            if (portalOpeningTicks % 5 == 0) {
                this.playSound(SoundEvents.PORTAL_AMBIENT, 1.0F, 1.0F);
            }
            
            if (portalOpeningTicks >= PORTAL_OPENING_TIME) {
                openPortal();
                setOpeningPortal(false);
                portalOpeningTicks = 0;
            }
        }
    }

    private void openPortal() {
        if (this.level() instanceof ServerLevel serverLevel) {
            BlockPos portalPos = this.blockPosition();
            
            // Создаем ВРЕМЕННЫЙ портал 3x3
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = portalPos.offset(x, 0, z);
                    serverLevel.setBlock(pos, ModBlocks.TEMPORARY_PORTAL.get().defaultBlockState(), 3);
                }
            }
            
            this.playSound(SoundEvents.PORTAL_TRIGGER, 2.0F, 1.0F);
        }
    }

    // Способность затягивать сущностей к порталу
    public void pullNearbyEntities() {
        if (!this.level().isClientSide) {
            // Находим ближайший портал
            BlockPos portalPos = findNearestPortal();
            if (portalPos != null) {
                // Затягиваем всех живых существ к порталу
                for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, 
                        this.getBoundingBox().inflate(15.0D))) {
                    if (entity != this && entity.isAlive()) {
                        Vec3 direction = Vec3.atCenterOf(portalPos).subtract(entity.position()).normalize();
                        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(0.15)));
                    }
                }
            }
        }
    }
    
    private BlockPos findNearestPortal() {
        BlockPos pos = this.blockPosition();
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (this.level().getBlockState(checkPos).is(ModBlocks.TEMPORARY_PORTAL.get()) ||
                        this.level().getBlockState(checkPos).is(ModBlocks.UPSIDE_DOWN_PORTAL.get())) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    // Класс для преследования и толкания к порталу в обычном мире
    static class PushToPortalGoal extends Goal {
        private final DemogorgonEntity demogorgon;
        private final double speedModifier;
        private LivingEntity target;

        public PushToPortalGoal(DemogorgonEntity demogorgon, double speedModifier) {
            this.demogorgon = demogorgon;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            // Работает только в обычном мире
            if (this.demogorgon.level().dimension().equals(ModDimensions.UPSIDE_DOWN_LEVEL)) {
                return false;
            }
            this.target = this.demogorgon.getTarget();
            return this.target != null && this.target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && 
                   !this.demogorgon.level().dimension().equals(ModDimensions.UPSIDE_DOWN_LEVEL);
        }

        @Override
        public void tick() {
            if (this.target != null) {
                // Преследуем цель
                this.demogorgon.getNavigation().moveTo(this.target, this.speedModifier);
                this.demogorgon.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
                
                // Если рядом с порталом - толкаем сильнее
                if (this.demogorgon.findNearestPortal() != null) {
                    this.demogorgon.pullNearbyEntities();
                }
            }
        }

        @Override
        public void stop() {
            this.target = null;
        }
    }

    // Класс для атаки только в Изнанке
    static class DimensionAwareMeleeGoal extends MeleeAttackGoal {
        private final DemogorgonEntity demogorgon;

        public DimensionAwareMeleeGoal(DemogorgonEntity demogorgon, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(demogorgon, speedModifier, followingTargetEvenIfNotSeen);
            this.demogorgon = demogorgon;
        }

        @Override
        public boolean canUse() {
            // Атакует только в Изнанке
            if (!this.demogorgon.level().dimension().equals(ModDimensions.UPSIDE_DOWN_LEVEL)) {
                return false;
            }
            return super.canUse();
        }
    }

    // Внутренний класс для AI цели открытия портала
    static class OpenPortalGoal extends Goal {
        private final DemogorgonEntity demogorgon;
        private int cooldown = 0;

        public OpenPortalGoal(DemogorgonEntity demogorgon) {
            this.demogorgon = demogorgon;
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            
            // В обычном мире - чаще открывает порталы (20%), в Изнанке реже (5%)
            LivingEntity target = this.demogorgon.getTarget();
            boolean isOverworld = !this.demogorgon.level().dimension().equals(ModDimensions.UPSIDE_DOWN_LEVEL);
            int chance = isOverworld ? 20 : 5;
            return target != null && this.demogorgon.random.nextInt(100) < chance;
        }

        @Override
        public void start() {
            this.demogorgon.setOpeningPortal(true);
            cooldown = 300; // 15 секунд кулдаун
        }

        @Override
        public boolean canContinueToUse() {
            return this.demogorgon.isOpeningPortal();
        }

        @Override
        public void tick() {
            this.demogorgon.pullNearbyEntities();
        }

        @Override
        public void stop() {
            this.demogorgon.setOpeningPortal(false);
        }
    }
}
