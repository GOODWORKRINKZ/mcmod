package com.strangerthings.mod.network;

import com.strangerthings.mod.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.function.Supplier;

public class SyncEffectPacket {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final int entityId;
    private final boolean hasEffect;

    public SyncEffectPacket(int entityId, boolean hasEffect) {
        this.entityId = entityId;
        this.hasEffect = hasEffect;
    }

    public SyncEffectPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.hasEffect = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(hasEffect);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    if (hasEffect) {
                        // Добавляем эффект на клиенте
                        livingEntity.addEffect(new MobEffectInstance(
                            ModEffects.UPSIDE_DOWN_EFFECT.get(),
                            999999,
                            0,
                            false,
                            false,
                            true
                        ));
                        LOGGER.info("[CLIENT SYNC] Added UPSIDE_DOWN effect to {}", entity.getName().getString());
                    } else {
                        // Убираем эффект на клиенте
                        livingEntity.removeEffect(ModEffects.UPSIDE_DOWN_EFFECT.get());
                        LOGGER.info("[CLIENT SYNC] Removed UPSIDE_DOWN effect from {}", entity.getName().getString());
                    }
                }
            });
        });
        return true;
    }
}
