package net.minecraft.client.gui.navigation;


import my.net.neoforged.api.distmarker.Dist;
import my.net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonInputs {
    public static boolean selected(int pKey) {
        return pKey == 257 || pKey == 32 || pKey == 335;
    }
}
