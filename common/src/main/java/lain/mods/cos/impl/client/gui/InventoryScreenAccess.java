package lain.mods.cos.impl.client.gui;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import zzik2.cosmeticarmorreworkedforked.mixin.InventoryScreenAccessor;

public class InventoryScreenAccess {

    public static float getXMouse(InventoryScreen screen) {
        return ((InventoryScreenAccessor) screen).getXMouse();
    }

    public static float getYMouse(InventoryScreen screen) {
        return ((InventoryScreenAccessor) screen).getYMouse();
    }

    public static void setXMouse(InventoryScreen screen, float xMouse) {
        ((InventoryScreenAccessor) screen).setXMouse(xMouse);
    }

    public static void setYMouse(InventoryScreen screen, float yMouse) {
        ((InventoryScreenAccessor) screen).setYMouse(yMouse);
    }
}
