package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import ca.teamdman.sfml.ast.Program;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundDiskItemSetProgramPacket implements SFMMessage<ServerboundDiskItemSetProgramPacket,IMessage> {
    private String programString;
    private EnumHand hand;

    public ServerboundDiskItemSetProgramPacket() {
    }

    public ServerboundDiskItemSetProgramPacket(String programString, EnumHand hand) {
        this.programString = programString;
        this.hand = hand;
    }

    public String getProgramString() {
        return programString;
    }

    public EnumHand getHand() {
        return hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);

        this.programString=packetBuffer.readString(Program.MAX_PROGRAM_LENGTH);
        this.hand=packetBuffer.readEnumValue(EnumHand.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.getProgramString());
        packetBuffer.writeEnumValue(this.getHand());
    }

    @Override
    public IMessage onMessage(ServerboundDiskItemSetProgramPacket message, MessageContext ctx) {
        EntityPlayerMP sender = ctx.getServerHandler().player;
        if (sender == null) {
            return null;
        }
        ItemStack stack = sender.getHeldItem(message.getHand());
        if (stack.getItem() instanceof DiskItem) {
            DiskItem.setProgram(stack, message.getProgramString());
            DiskItem.compileAndUpdateErrorsAndWarnings(stack, null);
            DiskItem.pruneIfDefault(stack);


        }
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

}
