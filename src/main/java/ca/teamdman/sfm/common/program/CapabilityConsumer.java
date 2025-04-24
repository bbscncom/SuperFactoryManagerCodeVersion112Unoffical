package ca.teamdman.sfm.common.program;

import ca.teamdman.sfml.ast.Label;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface CapabilityConsumer<T> {
    void accept(
            Label label,
            BlockPos pos,
            EnumFacing direction,
            T cap
    );
}
