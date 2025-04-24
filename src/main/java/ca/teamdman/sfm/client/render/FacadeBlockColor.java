package ca.teamdman.sfm.client.render;

import ca.teamdman.sfm.common.blockentity.IFacadeTileEntity;
import ca.teamdman.sfm.common.facade.FacadeData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.Nullable;

public class FacadeBlockColor implements IBlockColor {
    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
        if (world == null || pos == null) return -1;

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IFacadeTileEntity)) return -1;
        IFacadeTileEntity facadeBlockEntity = (IFacadeTileEntity) tileEntity;
        FacadeData facadeData = facadeBlockEntity.getFacadeData();
        if (facadeData == null) return -1;
        IBlockState facadeState = facadeData.facadeBlockState();
        return Minecraft.getMinecraft().getBlockColors().colorMultiplier(facadeState, world, pos, tintIndex);
    }
}
