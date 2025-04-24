package ca.teamdman.sfm.common.facade;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.block.IFacadableBlock;
import ca.teamdman.sfm.common.blockentity.IFacadeTileEntity;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ApplyFacadesFacadePlan implements IFacadePlan {
    public final FacadeData facadeData;
    public final FacadeTransparency facadeTransparency;
    public final Set<BlockPos> positions;

    public ApplyFacadesFacadePlan(FacadeData data, FacadeTransparency transparency, Set<BlockPos> positions) {
        this.facadeData = data;
        this.facadeTransparency = transparency;
        this.positions = positions;
    }

    @Override
    public void apply(World world) {
        this.positions().forEach(pos -> {
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block instanceof IFacadableBlock ) {
                IFacadableBlock facadableBlock = (IFacadableBlock) block;
                IBlockState nextBlockState = facadableBlock.getFacadeBlock().getStateForPlacementByFacadePlan(
                        world,
                        pos,
                        this.facadeTransparency
                );
//                Block.UPDATE_IMMEDIATE | Block.UPDATE_CLIENTS
                world.setBlockState(pos, nextBlockState, 3);
                TileEntity blockEntity = world.getTileEntity(pos);
                if (blockEntity instanceof IFacadeTileEntity ) {
                    IFacadeTileEntity facadeBlockEntity = (IFacadeTileEntity) blockEntity;
                    facadeBlockEntity.updateFacadeData(this.facadeData);
                } else {
                    SFM.LOGGER.warn("Block entity {} at {} is not a facade block entity", pos, blockEntity);
                }
            } else {
                SFM.LOGGER.warn("Block {} at {} is not a facadable block", block, pos);
            }
        });
    }

    @Override
    public Set<BlockPos> positions() {
        return positions;
    }

    @Override
    public @Nullable FacadePlanWarning computeWarning(
            World world
    ) {
        FacadePlanAnalysisResult analysisResult = FacadePlanAnalysisResult.analyze(world, positions);
        if (analysisResult.shouldWarn()) {
            return FacadePlanWarning.of(
                    LocalizationKeys.FACADE_CONFIRM_APPLY_SCREEN_TITLE.getComponent(),
                    LocalizationKeys.FACADE_CONFIRM_APPLY_SCREEN_MESSAGE.getComponent(
                            analysisResult.facadeDataToCount.size(),
                            analysisResult.countAffected()
                    )
            );
        }
        return null;
    }
}
