package com.cakeauto.client;

import com.cakeauto.CakeAutoPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public class CakeAutoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Получаем сигнал от сервера — открыть GUI
        ClientPlayNetworking.registerGlobalReceiver(CakeAutoPackets.OPEN_GUI, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.setScreen(new CakeAutoScreen());
            });
        });
    }

    // Отправляем серверу состояние галочки
    public static void sendToggle(boolean enabled) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enabled);
        ClientPlayNetworking.send(CakeAutoPackets.TOGGLE_AUTO, buf);
    }
}
