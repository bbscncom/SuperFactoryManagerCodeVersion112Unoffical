package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.item.LabelGunItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundLabelGunCycleViewModePacket implements SFMMessage<ServerboundLabelGunCycleViewModePacket, IMessage> {
    private EnumHand hand;

    public ServerboundLabelGunCycleViewModePacket() {
    }

    public ServerboundLabelGunCycleViewModePacket(EnumHand hand) {
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
    public IMessage onMessage(ServerboundLabelGunCycleViewModePacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            return null;
        }
        ItemStack heldItem = ctx.getServerHandler().player.getHeldItem(message.hand);
        if (heldItem.getItem() instanceof LabelGunItem) {
            LabelGunItem.cycleViewMode(heldItem);
        }
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}

