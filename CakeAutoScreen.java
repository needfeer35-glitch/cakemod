package com.cakeauto.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

public class CakeAutoScreen extends Screen {

    private boolean autoEnabled = false;
    private CheckboxWidget checkbox;

    public CakeAutoScreen() {
        super(Text.literal("§6🎂 CakeAuto — Настройки"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Чекбокс: включить авто-торт
        checkbox = CheckboxWidget.builder(
                Text.literal("Автоматически заполнять рецепт торта"),
                this.textRenderer
            )
            .pos(centerX - 120, centerY - 10)
            .checked(autoEnabled)
            .callback((cb, checked) -> {
                autoEnabled = checked;
                CakeAutoClient.sendToggle(checked);
            })
            .build();
        this.addDrawableChild(checkbox);

        // Кнопка "Открыть верстак"
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("🪚 Открыть верстак с тортом"),
                btn -> {
                    if (this.client != null) {
                        this.client.setScreen(null); // закрываем GUI
                        // Открываем верстак через mixin/экран
                        openCraftingWithCake();
                    }
                })
            .dimensions(centerX - 100, centerY + 30, 200, 20)
            .build()
        );

        // Кнопка закрыть
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Закрыть"),
                btn -> this.close()
            )
            .dimensions(centerX - 40, centerY + 60, 80, 20)
            .build()
        );
    }

    private void openCraftingWithCake() {
        if (this.client == null || this.client.player == null) return;
        // Открываем стандартный экран верстака
        this.client.execute(() -> {
            this.client.player.openHandledScreen(
                new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                    (syncId, inv, player) -> new net.minecraft.screen.CraftingScreenHandler(syncId, inv),
                    Text.literal("Создание торта")
                )
            );
            // После открытия верстака — заполнить рецепт (если чекбокс включён)
            if (autoEnabled) {
                // Небольшая задержка чтобы верстак успел открыться
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {}
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        fillCakeRecipe();
                    });
                }).start();
            }
        });
    }

    /**
     * Заполняет верстак ингредиентами торта:
     * Рецепт торта (3x3):
     * [молоко] [молоко] [молоко]
     * [сахар]  [яйцо]  [сахар]
     * [пшеница][пшеница][пшеница]
     */
    private void fillCakeRecipe() {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen == null) return;

        if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.CraftingScreen craftingScreen) {
            var handler = craftingScreen.getScreenHandler();
            var inventory = client.player.getInventory();

            // Слоты верстака (1-9): 1=топ-лево, 3=топ-право, 7=бот-лево, 9=бот-право
            int[] slotItems = {
                net.minecraft.item.Items.MILK_BUCKET.getRawId(),  // слот 1
                net.minecraft.item.Items.MILK_BUCKET.getRawId(),  // слот 2
                net.minecraft.item.Items.MILK_BUCKET.getRawId(),  // слот 3
                net.minecraft.item.Items.SUGAR.getRawId(),         // слот 4
                net.minecraft.item.Items.EGG.getRawId(),           // слот 5
                net.minecraft.item.Items.SUGAR.getRawId(),         // слот 6
                net.minecraft.item.Items.WHEAT.getRawId(),         // слот 7
                net.minecraft.item.Items.WHEAT.getRawId(),         // слот 8
                net.minecraft.item.Items.WHEAT.getRawId(),         // слот 9
            };

            // Используем interactSlot для перемещения предметов
            for (int i = 0; i < 9; i++) {
                var targetSlot = handler.slots.get(i + 1); // слоты верстака начинаются с 1
                if (!targetSlot.getStack().isEmpty()) continue; // слот уже занят

                net.minecraft.item.Item neededItem = net.minecraft.util.registry.Registry.ITEM.get(
                    net.minecraft.util.registry.Registry.ITEM.getId(slotItems[i])
                );

                // Ищем предмет в инвентаре
                for (int invSlot = 0; invSlot < inventory.size(); invSlot++) {
                    var stack = inventory.getStack(invSlot);
                    if (!stack.isEmpty() && stack.getItem() == neededItem) {
                        // Кликаем предмет в инвентарь верстака
                        client.interactionManager.clickSlot(
                            handler.syncId,
                            handler.slots.size() - inventory.size() + invSlot,
                            0,
                            net.minecraft.screen.slot.SlotActionType.PICKUP,
                            client.player
                        );
                        client.interactionManager.clickSlot(
                            handler.syncId,
                            i + 1,
                            0,
                            net.minecraft.screen.slot.SlotActionType.PICKUP,
                            client.player
                        );
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Рисуем фон панели
        context.fill(centerX - 130, centerY - 40, centerX + 130, centerY + 90, 0xCC1a1a2e);
        context.drawBorder(centerX - 130, centerY - 40, 260, 130, 0xFFf0a500);

        // Заголовок
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§6🎂 CakeAuto"),
            centerX, centerY - 30, 0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Рецепт: молоко×3, сахар×2, яйцо×1, пшеница×3"),
            centerX, centerY - 18, 0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
