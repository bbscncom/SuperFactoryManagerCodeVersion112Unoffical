package ca.teamdman.sfm.common.registry;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SFMCreativeTabs {
    public static final CreativeTabs SFMTAB = new CreativeTabs(CreativeTabs.getNextID(), "decorations")
    {
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon()
        {
            return new ItemStack(SFMBlocks.MANAGER_BLOCK, 1, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta());
        }
    };
}
