package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfml.ast.IfStatement;
import io.netty.buffer.ByteBuf;
import my.Tools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundIfStatementInspectionRequestPacket implements SFMMessage<ServerboundIfStatementInspectionRequestPacket, IMessage> {
    public static final int MAX_PROGRAM_LENGTH = 20480;
    private String programString;
    private int inputNodeIndex;

    public ServerboundIfStatementInspectionRequestPacket() {
    }

    public ServerboundIfStatementInspectionRequestPacket(String programString, int inputNodeIndex) {
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
    public IMessage onMessage(ServerboundIfStatementInspectionRequestPacket msg, MessageContext context) {
        SFMPacketHandlingContext sfmPacketHandlingContext = new SFMPacketHandlingContext(context);
        sfmPacketHandlingContext.compileAndThen(
                msg.programString,
                (program, player, managerBlockEntity) -> program.astBuilder
                        .getNodeAtIndex(msg.inputNodeIndex)
                        .filter(IfStatement.class::isInstance)
                        .map(IfStatement.class::cast)
                        .ifPresent(ifStatement -> {
                            StringBuilder payload = new StringBuilder();
                            payload
                                    .append(ifStatement.toStringCondensed())
                                    .append("\n-- peek results --\n");
                            ProgramContext programContext = new ProgramContext(
                                    program,
                                    managerBlockEntity,
                                    new SimulateExploreAllPathsProgramBehaviour()
                            );
                            boolean result = ifStatement.condition.test(programContext);
                            payload.append(result ? "TRUE" : "FALSE");

                            SFMPackets.sendToPlayer(player, new ClientboundIfStatementInspectionResultsPacket(
                                    Tools.truncate(
                                            payload.toString(),
                                            ClientboundIfStatementInspectionResultsPacket.MAX_RESULTS_LENGTH
                                    )));
                        })
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
