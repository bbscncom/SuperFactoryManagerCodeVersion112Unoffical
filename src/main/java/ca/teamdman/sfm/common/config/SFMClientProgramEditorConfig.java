package ca.teamdman.sfm.common.config;

import ca.teamdman.sfm.SFM;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SFM.MOD_ID)
public class SFMClientProgramEditorConfig {
    @Config.Comment("Whether to show line numbers in the program editor")
    @Config.Name("showLineNumbers")
    public static boolean showLineNumbers = true;

    @Config.Comment("The level of intellisense to use in the program editor")
    @Config.Name("intellisenseLevel")
    public static IntellisenseLevel intellisenseLevel = IntellisenseLevel.OFF;

    public enum IntellisenseLevel {
        OFF,
        BASIC,
        ADVANCED;

        public boolean isResourceIntellisenseEnabled() {
            return this == ADVANCED;
        }

        public boolean isDisabled() {
            return this == OFF;
        }
    }

    @Mod.EventBusSubscriber(modid = SFM.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(SFM.MOD_ID)) {
                ConfigManager.sync(SFM.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}