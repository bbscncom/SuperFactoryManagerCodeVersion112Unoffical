package ca.teamdman.sfm.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GuiQueue {
    private static GuiScreen nextScreen = null;
    private static final ConcurrentMap<GuiScreen, CompletableFuture<Void>> pendingFutures = new ConcurrentHashMap<>();

    public static  CompletableFuture<Void> showLater(GuiScreen screen) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        pendingFutures.put(screen, future);
        nextScreen = screen;
        return future;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if(nextScreen!=null&&Minecraft.getMinecraft().currentScreen!=null){
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        if (nextScreen != null && Minecraft.getMinecraft().currentScreen == null) {
            GuiScreen screenToShow = nextScreen;
            Minecraft.getMinecraft().displayGuiScreen(nextScreen);
            nextScreen = null;

            // 当屏幕被关闭时完成future
            CompletableFuture<Void> future = pendingFutures.remove(screenToShow);
            if (future != null) {
                future.complete(null);
            }
        }
    }
}