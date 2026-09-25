package lain.mods.cos.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import lain.mods.cos.impl.client.gui.GuiCosArmorInventory;
import lain.mods.cos.impl.network.payload.PayloadOpenCosArmorInventory;
import lain.mods.cos.impl.platform.PlatformHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public enum KeyHandler {

    INSTANCE;

    private final Minecraft mc = Minecraft.getInstance();

    public KeyMapping keyOpenCosArmorInventory = new KeyMapping("cos.key.opencosarmorinventory",
            InputConstants.UNKNOWN.getValue(), "key.categories.inventory");

    public void handleClientTick() {
        if (!mc.isWindowActive())
            return;
        if (keyOpenCosArmorInventory.consumeClick() && !(mc.screen instanceof GuiCosArmorInventory))
            PlatformHelper.sendToServer(new PayloadOpenCosArmorInventory());
    }

    public void registerEvents() {
    }

    public void registerKeyMappings(Consumer<KeyMapping> register) {
        register.accept(keyOpenCosArmorInventory);
    }

}
