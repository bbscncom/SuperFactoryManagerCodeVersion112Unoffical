package ca.teamdman.sfm.common.registry;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SFMCreativeTabs {

    public static CreativeTabs SFMTAB = new CreativeTabs("sfm") {
        public ItemStack createIcon() {
            return new ItemStack(SFMItems.LABEL_GUN_ITEM);
        }
    };

}
