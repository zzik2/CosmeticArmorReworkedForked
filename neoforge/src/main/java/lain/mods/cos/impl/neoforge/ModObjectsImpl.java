package lain.mods.cos.impl.neoforge;

import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.init.neoforge.NeoForgeCosmeticArmorReworked;
import net.minecraft.world.inventory.MenuType;

public class ModObjectsImpl {

    public static MenuType<ContainerCosArmor> getTypeContainerCosArmor() {
        return NeoForgeCosmeticArmorReworked.typeContainerCosArmor.get();
    }

}
