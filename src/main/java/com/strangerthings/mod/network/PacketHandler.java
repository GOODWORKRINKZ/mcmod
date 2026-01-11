package com.strangerthings.mod.network;

import com.strangerthings.mod.StrangerThingsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    public static void register() {
        SimpleChannel net = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StrangerThingsMod.MOD_ID, "messages"),
            () -> "1.0",
            s -> true,
            s -> true
        );
        INSTANCE = net;

        // Регистрируем пакет для синхронизации эффекта
        net.messageBuilder(SyncEffectPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncEffectPacket::new)
            .encoder(SyncEffectPacket::toBytes)
            .consumerMainThread(SyncEffectPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        // Отправляем всем игрокам на клиенте
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
