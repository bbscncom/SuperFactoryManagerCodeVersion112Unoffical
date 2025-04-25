package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.facade.FacadePlanner;
import ca.teamdman.sfm.common.facade.FacadeSpreadLogic;
import ca.teamdman.sfm.common.facade.IFacadePlan;
import ca.teamdman.sfm.common.util.RayTraceResultIO;
import ca.teamdman.sfm.common.util.SFMPlayerUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.Objects;

public final class ServerboundFacadePacket implements SFMMessage<ServerboundFacadePacket, IMessage> {
    public  RayTraceResult hitResult;
    public  FacadeSpreadLogic spreadLogic;
    public  ItemStack paintStack;
    public  EnumHand paintHand;

    public ServerboundFacadePacket() {
    }

    public ServerboundFacadePacket(RayTraceResult hitResult, FacadeSpreadLogic spreadLogic, ItemStack paintStack, EnumHand paintHand) {
        this.hitResult = hitResult;
        this.spreadLogic = spreadLogic;
        this.paintStack = paintStack;
        this.paintHand = paintHand;
    }

    public RayTraceResult hitResult() {
        return hitResult;
    }

    public FacadeSpreadLogic spreadLogic() {
        return spreadLogic;
    }

    public ItemStack paintStack() {
        return paintStack;
    }

    public EnumHand paintHand() {
        return paintHand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerboundFacadePacket that = (ServerboundFacadePacket) o;
        return Objects.equals(hitResult, that.hitResult) &&
                spreadLogic == that.spreadLogic &&
                Objects.equals(paintStack, that.paintStack) &&
                paintHand == that.paintHand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hitResult, spreadLogic, paintStack, paintHand);
    }

    @Override
    public String toString() {
        return "ServerboundFacadePacket[" +
                "hitResult=" + hitResult + ", " +
                "spreadLogic=" + spreadLogic + ", " +
                "paintStack=" + paintStack + ", " +
                "paintHand=" + paintHand + ']';
    }

    public static void handle(
            ServerboundFacadePacket msg,
            EntityPlayerMP sender
    ) {
        World level = SFMPlayerUtils.getWorld(sender);
        IFacadePlan facadePlan = FacadePlanner.getFacadePlan(sender, level, msg);
        if (facadePlan == null) {
            return;
        }
        facadePlan.apply(level);
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.hitResult=RayTraceResultIO.readFixed(buf);
        this.spreadLogic=packetBuffer.readEnumValue(FacadeSpreadLogic.class);
        try {
            this.paintStack=packetBuffer.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.paintHand=packetBuffer.readEnumValue(EnumHand.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        RayTraceResultIO.writeFixed(packetBuffer,hitResult);
        packetBuffer.writeEnumValue(spreadLogic);
        packetBuffer.writeItemStack(paintStack);
        packetBuffer.writeEnumValue(paintHand);
    }

    @Override
    public IMessage onMessage(ServerboundFacadePacket message, MessageContext ctx) {
        EntityPlayerMP sender = ctx.getServerHandler().player;
        if (sender == null) return null;
        ServerboundFacadePacket.handle(message, sender);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
