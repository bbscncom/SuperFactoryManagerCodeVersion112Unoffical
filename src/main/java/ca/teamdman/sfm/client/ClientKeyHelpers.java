package ca.teamdman.sfm.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ClientKeyHelpers {
    public static boolean isKeyDownInScreenOrWorld(KeyBinding key) {
        if (key.getKeyCode() == Keyboard.KEY_NONE) {
            return false;
        }
        // special effort is needed to ensure this works properly when the manager screen is open
        return Keyboard.isKeyDown(key.getKeyCode());
    }

    public static boolean isKeyDownInWorld(KeyBinding key) {
        if (key.getKeyCode() == Keyboard.KEY_NONE) {
            return false;
        }
        return key.isPressed();
    }
}
