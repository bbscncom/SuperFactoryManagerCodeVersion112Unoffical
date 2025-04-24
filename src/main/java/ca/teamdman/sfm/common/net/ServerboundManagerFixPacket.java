package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.linting.ProgramLinter;
import ca.teamdman.sfml.ast.Program;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;

public class ServerboundManagerFixPacket implements SFMMessage<ServerboundManagerFixPacket, IMessage> {
    private int windowId;
    private BlockPos pos;

    public ServerboundManagerFixPacket() {
    }

    public ServerboundManagerFixPacket(int windowId, BlockPos pos) {
        this.windowId = windowId;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        this.pos = packetBuffer.readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(this.windowId);
        packetBuffer.writeBlockPos(this.pos);
    }

    @Override
    public IMessage onMessage(ServerboundManagerFixPacket msg, MessageContext ctx) {
        new SFMPacketHandlingContext(ctx).handleServerboundContainerPacket(
                ManagerContainerMenu.class,
                ManagerBlockEntity.class,
                msg.pos,
                msg.windowId,
                (menu, manager) -> {
                    @Nullable ItemStack disk = manager.getDisk();
                    if (disk != null) {
                        @Nullable Program program = manager.getProgram();
                        if (program != null) {
                            ProgramLinter.fixWarnings(
                                    manager,
                                    disk,
                                    program
                            );
                        }
                    }
                }
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
