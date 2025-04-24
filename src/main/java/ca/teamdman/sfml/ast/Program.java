package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.config.SFMServerConfig;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.program.*;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.util.*;
import java.util.function.Consumer;

import static ca.teamdman.sfm.common.blockentity.ManagerBlockEntity.TICK_TIME_HISTORY_SIZE;
import static ca.teamdman.sfm.common.net.ServerboundManagerSetLogLevelPacket.MAX_LOG_LEVEL_NAME_LENGTH;

public class Program implements Statement {
    public  ASTBuilder astBuilder;
    public  String name;
    public  List<Trigger> triggers;
    public  Set<String> referencedLabels;
    public  Set<ResourceIdentifier<?, ?, ?>> referencedResources;
    public String name(){
        return name;
    }
    public Program(
            ASTBuilder astBuilder,
            String name,
            List<Trigger> triggers,
            Set<String> referencedLabels,
            Set<ResourceIdentifier<?, ?, ?>> referencedResources
    ) {
        this.astBuilder = astBuilder;
        this.name = name;
        this.triggers = triggers;
        this.referencedLabels = referencedLabels;
        this.referencedResources = referencedResources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Program program = (Program) o;
        return Objects.equals(astBuilder, program.astBuilder) &&
                Objects.equals(name, program.name) &&
                Objects.equals(triggers, program.triggers) &&
                Objects.equals(referencedLabels, program.referencedLabels) &&
                Objects.equals(referencedResources, program.referencedResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(astBuilder, name, triggers, referencedLabels, referencedResources);
    }



    /**
     * This comes from {@link java.io.DataOutputStream#writeUTF(String, DataOutput)}
     * and {@link NetworkHooks#openScreen(ServerPlayer, MenuProvider, Consumer)}
     */
    @SuppressWarnings("JavadocReference")
    public static final int MAX_PROGRAM_LENGTH = 32600 // from openScreen
                                                 - 8 * TICK_TIME_HISTORY_SIZE
                                                 - MAX_LOG_LEVEL_NAME_LENGTH
                                                 - 1 // manager state enum
                                                 - 8; // block pos
    public static final int MAX_LABEL_LENGTH = 256;

    public static void compile(
            String programString,
            Consumer<Program> onSuccess,
            Consumer<List<TextComponentTranslation>> onFailure
    ) {
        ProgramBuilder
                .build(programString)
                .caseSuccess((program, metadata) -> onSuccess.accept(program))
                .caseFailure(result -> onFailure.accept(result.metadata.errors));
    }

    /**
     * Create a context and tick the program.
     *
     * @return {@code true} if a trigger entered its body
     */
    public boolean tick(ManagerBlockEntity manager) {
        ProgramContext context = new ProgramContext(this, manager, new DefaultProgramBehaviour());

        // log if there are unprocessed redstone pulses
        int unprocessedRedstonePulseCount = manager.getUnprocessedRedstonePulseCount();
        if (unprocessedRedstonePulseCount > 0) {
            manager.logger.debug(x -> x.accept(LocalizationKeys.LOG_PROGRAM_TICK_WITH_REDSTONE_COUNT.get(
                    unprocessedRedstonePulseCount)));
        }


        tick(context);

        manager.clearRedstonePulseQueue();

        return context.didSomething();
    }

    @Override
    public List<Statement> getStatements() {
        //noinspection unchecked
        return (List<Statement>) (List<? extends Statement>) triggers;
    }

    @Override
    public void tick(ProgramContext context) {
        LimitedInputSlotObjectPool.checkInvariant();
        LimitedOutputSlotObjectPool.checkInvariant();

        for (Trigger trigger : triggers) {
            // Only process triggers that should tick
            if (!trigger.shouldTick(context)) {
                continue;
            }

            // Set flag and log on first trigger
            if (!context.didSomething()) {
                context.setDidSomething(true);
                context.getLogger().trace(getTraceLogWriter(context));
                context.getLogger().debug(debug -> debug.accept(LocalizationKeys.LOG_PROGRAM_TICK.get()));
            }

            // Log pretty triggers
            if (triggers instanceof ToStringCondensed ) {
                ToStringCondensed ss = (ToStringCondensed) triggers;
                context
                        .getLogger()
                        .debug(x -> x.accept(LocalizationKeys.LOG_PROGRAM_TICK_TRIGGER_STATEMENT.get(
                                ss.toStringCondensed())));
            }

            // Start stopwatch
            long start = System.nanoTime();

            // Perform tick
            if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour ) {
                SimulateExploreAllPathsProgramBehaviour simulation = (SimulateExploreAllPathsProgramBehaviour) context.getBehaviour();
                int maxConditionCount = SFMServerConfig.maxIfStatementsInTriggerBeforeSimulationIsntAllowed;
                int conditionCount = trigger.getConditionCount();
                if (conditionCount <= maxConditionCount) {
                    int numPossibleStates = (int) Math.max(1, Math.pow(2, conditionCount));
                    for (int i = 0; i < numPossibleStates; i++) {
                        ProgramContext forkedContext = context.fork();
                        trigger.tick(forkedContext);
                        forkedContext.free();
                        ((SimulateExploreAllPathsProgramBehaviour) forkedContext.getBehaviour()).terminatePathAndBeginAnew();
                    }
                } else {
                    context.getLogger().warn(LocalizationKeys.PROGRAM_WARNING_TOO_MANY_CONDITIONS.get(
                            trigger.toString(),
                            conditionCount,
                            maxConditionCount
                    ));
                }
                simulation.prepareNextTrigger();
            } else {
                ProgramContext forkedContext = context.fork();
                trigger.tick(forkedContext);
                forkedContext.free();
            }

            // End stopwatch
            long nanoTimePassed = System.nanoTime() - start;

            // Log trigger time
            context.getLogger().info(x -> x.accept(LocalizationKeys.PROGRAM_TICK_TRIGGER_TIME_MS.get(
                    nanoTimePassed / 1_000_000.0,
                    trigger.toString()
            )));
        }

        LimitedInputSlotObjectPool.checkInvariant();
        LimitedOutputSlotObjectPool.checkInvariant();

        if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour ) {
            SimulateExploreAllPathsProgramBehaviour simulation = (SimulateExploreAllPathsProgramBehaviour) context.getBehaviour();
            simulation.onProgramFinished(context, this);
        }
    }

    public int getConditionIndex(IfStatement ifStatement) {
        for (Trigger trigger : triggers) {
            int conditionIndex = trigger.getConditionIndex(ifStatement);
            if (conditionIndex != -1) {
                return conditionIndex;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder rtn = new StringBuilder();
        rtn.append("NAME \"").append(name).append("\"\n");
        for (Trigger trigger : triggers) {
            rtn.append(trigger).append("\n");
        }
        return rtn.toString();
    }

    public void replaceOutputStatement(
            OutputStatement oldStatement,
            OutputStatement newStatement
    ) {
        Deque<Statement> toPatch = new ArrayDeque<>();
        toPatch.add(this);
        while (!toPatch.isEmpty()) {
            Statement statement = toPatch.pollFirst();
            List<Statement> children = statement.getStatements();
            for (int i = 0; i < children.size(); i++) {
                Statement child = children.get(i);
                if (child == oldStatement) {
                    children.set(i, newStatement);
                } else {
                    toPatch.add(child);
                }
            }
        }
    }

    private static @NotNull Consumer<Consumer<TextComponentTranslation>> getTraceLogWriter(ProgramContext context) {
        return trace -> {
            trace.accept(LocalizationKeys.LOG_CABLE_NETWORK_DETAILS_HEADER_1.get());
            trace.accept(LocalizationKeys.LOG_CABLE_NETWORK_DETAILS_HEADER_2.get());
            World level = context
                    .getManager()
                    .getWorld();
            //noinspection DataFlowIssue
            context
                    .getNetwork()
                    .getCablePositions()
                    .map(pos -> "- "
                                + pos.toString()
                                + " "
                                + level
                                        .getBlockState(
                                                pos))
                    .forEach(body -> trace.accept(LocalizationKeys.LOG_CABLE_NETWORK_DETAILS_BODY.get(
                            body)));
            trace.accept(LocalizationKeys.LOG_CABLE_NETWORK_DETAILS_HEADER_3.get());
            //noinspection DataFlowIssue
            context
                    .getNetwork()
                    .getCapabilityProviderPositions()
                    .map(pos -> "- " + pos.toString() + " " + level
                            .getBlockState(pos))
                    .forEach(body -> trace.accept(LocalizationKeys.LOG_CABLE_NETWORK_DETAILS_BODY.get(
                            body)));
            trace.accept(LocalizationKeys.LOG_CABLE_NETWORK_DETAILS_FOOTER.get());

            trace.accept(LocalizationKeys.LOG_LABEL_POSITION_HOLDER_DETAILS_HEADER.get());
            //noinspection DataFlowIssue
            context
                    .getLabelPositionHolder()
                    .labels
                    .forEach((label, positions) -> positions
                            .stream()
                            .map(
                                    pos -> "- "
                                           + label
                                           + ": "
                                           + pos.toString()
                                           + " "
                                           + level
                                                   .getBlockState(
                                                           pos)

                            )
                            .forEach(body -> trace.accept(LocalizationKeys.LOG_LABEL_POSITION_HOLDER_DETAILS_BODY.get(
                                    body))));
            trace.accept(LocalizationKeys.LOG_LABEL_POSITION_HOLDER_DETAILS_FOOTER.get());
            trace.accept(LocalizationKeys.LOG_PROGRAM_CONTEXT.get(context.toString()));
        };
    }

    public static class ListErrorListener extends BaseErrorListener {
        private final List<String> errors;

        public ListErrorListener(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e
        ) {
            errors.add("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }
}
