package ca.teamdman.sfm;

import ca.teamdman.sfm.client.registry.CommonProxy;
import ca.teamdman.sfm.client.registry.SFMBlockColors;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.client.registry.SFMMenuScreens;
import ca.teamdman.sfm.common.config.SFMConfig;
import ca.teamdman.sfm.common.registry.*;
import my.net.neoforged.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = SFM.MOD_ID, name = SFM.NAME, version = SFM.VERSION)
public class SFM {
    public static final String MOD_ID = "sfm";
    public static final Logger LOGGER = LogManager.getLogger(SFM.MOD_ID);
    public static final String ISSUE_TRACKER_URL = "https://github.com/TeamDman/SuperFactoryManager/issues";
    public static final String NAME = "Super Factory Manager 1.12code";
    public static final String VERSION = "1.0";
    @Mod.Instance
    public static SFM instance;

    public SFM() {
        MinecraftForge.EVENT_BUS.register(this);

        // SFMBlocks.register(bus);
//        SFMItems.register(bus);
//        SFMDataComponents.register(bus);
//        SFMCreativeTabs.register(bus);
//        SFMResourceTypes.register(bus);
//        SFMProgramLinters.register(bus);
//        SFMBlockEntities.register(bus);
//        SFMMenus.register(bus);
//        SFMRecipeTypes.register(bus);
//        SFMRecipeSerializers.register(bus);
//        SFMConfig.register(ModLoadingContext.get());
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SFMConfig.preInit(event);
        SFMBlocks.register();
        SFMDataComponents.init();
        SFMItems.init();
        SFMPackets.register();

        SFMMenuScreens.register();
        SFMKeyMappings.registerBindings();
        LOGGER.info("SFM preInit 完成");
    }

    @SidedProxy(clientSide = "ca.teamdman.sfm.client.registry.SFMBlockColors", serverSide = "ca.teamdman.sfm.client.registry.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

}
