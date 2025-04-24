package ca.teamdman.sfm.client.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.registry.SFMMenus;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class SFMMenuScreens {
    public static void register() {
//        event.register(SFMMenus.MANAGER_MENU.get(), ManagerScreen::new);
        NetworkRegistry.INSTANCE.registerGuiHandler(SFM.instance, SFMMenus.handler);
    }
}
