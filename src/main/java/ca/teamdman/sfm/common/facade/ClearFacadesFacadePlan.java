package ca.teamdman.sfm.common.facade;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.block.IFacadableBlock;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ClearFacadesFacadePlan implements IFacadePlan {

    public Set<BlockPos> positions;

    public ClearFacadesFacadePlan(Set<BlockPos> positions) {
        this.positions=positions;
    }

    @Override
    public void apply(World world) {
        this.positions.forEach(pos -> {
            Block existingBlock = world.getBlockState(pos).getBlock();
            if (existingBlock instanceof IFacadableBlock) {
                IFacadableBlock facadableBlock = (IFacadableBlock) existingBlock;
                IBlockState nextBlockState = facadableBlock
                        .getNonFacadeBlock()
                        .getStateForPlacementByFacadePlan(
                                world,
                                pos,
                                null
                        );
                world.setBlockState(pos, nextBlockState, 3); // Use flag 3 for block updates
            } else {
                SFM.LOGGER.warn("Block {} at {} is not a facadable block", existingBlock, pos);
            }
        });
    }

    @Override
    public Set<BlockPos> positions() {
        return positions;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public @Nullable FacadePlanWarning computeWarning(
            World world
    ) {
        FacadePlanAnalysisResult analysisResult = FacadePlanAnalysisResult.analyze(world, positions);
        if (analysisResult.shouldWarn()) {
            return FacadePlanWarning.of(
                    LocalizationKeys.FACADE_CONFIRM_CLEAR_SCREEN_TITLE.getComponent(),
                    LocalizationKeys.FACADE_CONFIRM_CLEAR_SCREEN_MESSAGE.getComponent(
                            analysisResult.facadeDataToCount.size(),
                            analysisResult.countAffected()
                    )
            );
        }
        return null;
    }
}
