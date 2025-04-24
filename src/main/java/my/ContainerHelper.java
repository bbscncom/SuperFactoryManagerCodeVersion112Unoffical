package my;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import java.util.List;

public class ContainerHelper {
    public static final String TAG_ITEMS = "Items";

    public static ItemStack removeItem(List<ItemStack> pStacks, int pIndex, int pAmount) {
        return pIndex >= 0 && pIndex < pStacks.size() && !pStacks.get(pIndex).isEmpty() && pAmount > 0
            ? pStacks.get(pIndex).splitStack(pAmount)
            : ItemStack.EMPTY;
    }

    public static ItemStack takeItem(List<ItemStack> pStacks, int pIndex) {
        return pIndex >= 0 && pIndex < pStacks.size() ? pStacks.set(pIndex, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    public static NBTTagCompound saveAllItems(NBTTagCompound pTag, NonNullList<ItemStack> pItems, boolean pAlwaysPutTag) {
        NBTTagList listtag = new NBTTagList();
    
        for (int i = 0; i < pItems.size(); i++) {
            ItemStack itemstack = pItems.get(i);
            if (!itemstack.isEmpty()) {
                NBTTagCompound compoundtag = new NBTTagCompound();
                compoundtag.setByte("Slot", (byte)i);
                itemstack.writeToNBT(compoundtag);
                listtag.appendTag(compoundtag);
            }
        }
    
        if (!listtag.isEmpty() || pAlwaysPutTag) {
            pTag.setTag("Items", listtag);
        }
    
        return pTag;
    }

    public static void loadAllItems(NBTTagCompound pTag, NonNullList<ItemStack> pItems) {
        NBTTagList listtag = pTag.getTagList("Items", 10);

        for (int i = 0; i < listtag.tagCount(); i++) {
            NBTTagCompound compoundtag = listtag.getCompoundTagAt(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 0 && j < pItems.size()) {
                pItems.set(j, new ItemStack(compoundtag));
            }
        }
    }

    public static ItemStack loadItemStackFromNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            return null;
        }
        String itemId = nbt.getString("id");
        Item item = Item.getByNameOrId(itemId);
        if (item == null) {
            return null;
        }
        byte count = nbt.getByte("Count");
        short damage = nbt.getShort("Damage");
        ItemStack stack = new ItemStack(item, count, damage);

        if (nbt.hasKey("tag", 10)) {
            stack.setTagCompound(nbt.getCompoundTag("tag"));
        }
        return stack;
    }
//
//    /**
//     * Clears items from the inventory matching a predicate.
//     * @return The amount of items cleared
//     *
//     * @param pMaxItems The maximum amount of items to be cleared. A negative value
//     *                  means unlimited and 0 means count how many items are found
//     *                  that could be cleared.
//     */
//    public static int clearOrCountMatchingItems(Container pContainer, Predicate<ItemStack> pItemPredicate, int pMaxItems, boolean pSimulate) {
//        int i = 0;
//
//        for (int j = 0; j < pContainer.getContainerSize(); j++) {
//            ItemStack itemstack = pContainer.getItem(j);
//            int k = clearOrCountMatchingItems(itemstack, pItemPredicate, pMaxItems - i, pSimulate);
//            if (k > 0 && !pSimulate && itemstack.isEmpty()) {
//                pContainer.setItem(j, ItemStack.EMPTY);
//            }
//
//            i += k;
//        }
//
//        return i;
//    }
//
//    public static int clearOrCountMatchingItems(ItemStack pStack, Predicate<ItemStack> pItemPredicate, int pMaxItems, boolean pSimulate) {
//        if (pStack.isEmpty() || !pItemPredicate.test(pStack)) {
//            return 0;
//        } else if (pSimulate) {
//            return pStack.getCount();
//        } else {
//            int i = pMaxItems < 0 ? pStack.getCount() : Math.min(pMaxItems, pStack.getCount());
//            pStack.shrink(i);
//            return i;
//        }
//    }
}
