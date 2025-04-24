package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfml.ast.Label;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface LimitedSlot<STACK, ITEM, CAP> {
    ResourceType<STACK, ITEM, CAP> getType();

    CAP getHandler();

    BlockPos getPos();

    Label getLabel();

    EnumFacing getDirection();

    int getSlot();
}
