package zzik2.cosmeticarmorreworkedforked.fabric.client;

import lain.mods.cos.init.fabric.FabricCosmeticArmorReworkedClient;
import net.fabricmc.api.ClientModInitializer;

public final class CosmeticarmorreworkedForkedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as
        // rendering.
        new FabricCosmeticArmorReworkedClient().onInitializeClient();
    }
}
