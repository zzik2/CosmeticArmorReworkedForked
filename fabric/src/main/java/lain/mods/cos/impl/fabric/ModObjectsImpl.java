package lain.mods.cos.impl.fabric;

import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.init.fabric.FabricCosmeticArmorReworked;
import net.minecraft.world.inventory.MenuType;

public class ModObjectsImpl {

    public static MenuType<ContainerCosArmor> getTypeContainerCosArmor() {
        return FabricCosmeticArmorReworked.typeContainerCosArmor;
    }

}
