package com.cakeauto;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CakeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("tort")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        // Открываем GUI через сетевой пакет
                        CakeAutoPackets.sendOpenGuiPacket(player);
                        player.sendMessage(Text.literal("§a[CakeAuto] §fОткрываю меню настроек торта..."), false);
                    }
                    return 1;
                })
        );
    }
}
