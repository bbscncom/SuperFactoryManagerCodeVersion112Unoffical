package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;

import java.util.Collections;
import java.util.List;

public class TimerTrigger implements Trigger, ToStringCondensed {
    private final Interval interval;
    private final Block block;

    public TimerTrigger(Interval interval, Block block) {
        this.interval = interval;
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean shouldTick(ProgramContext context) {
        if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour) return true;
        return interval.shouldTick(context);
    }

    @Override
    public void tick(ProgramContext context) {
        block.tick(context);
        if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour) {
            ((SimulateExploreAllPathsProgramBehaviour) context.getBehaviour()).onTriggerDropped(context, this);
        }
    }

    @Override
    public List<Statement> getStatements() {
        return Collections.singletonList(block);
    }

    public boolean usesOnlyForgeEnergyResourceIO() {
        return getReferencedIOResourceIds().allMatch(id -> id.resourceTypeNamespace.equals("sfm")
                && (id.resourceTypeName.equals("forge_energy")
                || id.resourceTypeName.equals("mekanism_energy")));
    }

    @Override
    public String toString() {
        return "EVERY " + interval + " DO\n" + block.toString() + "\nEND";
    }

    @Override
    public String toStringCondensed() {
        return "EVERY " + interval + " DO";
    }
}
