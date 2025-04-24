package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMASTUtils;
import ca.teamdman.sfml.ast.InputStatement;
import io.netty.buffer.ByteBuf;
import my.Tools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundInputInspectionRequestPacket implements SFMMessage<ServerboundInputInspectionRequestPacket, IMessage> {
    public static final int MAX_PROGRAM_LENGTH = 20480;
    private String programString;
    private int inputNodeIndex;

    public ServerboundInputInspectionRequestPacket() {
    }

    public ServerboundInputInspectionRequestPacket(String programString, int inputNodeIndex) {
        this.programString = programString;
        this.inputNodeIndex = inputNodeIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.programString = packetBuffer.readString(MAX_PROGRAM_LENGTH);
        this.inputNodeIndex = packetBuffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.programString);
        packetBuffer.writeInt(this.inputNodeIndex);
    }

    @Override
    public IMessage onMessage(ServerboundInputInspectionRequestPacket msg, MessageContext context) {
        SFMPacketHandlingContext sfmPacketHandlingContext = new SFMPacketHandlingContext(context);
        sfmPacketHandlingContext.compileAndThen(
                msg.programString,
                (program, player, managerBlockEntity) ->
                        program.astBuilder
                                .getNodeAtIndex(msg.inputNodeIndex)
                                .filter(InputStatement.class::isInstance)
                                .map(InputStatement.class::cast)
                                .ifPresent(inputStatement -> {
                                    StringBuilder payload = new StringBuilder();
                                    payload
                                            .append(inputStatement.toStringPretty())
                                            .append("\n-- peek results --\n");

                                    ProgramContext programContext = new ProgramContext(
                                            program,
                                            managerBlockEntity,
                                            new SimulateExploreAllPathsProgramBehaviour()
                                    );
                                    int preLen = payload.length();
                                    inputStatement.gatherSlots(
                                            programContext,
                                            slot -> SFMASTUtils
                                                    .getInputStatementForSlot(
                                                            slot,
                                                            inputStatement.labelAccess()
                                                    )
                                                    .ifPresent(is -> payload
                                                            .append(is.toStringPretty())
                                                            .append("\n"))
                                    );
                                    if (payload.length() == preLen) {
                                        payload.append("none");
                                    }

                                    SFMPackets.sendToPlayer(
                                            player,
                                            new ClientboundInputInspectionResultsPacket(
                                                    Tools.truncate(
                                                            payload.toString(),
                                                            ClientboundInputInspectionResultsPacket.MAX_RESULTS_LENGTH
                                                    ))
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
