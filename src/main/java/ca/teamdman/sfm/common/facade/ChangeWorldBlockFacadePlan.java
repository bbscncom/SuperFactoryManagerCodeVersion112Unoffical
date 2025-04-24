package ca.teamdman.sfm.common.facade;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.block.IFacadableBlock;
import ca.teamdman.sfm.common.blockentity.IFacadeTileEntity;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ChangeWorldBlockFacadePlan implements IFacadePlan {
    public IFacadableBlock worldBlock;
    public Set<BlockPos> positions;

    public ChangeWorldBlockFacadePlan(IFacadableBlock facadeBlock, Set<BlockPos> positions) {
        this.worldBlock=facadeBlock;
        this.positions=positions;
    }

    @Override
    public void apply(World world) {
        this.positions.forEach(pos -> {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof IFacadeTileEntity) {
                IFacadeTileEntity oldFacadeBlockEntity = (IFacadeTileEntity) tileEntity;
                // this position already has a facade

                // get the old state
                IBlockState oldState = world.getBlockState(pos);
                FacadeData oldFacadeData = oldFacadeBlockEntity.getFacadeData();

                // if the old state is valid, we can set the new world block and restore the facade
                if (oldFacadeData != null && oldState.getPropertyKeys().contains(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY)) {
                    world.setBlockState(
                            pos,
                            this.worldBlock.getFacadeBlock().getStateForPlacementByFacadePlan(
                                    world,
                                    pos,
                                    oldState.getValue(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY)
                            ),
                            3 // Use flag 3 for block updates
                    );
                    TileEntity newTileEntity = world.getTileEntity(pos);
                    if (newTileEntity instanceof IFacadeTileEntity) {
                        IFacadeTileEntity facadeBlockEntity = (IFacadeTileEntity) newTileEntity;
                        facadeBlockEntity.updateFacadeData(oldFacadeData);
                    } else {
                        SFM.LOGGER.warn("Block entity {} at {} is not a facade block entity", pos, newTileEntity);
                    }
                }
            } else {
                // there was no old facade, just set the new world block
                world.setBlockState(
                        pos,
                        this.worldBlock.getNonFacadeBlock().getStateForPlacementByFacadePlan(
                                world,
                                pos,
                                null
                        ),
                        3 // Use flag 3 for block updates
                );
            }
        });
    }

    @Override
    public Set<BlockPos> positions() {
        return positions;
    }

    @Override
    public @Nullable FacadePlanWarning computeWarning(World world) {
        FacadePlanAnalysisResult analysisResult = FacadePlanAnalysisResult.analyze(world, positions);
        if (analysisResult.shouldWarn()) {
            return FacadePlanWarning.of(
                    LocalizationKeys.FACADE_CONFIRM_CHANGE_WORLD_BLOCK_SCREEN_TITLE.getComponent(),
                    LocalizationKeys.FACADE_CONFIRM_CHANGE_WORLD_BLOCK_SCREEN_MESSAGE.getComponent(
                            analysisResult.countAffected()
                    )
            );
        }
        return null;
    }
}
