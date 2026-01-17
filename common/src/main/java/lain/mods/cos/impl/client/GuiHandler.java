package lain.mods.cos.impl.client;

import com.google.common.collect.ImmutableSet;
import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.gui.*;
import lain.mods.cos.impl.network.payload.PayloadOpenCosArmorInventory;
import lain.mods.cos.impl.network.payload.PayloadOpenNormalInventory;
import lain.mods.cos.impl.platform.PlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import zzik2.cosmeticarmorreworkedforked.mixin.AbstractContainerScreenAccessor;

import java.util.Set;
import java.util.function.Consumer;

public enum GuiHandler {

    INSTANCE;

    public static final Set<Integer> ButtonIds = ImmutableSet.of(76, 77);

    private int lastLeft;
    private boolean lastInventoryOpen;

    public void handleGuiDrawPre(AbstractContainerScreen<?> screen) {
        int guiLeft = ((AbstractContainerScreenAccessor) screen).getLeftPos();
        if (lastLeft != guiLeft) {
            int diffLeft = guiLeft - lastLeft;
            lastLeft = guiLeft;
            screen.children().stream().filter(IShiftingWidget.class::isInstance).map(IShiftingWidget.class::cast)
                    .forEach(b -> b.shiftLeft(diffLeft));
        }
        if (screen instanceof CreativeModeInventoryScreen) {
            boolean isInventoryOpen = ((CreativeModeInventoryScreen) screen).isInventoryOpen();
            if (lastInventoryOpen != isInventoryOpen) {
                lastInventoryOpen = isInventoryOpen;
                screen.children().stream().filter(ICreativeInvWidget.class::isInstance)
                        .map(ICreativeInvWidget.class::cast).forEach(b -> b.onSelectedTabChanged(isInventoryOpen));
            }
        }
    }

    public void handleGuiInitPost(Screen screen,
            Consumer<net.minecraft.client.gui.components.AbstractWidget> addWidget) {
        if (screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;

            lastLeft = containerScreen instanceof CreativeModeInventoryScreen ? 0 : accessor.getLeftPos();
            lastInventoryOpen = true;
        }

        if (screen instanceof InventoryScreen || screen instanceof GuiCosArmorInventory) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
            Minecraft mc = Minecraft.getInstance();

            if (!ModConfigs.getCosArmorGuiButton_Hidden()) {
                addWidget.accept(new GuiCosArmorButton(
                        accessor.getLeftPos() + ModConfigs.getCosArmorGuiButton_Left()/* 65 */,
                        accessor.getTopPos() + ModConfigs.getCosArmorGuiButton_Top()/* 67 */,
                        10, 10,
                        screen instanceof GuiCosArmorInventory ? Component.translatable("cos.gui.buttonnormal")
                                : Component.translatable("cos.gui.buttoncos"),
                        button -> {
                            if (containerScreen instanceof GuiCosArmorInventory) {
                                InventoryScreen newGui = new InventoryScreen(mc.player);
                                InventoryScreenAccess.setXMouse(newGui,
                                        ((GuiCosArmorInventory) containerScreen).oldMouseX);
                                InventoryScreenAccess.setYMouse(newGui,
                                        ((GuiCosArmorInventory) containerScreen).oldMouseY);
                                mc.setScreen(newGui);
                                PlatformHelper.sendToServer(new PayloadOpenNormalInventory());
                            } else {
                                PlatformHelper.sendToServer(new PayloadOpenCosArmorInventory());
                            }
                        },
                        null));
            }
            if (!ModConfigs.getCosArmorToggleButton_Hidden()) {
                addWidget.accept(new GuiCosArmorToggleButton(
                        accessor.getLeftPos() + ModConfigs.getCosArmorToggleButton_Left()/* 59 */,
                        accessor.getTopPos() + ModConfigs.getCosArmorToggleButton_Top()/* 72 */,
                        5, 5,
                        Component.empty(),
                        PlayerRenderHandler.Disabled ? 1 : 0,
                        button -> {
                            PlayerRenderHandler.Disabled = !PlayerRenderHandler.Disabled;
                            ((GuiCosArmorToggleButton) button).state = PlayerRenderHandler.Disabled ? 1 : 0;
                        }));
            }
        } else if (screen instanceof CreativeModeInventoryScreen) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;

            if (!ModConfigs.getCosArmorCreativeGuiButton_Hidden()) {
                addWidget.accept(new GuiCosArmorButton(
                        /* screen.leftPos + */ModConfigs.getCosArmorCreativeGuiButton_Left()/* 95 */,
                        accessor.getTopPos() + ModConfigs.getCosArmorCreativeGuiButton_Top()/* 38 */,
                        10, 10,
                        Component.translatable("cos.gui.buttoncos"),
                        button -> {
                            PlatformHelper.sendToServer(new PayloadOpenCosArmorInventory());
                        },
                        (button, isInventoryOpen) -> {
                            button.visible = isInventoryOpen;
                        }));
            }
        }
    }

    public void registerEvents() {
    }

    public void registerMenuScreens(Object event) {
    }

}
