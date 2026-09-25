package lain.mods.cos.impl.network;

import lain.mods.cos.impl.InventoryManager;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.network.payload.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModPayloads {

    public static void handleSyncCosArmor(PayloadSyncCosArmor p, Runnable enqueue) {
        enqueue.run();
        ModObjects.invMan.getCosArmorInventoryClient(p.uuid()).setStackInSlot(p.slot(), p.itemCosArmor());
        ModObjects.invMan.getCosArmorInventoryClient(p.uuid()).setSkinArmor(p.slot(), p.isSkinArmor());
    }

    public static void handleSetSkinArmor(PayloadSetSkinArmor p, ServerPlayer player, Runnable enqueue) {
        enqueue.run();
        ModObjects.invMan.getCosArmorInventory(player.getUUID()).setSkinArmor(p.slot(), p.isSkinArmor());
    }

    public static void handleOpenCosArmorInventory(PayloadOpenCosArmorInventory p, ServerPlayer player,
            Runnable enqueue) {
        enqueue.run();
        player.openMenu(ModObjects.invMan.getCosArmorInventory(player.getUUID()));
    }

    public static void handleOpenNormalInventory(PayloadOpenNormalInventory p, ServerPlayer player, Runnable enqueue) {
        enqueue.run();
        player.doCloseContainer();
    }

    public static void handleSyncHiddenFlags(PayloadSyncHiddenFlags p, Runnable enqueue) {
        enqueue.run();
        if (InventoryManager.checkIdentifier(p.modid(), p.identifier())) {
            ModObjects.invMan.getCosArmorInventoryClient(p.uuid()).setHidden(p.modid(), p.identifier(), p.hidden());
        }
    }

    public static void handleSetHiddenFlags(PayloadSetHiddenFlags p, ServerPlayer player, Runnable enqueue) {
        enqueue.run();
        if (InventoryManager.checkIdentifier(p.modid(), p.identifier())) {
            ModObjects.invMan.getCosArmorInventory(player.getUUID()).setHidden(p.modid(), p.identifier(), p.hidden());
        }
    }

}
