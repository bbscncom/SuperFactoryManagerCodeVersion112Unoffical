package ca.teamdman.sfm.common.capability;

import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LabelGunCapProvider implements ICapabilityProvider {
    private Map<Capability, Class> types = new HashMap<>();
    private Map<Class, Object> datas = new HashMap<>();
    public LabelGunCapProvider() {
        types.put(SFMDataComponents.ACTIVE_LABEL,ActiveLabel.class);
        types.put(SFMDataComponents.LABEL_GUN_VIEW_MODE,LabelGunViewMod.class);
        types.put(SFMDataComponents.LABEL_POSITION_HOLDER, LabelPositionHolder.class);

        types.forEach((capability, aClass) -> {
            try {
                datas.put(aClass,aClass.newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return types.containsKey(capability);
    }
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T) datas.get(types.get(capability));
    }
}
