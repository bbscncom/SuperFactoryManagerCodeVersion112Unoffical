package ca.teamdman.sfm.client.handler;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.item.LabelGunItem;
import ca.teamdman.sfm.common.net.ServerboundLabelGunUpdatePacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = SFM.MOD_ID, value = Side.CLIENT)
public class LabelGunScrollSwitcher {
    @SubscribeEvent
    public static void onScroll(MouseEvent event) {
        // 获取滚轮滚动量（>0 向上滚，<0 向下滚）
        int scroll = event.getDwheel();
        if (scroll == 0)return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;
        if (!player.isSneaking()) return;

        ItemStack gun = player.getHeldItemMainhand();
        EnumHand hand = EnumHand.MAIN_HAND;
        if (!(gun.getItem() instanceof LabelGunItem)) {
            gun = player.getHeldItemOffhand();
            hand = EnumHand.OFF_HAND;
        }
        if (!(gun.getItem() instanceof LabelGunItem)) return;

        String next = LabelGunItem.getNextLabel(gun, Mouse.getEventDWheel() < 0 ? -1 : 1);
        SFMPackets.sendToServer(new ServerboundLabelGunUpdatePacket(
                next,
                hand
        ));

        event.setCanceled(true);
    }
}
