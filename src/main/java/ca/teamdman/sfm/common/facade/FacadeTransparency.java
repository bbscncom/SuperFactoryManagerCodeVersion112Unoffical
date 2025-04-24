package ca.teamdman.sfm.common.facade;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public enum FacadeTransparency implements IStringSerializable {
    OPAQUE, TRANSLUCENT;
    public static final PropertyEnum<FacadeTransparency> FACADE_TRANSPARENCY_PROPERTY = PropertyEnum.create("facade_transparency", FacadeTransparency.class);

    FacadeTransparency() {
    }

    @Override
    public @NotNull String getName() {
        return this == OPAQUE ? "opaque" : "translucent";
    }
}