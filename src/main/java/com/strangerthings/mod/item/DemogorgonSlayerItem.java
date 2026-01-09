package com.strangerthings.mod.item;

import com.strangerthings.mod.entity.DemogorgonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class DemogorgonSlayerItem extends SwordItem {
    
    public DemogorgonSlayerItem(Properties properties) {
        super(new DemogorgonSlayerTier(), 5, -2.4F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Дополнительный урон Демогоргонам
        if (target instanceof DemogorgonEntity) {
            target.hurt(attacker.damageSources().mobAttack(attacker), 10.0F); // +10 урона
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    // Кастомный Tier для меча
    private static class DemogorgonSlayerTier implements Tier {
        @Override
        public int getUses() {
            return 1500; // Прочность
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 6.0F; // Базовый урон
        }

        @Override
        public int getLevel() {
            return 3; // Уровень (алмазный)
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(net.minecraft.world.item.Items.DIAMOND);
        }
    }
}
