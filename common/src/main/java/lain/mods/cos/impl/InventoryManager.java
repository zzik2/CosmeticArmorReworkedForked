package lain.mods.cos.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lain.mods.cos.api.event.CosArmorDeathDrops;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import lain.mods.cos.impl.network.payload.PayloadSyncCosArmor;
import lain.mods.cos.impl.network.payload.PayloadSyncHiddenFlags;
import lain.mods.cos.impl.platform.PlatformHelper;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

import java.io.File;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class InventoryManager {

    protected static final InventoryCosArmor Dummy = new InventoryCosArmor() {

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public boolean isHidden(String modid, String identifier) {
            return false;
        }

        @Override
        public boolean isSkinArmor(int slot) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
        }

        @Override
        protected void onLoad() {
        }

        @Override
        public boolean setHidden(String modid, String identifier, boolean set) {
            return false;
        }

        @Override
        public void setSkinArmor(int slot, boolean enabled) {
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
        }

        @Override
        public boolean setUpdateListener(ContentsChangeListener listener) {
            return false;
        }

        @Override
        public boolean setUpdateListener(HiddenFlagsChangeListener listener) {
            return false;
        }

    };

    protected static final Random RANDOM = new Random();

    protected final LoadingCache<UUID, InventoryCosArmor> CommonCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<UUID, InventoryCosArmor>() {

                @Override
                public InventoryCosArmor load(UUID key) throws Exception {
                    InventoryCosArmor inventory = new InventoryCosArmor();
                    inventory.setUpdateListener((inv, slot) -> onInventoryChanged(key, inv, slot));
                    inventory.setUpdateListener(
                            (inv, modid, identifier) -> onHiddenFlagsChanged(key, inv, modid, identifier));
                    loadInventory(key, inventory);
                    return inventory;
                }

            });

    public static boolean checkIdentifier(String modid, String identifier) {
        if (modid == null || modid.isEmpty() || identifier == null || identifier.isEmpty()
                || !PlatformHelper.isModLoaded(modid))
            return false;

        return true;
    }

    public ContainerCosArmor createContainerClient(int windowId, Inventory invPlayer) {
        throw new UnsupportedOperationException();
    }

    public InventoryCosArmor getCosArmorInventory(UUID uuid) {
        if (uuid == null)
            return Dummy;
        return CommonCache.getUnchecked(uuid);
    }

    public InventoryCosArmor getCosArmorInventoryClient(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    protected File getDataFile(UUID uuid) {
        MinecraftServer server = PlatformHelper.getServer();
        return new File(PlatformHelper.getPlayerDataDir(server).toFile(), uuid + ".cosarmor");
    }

    public void handlePlayerDrops(Player player, Consumer<ItemEntity> addDrop, boolean isCanceled) {
        if (isCanceled)
            return;
        if (player.isEffectiveAi()
                && !player.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                && !ModConfigs.getCosArmorKeepThroughDeath()) {
            InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
            CosArmorDeathDrops event = new CosArmorDeathDrops(player, inv);
            if (CosArmorDeathDrops.EVENT.invoker().onDeathDrops(event))
                return;
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i).copy();
                if (stack.isEmpty())
                    continue;

                float fX = RANDOM.nextFloat() * 0.75F + 0.125F;
                float fY = RANDOM.nextFloat() * 0.75F;
                float fZ = RANDOM.nextFloat() * 0.75F + 0.125F;
                while (!stack.isEmpty()) {
                    ItemEntity entity = new ItemEntity(player.getCommandSenderWorld(), player.getX() + (double) fX,
                            player.getY() + (double) fY, player.getZ() + (double) fZ,
                            stack.split(RANDOM.nextInt(21) + 10));
                    entity.setDeltaMovement(RANDOM.nextGaussian() * (double) 0.05F,
                            RANDOM.nextGaussian() * (double) 0.05F + (double) 0.2F,
                            RANDOM.nextGaussian() * (double) 0.05F);
                    addDrop.accept(entity);
                }

                inv.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void handlePlayerLoggedIn(Player player) {
        CommonCache.invalidate(player.getUUID());
        getCosArmorInventory(player.getUUID());

        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            MinecraftServer server = PlatformHelper.getServer();
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == serverPlayer)
                    continue;
                UUID uuid = other.getUUID();
                InventoryCosArmor inv = getCosArmorInventory(uuid);
                for (int i = 0; i < inv.getSlots(); i++)
                    PlatformHelper.sendToPlayer(serverPlayer, new PayloadSyncCosArmor(uuid, inv, i));
                inv.forEachHidden((modid, identifier) -> PlatformHelper.sendToPlayer(serverPlayer,
                        new PayloadSyncHiddenFlags(uuid, inv, modid, identifier)));
            }
        }
    }

    public void handlePlayerLoggedOut(Player player) {
        UUID uuid;
        InventoryCosArmor inv;
        if ((inv = CommonCache.getIfPresent(uuid = player.getUUID())) != null) {
            saveInventory(uuid, inv);
            CommonCache.invalidate(uuid);
        }
    }

    public void registerCommands(
            com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("clearcosarmor").requires(s -> {
            return s.hasPermission(2);
        }).executes(s -> {
            int count = 0;
            ServerPlayer player = s.getSource().getPlayerOrException();
            InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
            for (int i = 0; i < inv.getSlots(); i++)
                count += inv.extractItem(i, Integer.MAX_VALUE, false).getCount();

            final int result = count;
            s.getSource().sendSuccess(() -> Component.translatable("cos.command.clearcosarmor.success.single", result,
                    player.getDisplayName()), true);
            return result;
        }).then(Commands.argument("targets", EntityArgument.players()).executes(s -> {
            int count = 0;
            Collection<ServerPlayer> players = EntityArgument.getPlayers(s, "targets");
            for (ServerPlayer player : players) {
                InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
                for (int i = 0; i < inv.getSlots(); i++)
                    count += inv.extractItem(i, Integer.MAX_VALUE, false).getCount();
            }

            final int result = count;
            if (players.size() == 1)
                s.getSource().sendSuccess(() -> Component.translatable("cos.command.clearcosarmor.success.single",
                        result, players.iterator().next().getDisplayName()), true);
            else
                s.getSource().sendSuccess(() -> Component.translatable("cos.command.clearcosarmor.success.multiple",
                        result, players.size()), true);
            return result;
        })));

        if (!ModConfigs.getCosArmorDisableCosHatCommand()) {
            dispatcher.register(Commands.literal("coshat").requires(s -> {
                return s.hasPermission(0);
            }).executes(s -> {
                ServerPlayer player = s.getSource().getPlayerOrException();
                InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
                ItemStack stack1 = player.getItemBySlot(EquipmentSlot.MAINHAND);
                ItemStack stack2 = inv.getStackInSlot(3);
                player.setItemSlot(EquipmentSlot.MAINHAND, stack2);
                inv.setStackInSlot(3, stack1);
                return 0;
            }));
        }
    }

    public void handleSaveToFile(UUID playerUUID) {
        InventoryCosArmor inv;
        if ((inv = CommonCache.getIfPresent(playerUUID)) != null)
            saveInventory(playerUUID, inv);
    }

    public void handleServerStopping() {
        ModObjects.logger.debug("Server is stopping... try to save all still loaded CosmeticArmor data");
        CommonCache.asMap().entrySet().forEach(e -> {
            ModObjects.logger.debug(e.getKey());
            saveInventory(e.getKey(), e.getValue());
        });
        CommonCache.invalidateAll();
    }

    protected void loadInventory(UUID uuid, InventoryCosArmor inventory) {
        if (inventory == Dummy)
            return;
        try {
            File file;
            MinecraftServer server = PlatformHelper.getServer();
            if ((file = getDataFile(uuid)).exists())
                inventory.deserializeNBT(server.registryAccess(), NbtIo.read(file.toPath()));
        } catch (Throwable t) {
            ModObjects.logger.fatal("Failed to load CosmeticArmor data", t);
        }
    }

    protected void onHiddenFlagsChanged(UUID uuid, InventoryCosArmor inventory, String modid, String identifier) {
        MinecraftServer server = PlatformHelper.getServer();
        if (server.isDedicatedServer())
            PlatformHelper.sendToAllPlayers(new PayloadSyncHiddenFlags(uuid, inventory, modid, identifier));
        else
            server.getPlayerList().getPlayers().forEach(player -> PlatformHelper.sendToPlayer(player,
                    new PayloadSyncHiddenFlags(uuid, inventory, modid, identifier)));
    }

    protected void onInventoryChanged(UUID uuid, InventoryCosArmor inventory, int slot) {
        MinecraftServer server = PlatformHelper.getServer();
        if (server.isDedicatedServer())
            PlatformHelper.sendToAllPlayers(new PayloadSyncCosArmor(uuid, inventory, slot));
        else
            server.getPlayerList().getPlayers().forEach(
                    player -> PlatformHelper.sendToPlayer(player, new PayloadSyncCosArmor(uuid, inventory, slot)));
    }

    public void registerEvents() {
    }

    public void registerEventsClient() {
        throw new UnsupportedOperationException();
    }

    protected void saveInventory(UUID uuid, InventoryCosArmor inventory) {
        if (inventory == Dummy)
            return;
        try {
            MinecraftServer server = PlatformHelper.getServer();
            NbtIo.write(inventory.serializeNBT(server.registryAccess()), getDataFile(uuid).toPath());
        } catch (Throwable t) {
            ModObjects.logger.fatal("Failed to save CosmeticArmor data", t);
        }
    }

}
