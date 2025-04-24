package my;

import my.net.neoforged.api.distmarker.Dist;
import my.net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum Whence {
    ABSOLUTE,
    RELATIVE,
    END;
}