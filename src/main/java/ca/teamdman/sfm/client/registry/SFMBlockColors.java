package ca.teamdman.sfm.client.registry;

import ca.teamdman.sfm.client.render.FacadeBlockColor;
import ca.teamdman.sfm.common.registry.SFMBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SFMBlockColors extends CommonProxy {
    public static void reg() {

        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
//        BlockColors blockColors = new BlockColors();
        FacadeBlockColor blockColor = new FacadeBlockColor();
//        blockColors.registerBlockColorHandler(blockColor, SFMBlocks.CABLE_FACADE_BLOCK, SFMBlocks.FANCY_CABLE_FACADE_BLOCK);
        blockColors.registerBlockColorHandler(blockColor, SFMBlocks.CABLE_FACADE_BLOCK);
    }
    @Override
    public void init() {
        super.init();
        // 客户端独有逻辑
        reg();
    }
}


