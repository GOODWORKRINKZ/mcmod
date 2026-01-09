package com.strangerthings.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class UpsideDownTeleporter implements ITeleporter {
    private final BlockPos targetPos;

    public UpsideDownTeleporter(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, 
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        Entity repositionedEntity = repositionEntity.apply(false);
        
        // Устанавливаем позицию телепортации
        Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        repositionedEntity.moveTo(targetVec.x, targetVec.y, targetVec.z, yaw, entity.getXRot());
        
        return repositionedEntity;
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, 
                                     Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5), 
                Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }
}
