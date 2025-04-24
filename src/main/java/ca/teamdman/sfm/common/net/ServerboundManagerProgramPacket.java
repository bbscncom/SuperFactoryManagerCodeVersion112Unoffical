package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfml.ast.Program;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundManagerProgramPacket implements SFMMessage<ServerboundManagerProgramPacket, IMessage> {
    private int windowId;
    private BlockPos pos;
    private String program;

    public ServerboundManagerProgramPacket() {
    }

    public ServerboundManagerProgramPacket(int windowId, BlockPos pos, String program) {
        this.windowId = windowId;
        this.pos = pos;
        this.program = program;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        this.pos = packetBuffer.readBlockPos();
        this.program = packetBuffer.readString(Program.MAX_PROGRAM_LENGTH);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(this.windowId);
        packetBuffer.writeBlockPos(this.pos);
        packetBuffer.writeString(this.program);
    }

    @Override
    public IMessage onMessage(ServerboundManagerProgramPacket msg, MessageContext ctx) {
        new SFMPacketHandlingContext(ctx).handleServerboundContainerPacket(
                ManagerContainerMenu.class,
                ManagerBlockEntity.class,
                msg.pos,
                msg.windowId,
                (menu, manager) -> manager.setProgram(msg.program)
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
