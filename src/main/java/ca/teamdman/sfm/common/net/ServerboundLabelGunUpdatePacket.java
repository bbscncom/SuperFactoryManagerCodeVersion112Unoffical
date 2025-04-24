package ca.teamdman.sfm.common.net;


import ca.teamdman.sfm.common.item.LabelGunItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundLabelGunUpdatePacket implements SFMMessage<ServerboundLabelGunUpdatePacket,IMessage> {
    public static final int MAX_LABEL_LENGTH = 80;
    private String label;
    private EnumHand hand;

    public ServerboundLabelGunUpdatePacket() {
    }
    public ServerboundLabelGunUpdatePacket(String label, EnumHand hand) {
        this.label = label;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        label = packetBuffer.readString(MAX_LABEL_LENGTH);
        hand = packetBuffer.readEnumValue(EnumHand.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(label);
        packetBuffer.writeEnumValue(hand);
    }
    @Override
    public IMessage onMessage(ServerboundLabelGunUpdatePacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            return null;
        }
        ItemStack heldItem = ctx.getServerHandler().player.getHeldItem(message.hand);
        if (heldItem.getItem() instanceof LabelGunItem) {
            LabelGunItem.setActiveLabel(heldItem, message.label);
        }
        return null;
    }


    @Override
    public Side getSide() {
        return Side.SERVER;
    }

}

