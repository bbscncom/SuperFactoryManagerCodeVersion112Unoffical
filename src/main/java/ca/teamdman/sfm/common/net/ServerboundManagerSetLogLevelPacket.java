package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

public class ServerboundManagerSetLogLevelPacket implements SFMMessage<ServerboundManagerSetLogLevelPacket,IMessage> {
    public static final int MAX_LOG_LEVEL_NAME_LENGTH = 64;
    private  int windowId;
    private  BlockPos pos;
    private  String logLevel;

    public ServerboundManagerSetLogLevelPacket() {
    }

    public ServerboundManagerSetLogLevelPacket(int windowId, BlockPos pos, String logLevel) {
        this.windowId = windowId;
        this.pos = pos;
        this.logLevel = logLevel;
    }

    public int windowId() {
        return windowId;
    }

    public BlockPos pos() {
        return pos;
    }

    public String logLevel() {
        return logLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        windowId = packetBuffer.readInt();
        pos = packetBuffer.readBlockPos();
        logLevel = packetBuffer.readString(MAX_LOG_LEVEL_NAME_LENGTH);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeInt(windowId);
        packetBuffer.writeBlockPos(pos);
        packetBuffer.writeString(logLevel);
    }
    @Override
    public IMessage onMessage(ServerboundManagerSetLogLevelPacket message, MessageContext ctx) {
        SFMPacketHandlingContext context = new SFMPacketHandlingContext(ctx);
        context.handleServerboundContainerPacket(
                ManagerContainerMenu.class,
                ManagerBlockEntity.class,
                message.pos,
                message.windowId,
                (menu, manager) -> {
                    // get the level
                    Level logLevelObj = Level.getLevel(message.logLevel());

                    // set the level
                    manager.setLogLevel(logLevelObj);

                    // log in manager
                    manager.logger.info(x -> x.accept(LocalizationKeys.LOG_LEVEL_UPDATED.get(
                            message.logLevel())));

                    // log in server console
                    String sender = "UNKNOWN SENDER";
                    EntityPlayerMP player = context.sender();
                    if (player != null) {
                        sender = player.getName();
                    }
                    SFM.LOGGER.debug(
                            "{} updated manager {} {} log level to {}",
                            sender,
                            message.pos(),
                            manager.getWorld(),
                            message.logLevel()
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
