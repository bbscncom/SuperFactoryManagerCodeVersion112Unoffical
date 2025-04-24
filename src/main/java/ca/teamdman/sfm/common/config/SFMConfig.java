package ca.teamdman.sfm.common.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class SFMConfig {
    // 配置文件实例
    public static SFMServerConfig SERVER_CONFIG;
    public static Configuration CLIENT_CONFIG;
    public static SFMClientProgramEditorConfig CLIENT_PROGRAM_EDITOR;

    // 初始化方法
    public static void preInit(FMLPreInitializationEvent event) {
        File configDir = event.getModConfigurationDirectory();
        // 初始化服务器配置
        SFMServerConfig.init(new File(configDir, "sfm-server"));

    }

    // 获取配置值的工具方法
    public static <T> T getOrDefault(Configuration config, String category, String name, T defaultValue) {
        try {
            if (defaultValue instanceof Boolean) {
                return (T) Boolean.valueOf(config.getBoolean(name, category, (Boolean) defaultValue, ""));
            } else if (defaultValue instanceof Integer) {
                return (T) Integer.valueOf(config.getInt(name, category, (Integer) defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, ""));
            } else if (defaultValue instanceof String) {
                return (T) config.getString(name, category, (String) defaultValue, "");
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T> T getOrFallback(Configuration config, String category, String name, T fallback) {
        try {
            if (fallback instanceof Boolean) {
                return (T) Boolean.valueOf(config.getBoolean(name, category, (Boolean) fallback, ""));
            } else if (fallback instanceof Integer) {
                return (T) Integer.valueOf(config.getInt(name, category, (Integer) fallback, Integer.MIN_VALUE, Integer.MAX_VALUE, ""));
            } else if (fallback instanceof String) {
                return (T) config.getString(name, category, (String) fallback, "");
            }
            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
