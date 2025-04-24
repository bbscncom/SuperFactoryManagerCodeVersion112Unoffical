package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfml.ast.Program;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundManagerGuiUpdatePacket implements SFMMessage<ClientboundManagerGuiUpdatePacket, IMessage> {
    private int windowId;
    private String program;
    private ManagerBlockEntity.State state;
    private long[] tickTimes;

    // 默认构造函数用于反序列化
    public ClientboundManagerGuiUpdatePacket() {
    }

    public ClientboundManagerGuiUpdatePacket(int windowId, String program, ManagerBlockEntity.State state, long[] tickTimes) {
        this.windowId = windowId;
        this.program = program;
        this.state = state;
        this.tickTimes = tickTimes;
    }

    public ClientboundManagerGuiUpdatePacket cloneWithWindowId(int windowId) {
        return new ClientboundManagerGuiUpdatePacket(windowId, program, state, tickTimes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        this.program = packetBuffer.readString(Program.MAX_PROGRAM_LENGTH);
        this.state = packetBuffer.readEnumValue(ManagerBlockEntity.State.class);
        this.tickTimes = new long[packetBuffer.readVarInt()];
        for (int i = 0; i < tickTimes.length; i++) {
            this.tickTimes[i] = packetBuffer.readLong();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(this.windowId);
        packetBuffer.writeString(this.program);
        packetBuffer.writeEnumValue(this.state);
        packetBuffer.writeVarInt(this.tickTimes.length);
        for (long time : this.tickTimes) {
            packetBuffer.writeLong(time);
        }
    }

    @Override
    public IMessage onMessage(ClientboundManagerGuiUpdatePacket message, MessageContext ctx) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return null;
        }

        if (!(player.openContainer instanceof ManagerContainerMenu)) {
            return null;
        }

        ManagerContainerMenu menu = (ManagerContainerMenu) player.openContainer;
        if (menu.windowId != message.windowId) {
            return null;
        }

        menu.tickTimeNanos = message.tickTimes;
        menu.state = message.state;
        menu.program = message.program;
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
