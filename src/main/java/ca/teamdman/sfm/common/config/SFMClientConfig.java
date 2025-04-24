package ca.teamdman.sfm.common.config;

import ca.teamdman.sfm.SFM;
import net.minecraftforge.common.config.Config;

@Config(modid = SFM.MOD_ID, category = "client")
public class    SFMClientConfig {
    @Config.Comment("Show label gun reminder overlay")
    public static boolean showLabelGunReminderOverlay = true;

    @Config.Comment("Show network tool reminder overlay") 
    public static boolean showNetworkToolReminderOverlay = true;
}
