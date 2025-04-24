package ca.teamdman.sfm.client.registry;

import ca.teamdman.sfm.common.localization.LocalizationKeys;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

//@Mod.EventBusSubscriber(modid = SFM.MOD_ID, value = Side.CLIENT)
public class SFMKeyMappings {
    public static final KeyBinding MORE_INFO_TOOLTIP_KEY = new KeyBinding(
            LocalizationKeys.MORE_HOVER_INFO_KEY.key().get(),
            Keyboard.KEY_LSHIFT,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding CYCLE_LABEL_VIEW_KEY = new KeyBinding(
            LocalizationKeys.CYCLE_LABEL_VIEW_KEY.key().get(),
            Keyboard.KEY_LMENU,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding TOGGLE_NETWORK_TOOL_OVERLAY_KEY = new KeyBinding(
            LocalizationKeys.TOGGLE_NETWORK_TOOL_OVERLAY.key().get(),
            Keyboard.KEY_LMENU,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding CONTAINER_INSPECTOR_KEY = new KeyBinding(
            LocalizationKeys.CONTAINER_INSPECTOR_TOGGLE_KEY.key().get(),
            Keyboard.KEY_I,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding ITEM_INSPECTOR_KEY = new KeyBinding(
            LocalizationKeys.ITEM_INSPECTOR_TOGGLE_KEY.key().get(),
            Keyboard.KEY_NONE,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding LABEL_GUN_PICK_BLOCK_MODIFIER_KEY = new KeyBinding(
            LocalizationKeys.LABEL_GUN_PICK_BLOCK_MODIFIER_KEY.key().get(),
            Keyboard.KEY_LMENU,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding LABEL_GUN_NEXT_LABEL_KEY = new KeyBinding(
            LocalizationKeys.LABEL_GUN_NEXT_LABEL_KEY.key().get(),
            Keyboard.KEY_NONE,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

    public static final KeyBinding LABEL_GUN_PREVIOUS_LABEL_KEY = new KeyBinding(
            LocalizationKeys.LABEL_GUN_PREVIOUS_LABEL_KEY.key().get(),
            Keyboard.KEY_NONE,
            LocalizationKeys.SFM_KEY_CATEGORY.key().get()
    );

//    @SubscribeEvent
    public static void registerBindings() {
        ClientRegistry.registerKeyBinding(MORE_INFO_TOOLTIP_KEY);
        ClientRegistry.registerKeyBinding(CONTAINER_INSPECTOR_KEY);
        ClientRegistry.registerKeyBinding(ITEM_INSPECTOR_KEY);
        ClientRegistry.registerKeyBinding(CYCLE_LABEL_VIEW_KEY);
        ClientRegistry.registerKeyBinding(LABEL_GUN_PICK_BLOCK_MODIFIER_KEY);
        ClientRegistry.registerKeyBinding(LABEL_GUN_NEXT_LABEL_KEY);
        ClientRegistry.registerKeyBinding(LABEL_GUN_PREVIOUS_LABEL_KEY);
    }
}
