package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.item.LabelGunItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundLabelGunClearPacket implements SFMMessage<ServerboundLabelGunClearPacket, IMessage> {
    private EnumHand hand;

    public ServerboundLabelGunClearPacket() {
    }

    public ServerboundLabelGunClearPacket(EnumHand hand) {
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        hand = packetBuffer.readEnumValue(EnumHand.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeEnumValue(hand);
    }

    @Override
    public IMessage onMessage(ServerboundLabelGunClearPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            return null;
        }
        ItemStack heldItem = ctx.getServerHandler().player.getHeldItem(message.hand);
        if (heldItem.getItem() instanceof LabelGunItem) {
            LabelGunItem.clearAll(heldItem);
        }
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
