package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.program.LimitedInputSlot;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.registry.SFMResourceTypes;
import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfm.common.util.SFMASTUtils;
import ca.teamdman.sfml.ast.Number;
import ca.teamdman.sfml.ast.*;
import io.netty.buffer.ByteBuf;
import my.Tools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ServerboundOutputInspectionRequestPacket implements SFMMessage<ServerboundOutputInspectionRequestPacket, IMessage> {
    public String programString;
    public int outputNodeIndex;
    private static final int MAX_RESULTS_LENGTH = 20480;

    public ServerboundOutputInspectionRequestPacket() {
    }

    public ServerboundOutputInspectionRequestPacket(String programString, int nodeIndex) {
        this.programString=programString;
        this.outputNodeIndex=nodeIndex;
    }

    public static String getOutputStatementInspectionResultsString(
            ManagerBlockEntity manager,
            Program successProgram,
            OutputStatement outputStatement
    ) {
        StringBuilder payload = new StringBuilder();
        payload.append(outputStatement.toStringPretty()).append("\n");
        payload.append("-- predictions may differ from actual execution results\n");

        AtomicInteger branchCount = new AtomicInteger(0);
        successProgram.replaceOutputStatement(outputStatement, new OutputStatement(
                outputStatement.labelAccess(),
                outputStatement.resourceLimits(),
                outputStatement.each()
        ) {
            @Override
            public void tick(ProgramContext context) {
                if (!(context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour)) {
                    throw new IllegalStateException("Expected behaviour to be SimulateExploreAllPathsProgramBehaviour");
                }
                SimulateExploreAllPathsProgramBehaviour behaviour = (SimulateExploreAllPathsProgramBehaviour) context.getBehaviour();
                StringBuilder branchPayload = new StringBuilder();

                payload
                        .append("-- POSSIBILITY ")
                        .append(branchCount.getAndIncrement())
                        .append(" --");
                if (behaviour.getCurrentPath().streamBranches().allMatch(SimulateExploreAllPathsProgramBehaviour.Branch::wasTrue)) {
                    payload.append(" all true\n");
                } else if (behaviour
                        .getCurrentPath()
                        .streamBranches()
                        .allMatch(branch -> !branch.wasTrue())) {
                    payload.append(" all false\n");
                } else {
                    payload.append('\n');
                }
                behaviour.getCurrentPath()
                        .streamBranches()
                        .forEach(branch -> {
                            if (branch.wasTrue()) {
                                payload
                                        .append(branch.ifStatement().condition.toStringPretty())
                                        .append(" -- true");
                            } else {
                                payload
                                        .append(branch.ifStatement().condition.toStringPretty())
                                        .append(" -- false");
                            }
                            payload.append("\n");
                        });
                payload.append("\n");


                branchPayload.append("-- predicted inputs:\n");
                List<Pair<LimitedInputSlot<?, ?, ?>, LabelAccess>> inputSlots = new ArrayList<>();
                context
                        .getInputs()
                        .forEach(inputStatement -> inputStatement.gatherSlots(
                                context,
                                slot -> inputSlots.add(new Pair<>(
                                        slot,
                                        inputStatement.labelAccess()
                                ))
                        ));
                List<InputStatement> inputStatements = inputSlots.stream()
                        .map(slot -> SFMASTUtils.getInputStatementForSlot(slot.a, slot.b))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                if (inputStatements.isEmpty()) {
                    branchPayload.append("none\n-- predicted outputs:\nnone");
                } else {
                    inputStatements.stream()
                            .map(InputStatement::toStringPretty)
                            .map(x -> x + "\n")
                            .forEach(branchPayload::append);

                    branchPayload.append(
                            "-- predicted outputs:\n");
                    ResourceLimits condensedResourceLimits;
                    {
                        ResourceLimits resourceLimits = new ResourceLimits(
                                inputSlots
                                        .stream()
                                        .map(slot -> slot.a)
                                        .map(ServerboundOutputInspectionRequestPacket::getSlotResource)
                                        .collect(Collectors.toList()),
                                ResourceIdSet.EMPTY
                        );
                        List<ResourceLimit> condensedResourceLimitList = new ArrayList<>();
                        for (ResourceLimit resourceLimit : resourceLimits.resourceLimitList()) {
                            // check if an existing resource limit has the same resource identifier
                            Optional<ResourceLimit> first = condensedResourceLimitList
                                    .stream()
                                    .filter(x -> x
                                            .resourceIds()
                                            .equals(resourceLimit.resourceIds()))
                                    .findFirst();
                            if (first.isPresent()) {
                                ResourceLimit resourceLimit1 = first.get();
                                int i = condensedResourceLimitList.indexOf(resourceLimit1);
                                ResourceLimit newLimit = resourceLimit1.withLimit(new Limit(
                                        resourceLimit1
                                                .limit()
                                                .quantity()
                                                .add(resourceLimit.limit().quantity()),
                                        ResourceQuantity.MAX_QUANTITY
                                ));
                                condensedResourceLimitList.set(i, newLimit);
                            } else {
                                condensedResourceLimitList.add(resourceLimit);
                            }
                        }
                        {
                            // prune items not covered by the output resource limits
                            ListIterator<ResourceLimit> iter = condensedResourceLimitList.listIterator();
                            while (iter.hasNext()) {
                                ResourceLimit resourceLimit = iter.next();
                                if (resourceLimit.resourceIds().size() != 1) {
                                    throw new IllegalStateException(
                                            "Expected resource limit to have exactly one resource id");
                                }
                                ResourceIdentifier<?, ?, ?> resourceId = resourceLimit
                                        .resourceIds()
                                        .stream()
                                        .iterator()
                                        .next();

                                // because these resource limits were generated from resource stacks
                                // they should always be valid resource locations (not patterns)
                                ResourceLocation resourceLimitLocation =
                                        new ResourceLocation(resourceId.resourceNamespace,resourceId.resourceName);
                                long accept = outputStatement
                                        .resourceLimits()
                                        .resourceLimitList()
                                        .stream()
                                        .filter(outputResourceLimit -> outputResourceLimit
                                                .resourceIds()
                                                .anyMatchResourceLocation(
                                                        resourceLimitLocation)
                                                && outputStatement
                                                .resourceLimits()
                                                .exclusions()
                                                .stream()
                                                .noneMatch(
                                                        exclusion -> exclusion.matchesResourceLocation(
                                                                resourceLimitLocation)))
                                        .mapToLong(rl -> rl.limit().quantity().number().value())
                                        .max()
                                        .orElse(0);
                                if (accept == 0) {
                                    iter.remove();
                                } else {
                                    iter.set(resourceLimit.withLimit(new Limit(
                                            new ResourceQuantity(new Number(Long.min(
                                                    accept,
                                                    resourceLimit
                                                            .limit()
                                                            .quantity()
                                                            .number()
                                                            .value()
                                            )), resourceLimit.limit().quantity()
                                                    .idExpansionBehaviour()),
                                            ResourceQuantity.MAX_QUANTITY
                                    )));
                                }
                            }
                        }
                        condensedResourceLimits = new ResourceLimits(
                                condensedResourceLimitList,
                                ResourceIdSet.EMPTY
                        );
                    }
                    if (condensedResourceLimits.resourceLimitList().isEmpty()) {
                        branchPayload.append("none\n");
                    } else {
                        branchPayload
                                .append(new OutputStatement(
                                        outputStatement.labelAccess(),
                                        condensedResourceLimits,
                                        outputStatement.each()
                                ).toStringPretty());
                    }

                }
                branchPayload.append("\n");
                payload.append(Tools.indent(branchPayload.toString(),4));
            }
        });

        successProgram.tick(new

                ProgramContext(
                successProgram,
                manager,
                new SimulateExploreAllPathsProgramBehaviour()
        ));

        return Tools.stripLeading(payload.toString());
    }

    private static <STACK, ITEM, CAP> ResourceLimit getSlotResource(
            LimitedInputSlot<STACK, ITEM, CAP> limitedInputSlot
    ) {
        ResourceType<STACK, ITEM, CAP> resourceType = limitedInputSlot.type;
        //noinspection OptionalGetWithoutIsPresent
        ResourceLocation key = SFMResourceTypes.registry().getKey(limitedInputSlot.type);

        STACK stack = limitedInputSlot.peekExtractPotential();
        long amount = limitedInputSlot.type.getAmount(stack);
        amount = Long.min(amount, limitedInputSlot.tracker.getResourceLimit().limit().quantity().number().value());
        long remainingObligation = limitedInputSlot.tracker.getRemainingRetentionObligation(resourceType, stack);
        amount -= Long.min(amount, remainingObligation);
        Limit amountLimit = new Limit(
                new ResourceQuantity(new Number(amount), ResourceQuantity.IdExpansionBehaviour.NO_EXPAND),
                ResourceQuantity.MAX_QUANTITY
        );
        ResourceLocation stackId = resourceType.getRegistryKeyForStack(stack);
        ResourceIdentifier<STACK, ITEM, CAP> resourceIdentifier = new ResourceIdentifier<>(
                key,
                stackId
        );
        return new ResourceLimit(
                new ResourceIdSet(Collections.singletonList(resourceIdentifier)),
                amountLimit,
                With.ALWAYS_TRUE
        );
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.programString=packetBuffer.readString(Program.MAX_PROGRAM_LENGTH);
        this.outputNodeIndex=packetBuffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(programString);
        packetBuffer.writeInt(outputNodeIndex);
    }

    @Override
    public IMessage onMessage(ServerboundOutputInspectionRequestPacket msg, MessageContext context) {
        SFMPacketHandlingContext context1 = new SFMPacketHandlingContext(context);
        context1.compileAndThen(
                msg.programString,
                (program, player, managerBlockEntity) -> program.astBuilder
                        .getNodeAtIndex(msg.outputNodeIndex)
                        .filter(OutputStatement.class::isInstance)
                        .map(OutputStatement.class::cast)
                        .ifPresent(outputStatement -> {
                            String payload = getOutputStatementInspectionResultsString(
                                    managerBlockEntity,
                                    program,
                                    outputStatement
                            );
                            payload = SFMMessage.truncate(
                                    payload,
                                    ServerboundOutputInspectionRequestPacket.MAX_RESULTS_LENGTH
                            );
                            SFM.LOGGER.debug(
                                    "Sending output inspection results packet with length {}",
                                    payload.length()
                            );
                            SFMPackets.sendToPlayer(
                                    player,
                                    new ClientboundOutputInspectionResultsPacket(payload)
                            );
                        })
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
