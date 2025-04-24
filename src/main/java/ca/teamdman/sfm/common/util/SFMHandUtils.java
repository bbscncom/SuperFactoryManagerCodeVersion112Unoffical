package ca.teamdman.sfm.common.util;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.jetbrains.annotations.Nullable;

public class SFMHandUtils {
    public static @Nullable SFMHandUtils.ItemStackInHand getItemAndHand(
            EntityPlayerSP player,
            Item seeking
    ) {
        ItemStack mainHandItem = player.getHeldItemMainhand();
        if (mainHandItem.getItem() == seeking) {
            return new ItemStackInHand(mainHandItem, EnumHand.MAIN_HAND);
        } else {
            ItemStack offhandItem = player.getHeldItemOffhand();
            if (offhandItem.getItem() == seeking) {
                return new ItemStackInHand(offhandItem, EnumHand.OFF_HAND);
            }
        }
        return null;
    }

    public static ItemStack getItemInEitherHand(
            EntityPlayerSP player,
            Item seeking
    ) {
        if (player.getHeldItemMainhand().getItem() == seeking) {
            return player.getHeldItemMainhand();
        } else if (player.getHeldItemOffhand().getItem() == seeking) {
            return player.getHeldItemOffhand();
        }
        return ItemStack.EMPTY;
    }

    public static @Nullable EnumHand getHandHoldingItem(
            EntityPlayerSP player,
            Item seeking
    ) {
        if (player.getHeldItemMainhand().getItem() == seeking) {
            return EnumHand.MAIN_HAND;
        } else if (player.getHeldItemOffhand().getItem() == seeking) {
            return EnumHand.OFF_HAND;
        }
        return null;
    }

    public static class ItemStackInHand{
        public ItemStack stack;
        public EnumHand hand;

        public ItemStackInHand(ItemStack itemStack, EnumHand enumHand) {
            this.stack=itemStack;
            this.hand=enumHand;
        }
    }
}
