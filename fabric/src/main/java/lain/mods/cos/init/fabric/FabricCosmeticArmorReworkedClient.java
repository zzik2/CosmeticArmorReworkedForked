package lain.mods.cos.init.fabric;

import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.GuiHandler;
import lain.mods.cos.impl.client.InventoryManagerClient;
import lain.mods.cos.impl.client.KeyHandler;
import lain.mods.cos.impl.client.PlayerRenderHandler;
import lain.mods.cos.impl.client.gui.GuiCosArmorInventory;
import lain.mods.cos.impl.network.ModPayloads;
import lain.mods.cos.impl.network.payload.PayloadSyncCosArmor;
import lain.mods.cos.impl.network.payload.PayloadSyncHiddenFlags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class FabricCosmeticArmorReworkedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register Menu Screen
        MenuScreens.register(FabricCosmeticArmorReworked.typeContainerCosArmor, GuiCosArmorInventory::new);

        // Register Key Mappings
        KeyHandler.INSTANCE.registerKeyMappings(KeyBindingHelper::registerKeyBinding);

        // Client Payload handlers
        ClientPlayNetworking.registerGlobalReceiver(PayloadSyncCosArmor.TYPE, (payload, context) -> {
            ModPayloads.handleSyncCosArmor(payload, () -> context.client().execute(() -> {
            }));
        });
        ClientPlayNetworking.registerGlobalReceiver(PayloadSyncHiddenFlags.TYPE, (payload, context) -> {
            ModPayloads.handleSyncHiddenFlags(payload, () -> context.client().execute(() -> {
            }));
        });

        // Client events
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (ModObjects.invMan instanceof InventoryManagerClient invManClient) {
                invManClient.handleLoggedOut();
            }
            PlayerRenderHandler.INSTANCE.handleLoggedOut();
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            KeyHandler.INSTANCE.handleClientTick();
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                GuiHandler.INSTANCE.handleGuiInitPost(screen, widget -> Screens.getButtons(screen).add(widget));
            }
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?>) {
                ScreenEvents.beforeRender(screen).register((screen1, context, mouseX, mouseY, tickDelta) -> {
                    if (screen1 instanceof AbstractContainerScreen<?> containerScreen) {
                        GuiHandler.INSTANCE.handleGuiDrawPre(containerScreen);
                    }
                });
            }
        });

        // Player render events - using mixin or direct armor manipulation
        LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player -> {
            return true;
        });
    }

}
