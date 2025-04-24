package ca.teamdman.sfm.common.config;

import ca.teamdman.sfm.SFM;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class SFMConfigTracker {
    private static final HashMap<String, Configuration> configFiles = new HashMap<>();
    
    public static @Nullable Configuration getConfig(String name) {
        return configFiles.get(name);
    }
    
    public static void loadConfig(String name, File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();
        configFiles.put(name, config);
    }

    public static void saveConfig(String name) {
        Configuration config = configFiles.get(name);
        if(config != null && config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventBusSubscriber(modid = SFM.MOD_ID)
    public static class ConfigEventHandler {
        
        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            // 当玩家登出时保存服务器配置
            saveConfig("server");
        }
    }
}
