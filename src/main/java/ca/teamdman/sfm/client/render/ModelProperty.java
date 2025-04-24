package ca.teamdman.sfm.client.render;

import ca.teamdman.sfm.common.facade.FacadeData;
import com.google.common.base.Optional;
import net.minecraft.block.properties.PropertyHelper;

import java.util.Collection;

public class  ModelProperty<T extends Comparable<T>> extends PropertyHelper<T> {
    public static ModelProperty modelProperty = new ModelProperty(null, null);
    private FacadeData facadeData=null;
    public ModelProperty(String name, Class<T> valueClass) {
        super(name, valueClass);
    }

    @Override
    public Collection getAllowedValues() {
        return null;
    }

    @Override
    public Optional parseValue(String value) {
        return Optional.fromNullable(facadeData);
    }

    @Override
    public String getName(T value) {
        return getName();
    }
}
