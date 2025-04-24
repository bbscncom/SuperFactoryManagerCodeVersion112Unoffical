package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundManagerLogDesireUpdatePacket implements  SFMMessage<ServerboundManagerLogDesireUpdatePacket,IMessage> {
    private int windowId;
    private BlockPos pos;
    private boolean isLogScreenOpen;

    // 默认构造函数用于反序列化
    public ServerboundManagerLogDesireUpdatePacket() {
    }

    public ServerboundManagerLogDesireUpdatePacket(int windowId, BlockPos pos, boolean isLogScreenOpen) {
        this.windowId = windowId;
        this.pos = pos;
        this.isLogScreenOpen = isLogScreenOpen;
    }

    public int getWindowId() {
        return windowId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isLogScreenOpen() {
        return isLogScreenOpen;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        this.pos = BlockPos.fromLong(packetBuffer.readLong());
        this.isLogScreenOpen = packetBuffer.readBoolean();
    }


    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(this.windowId);
        packetBuffer.writeLong(this.pos.toLong());
        packetBuffer.writeBoolean(this.isLogScreenOpen);
    }

    @Override
    public IMessage onMessage(ServerboundManagerLogDesireUpdatePacket message, MessageContext ctx) {
        SFMPacketHandlingContext ctx1 = new SFMPacketHandlingContext(ctx);
        ctx1.handleServerboundContainerPacket(
                ManagerContainerMenu.class,
                ManagerBlockEntity.class,
                message.pos,
                message.windowId,
                (menu, manager) -> {
                    menu.isLogScreenOpen = message.isLogScreenOpen();
                    manager.sendUpdatePacket();
                }
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
