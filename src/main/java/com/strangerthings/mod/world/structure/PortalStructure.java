package com.strangerthings.mod.world.structure;

import com.strangerthings.mod.StrangerThingsMod;
import com.strangerthings.mod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Random;

public class PortalStructure {
    
    public static void generate(ServerLevelAccessor level, BlockPos pos, Random random) {
        // Создаем платформу
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos platformPos = pos.offset(x, -1, z);
                level.setBlock(platformPos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
        }
        
        // Создаем каменную раму портала
        // Вертикальные столбы
        for (int y = 0; y < 4; y++) {
            level.setBlock(pos.offset(-2, y, -1), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(-2, y, 1), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(2, y, -1), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(2, y, 1), Blocks.STONE_BRICKS.defaultBlockState(), 3);
        }
        
        // Верхняя перекладина
        for (int z = -1; z <= 1; z++) {
            level.setBlock(pos.offset(-2, 4, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(-1, 4, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(0, 4, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(1, 4, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            level.setBlock(pos.offset(2, 4, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
        }
        
        // ПОСТОЯННЫЙ портал внутри (3x3)
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 || z == 0) { // Только в центре и по краям
                        level.setBlock(pos.offset(x, y, z), 
                            ModBlocks.UPSIDE_DOWN_PORTAL.get().defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // Факелы для освещения
        level.setBlock(pos.offset(-2, 2, -2), Blocks.TORCH.defaultBlockState(), 3);
        level.setBlock(pos.offset(-2, 2, 2), Blocks.TORCH.defaultBlockState(), 3);
        level.setBlock(pos.offset(2, 2, -2), Blocks.TORCH.defaultBlockState(), 3);
        level.setBlock(pos.offset(2, 2, 2), Blocks.TORCH.defaultBlockState(), 3);
        
        StrangerThingsMod.LOGGER.info("Generated Upside Down Portal structure at " + pos);
    }
}
