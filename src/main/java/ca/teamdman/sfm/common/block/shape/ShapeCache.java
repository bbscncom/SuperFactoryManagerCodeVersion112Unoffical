package ca.teamdman.sfm.common.block.shape;


import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ShapeCache {
//    private static final Map<IBlockState, VoxelShape> STORAGE = new HashMap<>();
    private static final Map<IBlockState, AxisAlignedBB> STORAGE = new HashMap<>();

    private ShapeCache() {
    }

    public static AxisAlignedBB getOrCompute(IBlockState state, Function<IBlockState, AxisAlignedBB> computeFunction) {
        return STORAGE.computeIfAbsent(state, computeFunction);
    }
}
