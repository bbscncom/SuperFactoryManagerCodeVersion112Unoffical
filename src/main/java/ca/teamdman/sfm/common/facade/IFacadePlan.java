package ca.teamdman.sfm.common.facade;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IFacadePlan {
    void apply(World world);
    Set<BlockPos> positions();
    @Nullable FacadePlanWarning computeWarning(World world);
}
