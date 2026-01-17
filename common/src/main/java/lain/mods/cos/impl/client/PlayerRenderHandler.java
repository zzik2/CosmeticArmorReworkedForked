package lain.mods.cos.impl.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;

public enum PlayerRenderHandler {

    INSTANCE;

    public static boolean Disabled = false;

    private final LoadingCache<Object, Deque<Runnable>> cache = CacheBuilder.newBuilder().weakKeys()
            .build(new CacheLoader<Object, Deque<Runnable>>() {

                @Override
                public Deque<Runnable> load(Object key) throws Exception {
                    return new ArrayDeque<>();
                }

            });

    private final LoadingCache<Object, ItemStack[]> cosArmorCache = CacheBuilder.newBuilder().weakKeys()
            .build(new CacheLoader<Object, ItemStack[]>() {

                @Override
                public ItemStack[] load(Object key) throws Exception {
                    return new ItemStack[4];
                }

            });

    public void handleLoggedOut() {
        Disabled = false;
    }

    public void handlePreRenderPlayer(Player player) {
        Deque<Runnable> queue = cache.getUnchecked(player);
        restoreItems(queue);
        NonNullList<ItemStack> armor = player.getInventory().armor;

        for (int i = 0; i < armor.size(); i++) {
            int slot = i;
            ItemStack stack = armor.get(slot);
            queue.add(() -> armor.set(slot, stack));
        }

        if (Disabled)
            return;

        InventoryCosArmor invCosArmor = ModObjects.invMan.getCosArmorInventoryClient(player.getUUID());

        if (ModConfigs.getCosArmorStackRendering()) {
            ItemStack[] cosArmor = cosArmorCache.getUnchecked(player);
            for (int i = 0; i < armor.size(); i++) {
                if (invCosArmor.isSkinArmor(i)) {
                    cosArmor[i] = null;
                } else {
                    ItemStack cosStack = invCosArmor.getStackInSlot(i);
                    cosArmor[i] = cosStack.isEmpty() ? null : cosStack.copy();
                }
            }
        } else {
            ItemStack stack;
            for (int i = 0; i < armor.size(); i++) {
                if (invCosArmor.isSkinArmor(i))
                    armor.set(i, ItemStack.EMPTY);
                else if (!(stack = invCosArmor.getStackInSlot(i)).isEmpty())
                    armor.set(i, stack);
            }
        }
    }

    public void handlePostRenderPlayer(Player player) {
        restoreItems(cache.getUnchecked(player));
    }

    public void handlePreRenderPlayerCanceled(Player player) {
        restoreItems(cache.getUnchecked(player));
    }

    public ItemStack[] getCosArmorForStackRendering(Player player) {
        if (!ModConfigs.getCosArmorStackRendering() || Disabled)
            return null;
        return cosArmorCache.getUnchecked(player);
    }

    public void handleRenderHand() {
        Player player = Minecraft.getInstance().player;
        Deque<Runnable> queue = cache.getUnchecked(player);
        restoreItems(queue);
        NonNullList<ItemStack> armor = player.getInventory().armor;

        for (int i = 0; i < armor.size(); i++) {
            int slot = i;
            ItemStack stack = armor.get(slot);
            queue.add(() -> armor.set(slot, stack));
        }

        if (Disabled)
            return;

        InventoryCosArmor invCosArmor = ModObjects.invMan.getCosArmorInventoryClient(player.getUUID());
        ItemStack stack;
        for (int i = 0; i < armor.size(); i++) {
            if (invCosArmor.isSkinArmor(i))
                armor.set(i, ItemStack.EMPTY);
            else if (!(stack = invCosArmor.getStackInSlot(i)).isEmpty())
                armor.set(i, stack);
        }
    }

    public void handleRenderHandCanceled() {
        restoreItems(cache.getUnchecked(Minecraft.getInstance().player));
    }

    public void handleRenderArm() {
        Player player = Minecraft.getInstance().player;
        Deque<Runnable> queue = cache.getUnchecked(player);
        restoreItems(queue);
        NonNullList<ItemStack> armor = player.getInventory().armor;

        for (int i = 0; i < armor.size(); i++) {
            int slot = i;
            ItemStack stack = armor.get(slot);
            queue.add(() -> armor.set(slot, stack));
        }

        if (Disabled)
            return;

        InventoryCosArmor invCosArmor = ModObjects.invMan.getCosArmorInventoryClient(player.getUUID());
        ItemStack stack;
        for (int i = 0; i < armor.size(); i++) {
            if (invCosArmor.isSkinArmor(i))
                armor.set(i, ItemStack.EMPTY);
            else if (!(stack = invCosArmor.getStackInSlot(i)).isEmpty())
                armor.set(i, stack);
        }
    }

    public void handleRenderArmCanceled() {
        restoreItems(cache.getUnchecked(Minecraft.getInstance().player));
    }

    public void registerEvents() {
    }

    private void restoreItems(Deque<Runnable> queue) {
        Runnable runnable;
        while ((runnable = queue.poll()) != null) {
            try {
                runnable.run();
            } catch (Throwable e) {
                ModObjects.logger.error("Failed in restoring client player items", e);
            }
        }
    }

}
