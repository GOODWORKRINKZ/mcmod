package com.strangerthings.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;

public class TemporaryPortalBlock extends UpsideDownPortalBlock {
    private static final int PORTAL_LIFETIME = 600; // 30 секунд (20 тиков = 1 секунда)

    public TemporaryPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Проверяем тег блока - если это постоянный портал, не удаляем
            // Временные порталы создаются Демогорганом без специального тега
            if (!state.is(net.minecraft.tags.BlockTags.create(
                new net.minecraft.resources.ResourceLocation(com.strangerthings.mod.StrangerThingsMod.MOD_ID, "permanent_portal")))) {
                // Запланировать удаление портала через 30 секунд
                serverLevel.scheduleTick(pos, this, PORTAL_LIFETIME);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // Удаляем портал
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.levelEvent(2001, pos, Block.getId(state)); // Эффект разрушения блока
    }
}
