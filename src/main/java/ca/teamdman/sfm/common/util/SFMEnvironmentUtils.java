package ca.teamdman.sfm.common.util;

import ca.teamdman.sfm.SFM;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = SFM.MOD_ID)
public class SFMEnvironmentUtils {
    private static boolean gameLoaded = false;

//    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        gameLoaded = true;
    }

    public static boolean isGameLoaded() {
        return gameLoaded;
    }

    public static boolean isInIDE() {
        // In 1.12, you might need to use a different approach to determine if running in IDE
        return System.getProperty("java.class.path").contains("idea_rt.jar");
    }

    public static boolean isClient() {
        return FMLCommonHandler.instance().getSide() == Side.CLIENT;
    }
}
