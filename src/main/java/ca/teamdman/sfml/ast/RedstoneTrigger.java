package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;

public class RedstoneTrigger implements Trigger, ToStringCondensed {
    private final Block block;

    public RedstoneTrigger(Block block) {
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public void tick(ProgramContext context) {
        for (int i = 0; i < context.getRedstonePulses(); i++) {
            block.tick(context);
        }
        if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour) {
            ((SimulateExploreAllPathsProgramBehaviour) context.getBehaviour()).onTriggerDropped(context, this);
        }
    }

    @Override
    public boolean shouldTick(ProgramContext context) {
        if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour) return true;
        return context.getManager().getUnprocessedRedstonePulseCount() > 0;
    }

    @Override
    public String toString() {
        return "EVERY REDSTONE PULSE DO\n" + block.toString() + "\nEND";
    }

    @Override
    public String toStringCondensed() {
        return "EVERY REDSTONE PULSE DO";
    }
}
