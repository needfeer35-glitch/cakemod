package com.cakeauto.mixin;

import com.cakeauto.CakeAutoState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Inject(method = "onTakeItem", at = @At("TAIL"))
    private void onCakeCrafted(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // Проверяем: это торт и авто-режим включён?
        if (!stack.isOf(Items.CAKE)) return;

        // На сервере проверяем состояние
        if (!player.getWorld().isClient()) {
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                CakeAutoState state = CakeAutoState.get(serverPlayer);
                if (!state.isAutoEnabled()) return;
            }
            return;
        }

        // На клиенте: заполняем рецепт снова
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.CraftingScreen craftingScreen) {
            refillCakeIngredients(craftingScreen, player);
        }
    }

    private void refillCakeIngredients(
        net.minecraft.client.gui.screen.ingame.CraftingScreen screen,
        PlayerEntity player
    ) {
        var handler = screen.getScreenHandler();
        var inventory = player.getInventory();
        var client = net.minecraft.client.MinecraftClient.getInstance();

        // Рецепт торта:
        // [молоко][молоко][молоко]
        // [сахар ][яйцо ][сахар ]
        // [пшен ][пшен ][пшен  ]
        net.minecraft.item.Item[] recipe = {
            Items.MILK_BUCKET, Items.MILK_BUCKET, Items.MILK_BUCKET,
            Items.SUGAR,       Items.EGG,         Items.SUGAR,
            Items.WHEAT,       Items.WHEAT,        Items.WHEAT
        };

        for (int i = 0; i < 9; i++) {
            var craftSlot = handler.slots.get(i + 1);
            if (!craftSlot.getStack().isEmpty()) continue; // уже есть предмет

            net.minecraft.item.Item needed = recipe[i];
            int inventoryOffset = handler.slots.size() - inventory.main.size() - 4; // учитываем хотбар и броню

            for (int invIdx = 0; invIdx < inventory.main.size(); invIdx++) {
                var invStack = inventory.main.get(invIdx);
                if (!invStack.isEmpty() && invStack.isOf(needed)) {
                    int guiSlot = inventoryOffset + invIdx;
                    if (guiSlot < 0) continue;

                    // Берём из инвентаря
                    client.interactionManager.clickSlot(
                        handler.syncId, guiSlot, 0,
                        net.minecraft.screen.slot.SlotActionType.PICKUP, client.player
                    );
                    // Кладём в верстак
                    client.interactionManager.clickSlot(
                        handler.syncId, i + 1, 0,
                        net.minecraft.screen.slot.SlotActionType.PICKUP, client.player
                    );
                    break;
                }
            }
        }
    }
}
