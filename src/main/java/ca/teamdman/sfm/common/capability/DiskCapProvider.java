package ca.teamdman.sfm.common.capability;

import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DiskCapProvider extends AbstarctCapabilityProvider {

    public DiskCapProvider() {
        this.types.put(SFMDataComponents.PROGRAM_DATA,ProgramData.class);
        types.put(SFMDataComponents.LABEL_POSITION_HOLDER, LabelPositionHolder.class);
        types.put(SFMDataComponents.WARNINGS, Warnings.class);
        types.put(SFMDataComponents.ERRORS, Errors.class);

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
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T) datas.get(types.get(capability));
    }
}
