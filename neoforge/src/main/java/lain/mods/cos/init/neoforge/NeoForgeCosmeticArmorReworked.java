package lain.mods.cos.init.neoforge;

import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.GuiHandler;
import lain.mods.cos.impl.client.InventoryManagerClient;
import lain.mods.cos.impl.client.KeyHandler;
import lain.mods.cos.impl.client.PlayerRenderHandler;
import lain.mods.cos.impl.client.gui.GuiCosArmorInventory;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.network.ModPayloads;
import lain.mods.cos.impl.network.payload.*;
import lain.mods.cos.init.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ModConstants.MODID)
public class NeoForgeCosmeticArmorReworked {

    private static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(BuiltInRegistries.MENU,
            ModConstants.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCosArmor>> typeContainerCosArmor = MENU.register(
            "inventorycosarmor",
            () -> new MenuType<>(ModObjects.invMan::createContainerClient, FeatureFlags.VANILLA_SET));

    public NeoForgeCosmeticArmorReworked(IEventBus bus) {
        MENU.register(bus);
        bus.addListener(this::setup);
        bus.addListener(this::setupClient);
        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(this::setupKeyMappings);
            bus.addListener(this::setupMenuScreens);
        }
        bus.addListener(this::setupPayloadHandlers);
        ModConfigs.registerConfigs();
    }

    private void setup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(this::handlePlayerDrops);
        NeoForge.EVENT_BUS.addListener(this::handlePlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::handlePlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::handleSaveToFile);
        NeoForge.EVENT_BUS.addListener(this::handleRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::handleServerStopping);
    }

    private void setupClient(FMLClientSetupEvent event) {
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(this::handleLoggedOutClient);
            NeoForge.EVENT_BUS.addListener(this::handleGuiDrawPre);
            NeoForge.EVENT_BUS.addListener(this::handleGuiInitPost);
            NeoForge.EVENT_BUS.addListener(this::handleClientTick);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGH, this::handlePreRenderPlayer);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOW, this::handlePostRenderPlayer);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, true,
                    this::handlePreRenderPlayerCanceled);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGH, this::handleRenderHand);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, true,
                    this::handleRenderHandCanceled);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGH, this::handleRenderArm);
            NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, true,
                    this::handleRenderArmCanceled);
        }
    }

    private void setupKeyMappings(RegisterKeyMappingsEvent event) {
        KeyHandler.INSTANCE.registerKeyMappings(event::register);
    }

    private void setupMenuScreens(RegisterMenuScreensEvent event) {
        event.register(typeContainerCosArmor.get(), GuiCosArmorInventory::new);
    }

    private void setupPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("5");
        registrar.playToClient(PayloadSyncCosArmor.TYPE, PayloadSyncCosArmor.STREAM_CODEC, (p, c) -> {
            ModPayloads.handleSyncCosArmor(p, () -> c.enqueueWork(() -> {
            }));
        });
        registrar.playToServer(PayloadSetSkinArmor.TYPE, PayloadSetSkinArmor.STREAM_CODEC, (p, c) -> {
            ModPayloads.handleSetSkinArmor(p, (net.minecraft.server.level.ServerPlayer) c.player(),
                    () -> c.enqueueWork(() -> {
                    }));
        });
        registrar.playToServer(PayloadOpenCosArmorInventory.TYPE, PayloadOpenCosArmorInventory.STREAM_CODEC, (p, c) -> {
            ModPayloads.handleOpenCosArmorInventory(p, (net.minecraft.server.level.ServerPlayer) c.player(),
                    () -> c.enqueueWork(() -> {
                    }));
        });
        registrar.playToServer(PayloadOpenNormalInventory.TYPE, PayloadOpenNormalInventory.STREAM_CODEC, (p, c) -> {
            ModPayloads.handleOpenNormalInventory(p, (net.minecraft.server.level.ServerPlayer) c.player(),
                    () -> c.enqueueWork(() -> {
                    }));
        });
        registrar.playToClient(PayloadSyncHiddenFlags.TYPE, PayloadSyncHiddenFlags.STREAM_CODEC, (p, c) -> {
            ModPayloads.handleSyncHiddenFlags(p, () -> c.enqueueWork(() -> {
            }));
        });
        registrar.playToServer(PayloadSetHiddenFlags.TYPE, PayloadSetHiddenFlags.STREAM_CODEC, (p, c) -> {
            ModPayloads.handleSetHiddenFlags(p, (net.minecraft.server.level.ServerPlayer) c.player(),
                    () -> c.enqueueWork(() -> {
                    }));
        });
    }

    // Server Events
    private void handlePlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player) {
            ModObjects.invMan.handlePlayerDrops(player, event.getDrops()::add, event.isCanceled());
        }
    }

    private void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ModObjects.invMan.handlePlayerLoggedIn(event.getEntity());
    }

    private void handlePlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ModObjects.invMan.handlePlayerLoggedOut(event.getEntity());
    }

    private void handleSaveToFile(PlayerEvent.SaveToFile event) {
        ModObjects.invMan.handleSaveToFile(java.util.UUID.fromString(event.getPlayerUUID()));
    }

    private void handleRegisterCommands(RegisterCommandsEvent event) {
        ModObjects.invMan.registerCommands(event.getDispatcher());
    }

    private void handleServerStopping(ServerStoppingEvent event) {
        ModObjects.invMan.handleServerStopping();
    }

    // Client Events
    private void handleLoggedOutClient(ClientPlayerNetworkEvent.LoggingOut event) {
        if (ModObjects.invMan instanceof InventoryManagerClient client) {
            client.handleLoggedOut();
        }
        PlayerRenderHandler.INSTANCE.handleLoggedOut();
    }

    private void handleGuiDrawPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen) {
            GuiHandler.INSTANCE.handleGuiDrawPre(screen);
        }
    }

    private void handleGuiInitPost(ScreenEvent.Init.Post event) {
        GuiHandler.INSTANCE.handleGuiInitPost(event.getScreen(), event::addListener);
    }

    private void handleClientTick(ClientTickEvent.Pre event) {
        KeyHandler.INSTANCE.handleClientTick();
    }

    private void handlePreRenderPlayer(RenderPlayerEvent.Pre event) {
        PlayerRenderHandler.INSTANCE.handlePreRenderPlayer(event.getEntity());
    }

    private void handlePostRenderPlayer(RenderPlayerEvent.Post event) {
        PlayerRenderHandler.INSTANCE.handlePostRenderPlayer(event.getEntity());
    }

    private void handlePreRenderPlayerCanceled(RenderPlayerEvent.Pre event) {
        if (event.isCanceled()) {
            PlayerRenderHandler.INSTANCE.handlePreRenderPlayerCanceled(event.getEntity());
        }
    }

    private void handleRenderHand(RenderHandEvent event) {
        PlayerRenderHandler.INSTANCE.handleRenderHand();
    }

    private void handleRenderHandCanceled(RenderHandEvent event) {
        PlayerRenderHandler.INSTANCE.handleRenderHandCanceled();
    }

    private void handleRenderArm(RenderArmEvent event) {
        PlayerRenderHandler.INSTANCE.handleRenderArm();
    }

    private void handleRenderArmCanceled(RenderArmEvent event) {
        PlayerRenderHandler.INSTANCE.handleRenderArmCanceled();
    }

}
