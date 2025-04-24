package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundManagerRebuildPacket implements SFMMessage<ServerboundManagerRebuildPacket, IMessage> {
    private int windowId;
    private BlockPos pos;

    public ServerboundManagerRebuildPacket() {
    }

    public ServerboundManagerRebuildPacket(int windowId, BlockPos pos) {
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
    public IMessage onMessage(ServerboundManagerRebuildPacket msg, MessageContext ctx) {
        new SFMPacketHandlingContext(ctx).handleServerboundContainerPacket(
                ManagerContainerMenu.class,
                ManagerBlockEntity.class,
                msg.pos,
                msg.windowId,
                (menu, manager) -> {
                    CableNetworkManager.purgeCableNetworkForManager(manager);
                    manager.logger.warn(x -> x.accept(LocalizationKeys.LOG_MANAGER_CABLE_NETWORK_REBUILD.get()));
                    SFM.LOGGER.debug(
                            "{} performed rebuild for manager {} {}",
                            ctx.getServerHandler().player.getName(),
                            msg.pos,
                            manager.getWorld()
                    );
                }
        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
