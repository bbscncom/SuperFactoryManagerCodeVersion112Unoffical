package ca.teamdman.sfm.common.block;

import ca.teamdman.sfm.common.facade.FacadeTransparency;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IFacadableBlock {
    IFacadableBlock getNonFacadeBlock();

    IFacadableBlock getFacadeBlock();

    IBlockState getStateForPlacementByFacadePlan(
            World world,
            BlockPos pos,
            @Nullable FacadeTransparency facadeTransparency
    );
}
