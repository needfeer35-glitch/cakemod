package com.cakeauto;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CakeAutoPackets {
    public static final Identifier OPEN_GUI = new Identifier("cakeauto", "open_gui");
    public static final Identifier TOGGLE_AUTO = new Identifier("cakeauto", "toggle_auto");

    public static void register() {
        // Получаем пакет от клиента с состоянием галочки
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_AUTO, (server, player, handler, buf, responseSender) -> {
            boolean enabled = buf.readBoolean();
            server.execute(() -> {
                CakeAutoState state = CakeAutoState.get(player);
                state.setAutoEnabled(enabled);
                player.sendMessage(
                    net.minecraft.text.Text.literal(
                        enabled ? "§a[CakeAuto] §fАвто-торт §aвключён!" : "§a[CakeAuto] §fАвто-торт §cвыключен."
                    ), false
                );
            });
        });
    }

    public static void sendOpenGuiPacket(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, OPEN_GUI, buf);
    }
}
