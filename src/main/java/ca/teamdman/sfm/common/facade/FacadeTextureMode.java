package ca.teamdman.sfm.common.facade;

import org.jetbrains.annotations.Nullable;

public enum FacadeTextureMode {
    STRETCH,
    FILL;

    @Nullable
    public static FacadeTextureMode byName(@Nullable String name) {
        if (name == null) return null;
        for (FacadeTextureMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
