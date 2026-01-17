package lain.mods.cos.api.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This is the actual inventory associated with the player. <br>
 * Changes made to server side CAStacks will be sync to the clients. <br>
 * Do not make changes to client side CAStacks, it is not expected, and can
 * cause problems. <br>
 * <br>
 * CosmeticArmorReworkedForked uses 4 slots. <br>
 * Slot 0-3 are {@link net.minecraft.world.entity.EquipmentSlot#FEET FEET},
 * {@link net.minecraft.world.entity.EquipmentSlot#LEGS LEGS},
 * {@link net.minecraft.world.entity.EquipmentSlot#CHEST CHEST},
 * {@link net.minecraft.world.entity.EquipmentSlot#HEAD HEAD}. <br>
 * <br>
 * For toggling visibilities of other mods, use these methods: <br>
 * {@link #setHidden(String, String, boolean) setHidden},
 * {@link #isHidden(String, String) isHidden}, {@link #forEachHidden(BiConsumer)
 * forEachHidden}.
 */
public class CAStacksBase {

    protected final Map<String, Set<String>> hidden = new HashMap<>();

    protected boolean[] isSkinArmor;
    protected NonNullList<ItemStack> stacks;

    public CAStacksBase() {
        this(4);
    }

    public CAStacksBase(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        isSkinArmor = new boolean[size];
    }

    public int getSlots() {
        return stacks.size();
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        isSkinArmor = new boolean[size];
    }

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = stacks.get(slot);

        int limit = getSlotLimit(slot);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }
            return existing.copyWithCount(toExtract);
        }
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onContentsChanged(int slot) {
    }

    protected void onLoad() {
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                if (itemTags.contains("id"))
                    ItemStack.parse(provider, itemTags).ifPresent(stack -> stacks.set(slot, stack));
                if (itemTags.contains("isSkinArmor"))
                    isSkinArmor[slot] = itemTags.getBoolean("isSkinArmor");
            }
        }
        hidden.clear();
        Arrays.stream(nbt.getString("Hidden").split("\0")).forEach(str -> {
            int i = str.indexOf(":");
            if (i != -1)
                hidden.computeIfAbsent(str.substring(0, i), key -> new HashSet<>()).add(str.substring(i + 1));
        });
        onLoad();
    }

    /**
     * Iterates through all set hidden other mods' things.
     *
     * @param consumer the consumer that will be accepting pairs of modid and
     *                 identifier
     */
    public void forEachHidden(BiConsumer<String, String> consumer) {
        for (String modid : hidden.keySet())
            for (String identifier : hidden.get(modid))
                consumer.accept(modid, identifier);
    }

    /**
     * Checks to see if something should be hidden when rendering.
     *
     * @param modid      the modid of the related mod (example: curios)
     * @param identifier the identifier of the related slot (format:
     *                   slotId#slotIndex) (example: ring#0)
     * @return true if the item in the related slot should be hidden when rendering
     */
    public boolean isHidden(String modid, String identifier) {
        return hidden.getOrDefault(modid, Collections.emptySet()).contains(identifier);
    }

    public boolean isSkinArmor(int slot) {
        validateSlotIndex(slot);
        return isSkinArmor[slot];
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty() || isSkinArmor[i]) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                if (!stacks.get(i).isEmpty())
                    itemTag = (CompoundTag) stacks.get(i).save(provider, itemTag);
                if (isSkinArmor[i])
                    itemTag.putBoolean("isSkinArmor", true);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        // writeUTF limit = a 16-bit unsigned integer = 65535 - Should be enough
        nbt.putString("Hidden",
                hidden.entrySet().stream().map(entry -> entry.getValue().stream()
                        .map(value -> entry.getKey() + ":" + value).collect(Collectors.joining("\0")))
                        .collect(Collectors.joining("\0")));
        return nbt;
    }

    /**
     * Sets or removes something from hidden when rendering.
     *
     * @param modid      the modid of the related mod (example: curios)
     * @param identifier the identifer of the related slot (format:
     *                   slotId#slotIndex) (example: ring#0)
     * @param set        true for set, false for remove
     * @return if something changed due to this invocation
     */
    public boolean setHidden(String modid, String identifier, boolean set) {
        if (set)
            return hidden.computeIfAbsent(modid, key -> new HashSet<>()).add(identifier);
        else
            return hidden.getOrDefault(modid, Collections.emptySet()).remove(identifier);
    }

    public void setSkinArmor(int slot, boolean enabled) {
        validateSlotIndex(slot);
        if (isSkinArmor[slot] == enabled)
            return;
        isSkinArmor[slot] = enabled;
        onContentsChanged(slot);
    }

}
