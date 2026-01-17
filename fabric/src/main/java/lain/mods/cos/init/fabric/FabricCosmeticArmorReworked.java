package lain.mods.cos.init.fabric;

import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.InventoryManagerClient;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.network.ModPayloads;
import lain.mods.cos.impl.network.payload.*;
import lain.mods.cos.impl.platform.fabric.PlatformHelperImpl;
import lain.mods.cos.init.ModConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class FabricCosmeticArmorReworked implements ModInitializer {

    public static MenuType<ContainerCosArmor> typeContainerCosArmor;

    @Override
    public void onInitialize() {
        typeContainerCosArmor = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(ModConstants.MODID, "inventorycosarmor"),
                new MenuType<>(ModObjects.invMan::createContainerClient, FeatureFlags.VANILLA_SET)
        );

        ModConfigs.registerConfigs();

        // Register Payloads
        PayloadTypeRegistry.playS2C().register(PayloadSyncCosArmor.TYPE, PayloadSyncCosArmor.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PayloadSyncHiddenFlags.TYPE, PayloadSyncHiddenFlags.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PayloadSetSkinArmor.TYPE, PayloadSetSkinArmor.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PayloadOpenCosArmorInventory.TYPE, PayloadOpenCosArmorInventory.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PayloadOpenNormalInventory.TYPE, PayloadOpenNormalInventory.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PayloadSetHiddenFlags.TYPE, PayloadSetHiddenFlags.STREAM_CODEC);

        // Server handlers
        ServerPlayNetworking.registerGlobalReceiver(PayloadSetSkinArmor.TYPE, (payload, context) -> {
            ModPayloads.handleSetSkinArmor(payload, context.player(), () -> context.server().execute(() -> {
            }));
        });
        ServerPlayNetworking.registerGlobalReceiver(PayloadOpenCosArmorInventory.TYPE, (payload, context) -> {
            ModPayloads.handleOpenCosArmorInventory(payload, context.player(), () -> context.server().execute(() -> {
            }));
        });
        ServerPlayNetworking.registerGlobalReceiver(PayloadOpenNormalInventory.TYPE, (payload, context) -> {
            ModPayloads.handleOpenNormalInventory(payload, context.player(), () -> context.server().execute(() -> {
            }));
        });
        ServerPlayNetworking.registerGlobalReceiver(PayloadSetHiddenFlags.TYPE, (payload, context) -> {
            ModPayloads.handleSetHiddenFlags(payload, context.player(), () -> context.server().execute(() -> {
            }));
        });

        // Server events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            PlatformHelperImpl.setServer(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ModObjects.invMan.handleServerStopping();
            PlatformHelperImpl.setServer(null);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModObjects.invMan.handlePlayerLoggedIn(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ModObjects.invMan.handlePlayerLoggedOut(handler.getPlayer());
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                ModObjects.invMan.handlePlayerDrops(player, drop -> entity.level().addFreshEntity(drop), false);
            }
            return true;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModObjects.invMan.registerCommands(dispatcher);
        });
    }

}
