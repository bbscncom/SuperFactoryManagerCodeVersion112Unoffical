package ca.teamdman.sfm.client.render;

import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;

public class BlockStateWrapper implements Comparable<BlockStateWrapper> {

    public IBlockState blockState;

    public BlockStateWrapper(IBlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public int compareTo(@NotNull BlockStateWrapper o) {
        return 0;
    }
}
