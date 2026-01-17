package lain.mods.cos.impl;

import dev.architectury.injectables.annotations.ExpectPlatform;
import lain.mods.cos.impl.client.InventoryManagerClient;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.platform.PlatformHelper;
import lain.mods.cos.init.ModConstants;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModObjects {

    public static final Logger logger = LogManager.getLogger(ModConstants.MODID);
    public static final InventoryManager invMan = PlatformHelper.isClient() ? new InventoryManagerClient()
            : new InventoryManager();

    @ExpectPlatform
    public static MenuType<ContainerCosArmor> getTypeContainerCosArmor() {
        throw new AssertionError();
    }

}
