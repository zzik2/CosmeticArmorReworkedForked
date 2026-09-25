package zzik2.cosmeticarmorreworkedforked.mixin;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InventoryScreen.class)
public interface InventoryScreenAccessor {

    @Accessor("xMouse")
    float getXMouse();

    @Accessor("yMouse")
    float getYMouse();

    @Accessor("xMouse")
    void setXMouse(float value);

    @Accessor("yMouse")
    void setYMouse(float value);
}
