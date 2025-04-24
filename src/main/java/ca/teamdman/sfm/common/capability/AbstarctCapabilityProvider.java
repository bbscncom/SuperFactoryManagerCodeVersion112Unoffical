package ca.teamdman.sfm.common.capability;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import com.google.common.reflect.Reflection;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstarctCapabilityProvider  implements ICapabilitySerializable<NBTBase> {
    Map<Capability, Class> types = new HashMap<>();
    Map<Class, Object> datas = new HashMap<>();


    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return types.containsKey(capability);
    }
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T) datas.get(types.get(capability));
    }
    @Override
    public NBTBase serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        for (Map.Entry<Capability, Class> entry : types.entrySet()) {
            Capability cap = entry.getKey();
            Object data = datas.get(entry.getValue());
            if (data != null) {
                NBTBase capNBT = cap.writeNBT(data, null);
                if (capNBT != null) {
                    // 使用 Capability 的 registry name 作为子 key
                    compound.setTag(cap.getName(), capNBT);
                }
            }
        }
//        System.out.println(compound);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) return;
        NBTTagCompound compound = (NBTTagCompound) nbt;
        for (Map.Entry<Capability, Class> entry : types.entrySet()) {
            Capability cap = entry.getKey();
            Object data = datas.get(entry.getValue());
            if (data != null && compound.hasKey(cap.getName())) {
                cap.readNBT(data, null, compound.getTag(cap.getName()));
            }
        }
    }

    public static <T extends CapData> ItemStack updateSingleCapabilityToNBT(
            ItemStack stack,
            Capability<T> capability
    ) {
        if (stack.isEmpty() || capability == null) {
            return stack;
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound capsTag = stack.getTagCompound().getCompoundTag("ForgeCaps");
        T capValue = stack.getCapability(capability, null);
        if(capValue.get()==null)return stack;
        NBTBase capNBT = capability.writeNBT(capValue, null);
        if (capNBT != null) {
            capsTag.setTag(capability.getName(), capNBT);
            stack.getTagCompound().setTag("ForgeCaps", capsTag);
        }

        return stack;
    }
    public static <T> T readSingleCapabilityFromNBT(
            ItemStack stack,
            Capability<T> capability
    ) {
        if (stack.isEmpty() || capability == null || !stack.hasTagCompound()) {
            return null;
        }

        NBTTagCompound capsTag = stack.getTagCompound().getCompoundTag("ForgeCaps");
        if (capsTag.hasKey(capability.getName())) {
            T capInstance = stack.getCapability(capability, null);
            if (capInstance != null) {
                capability.readNBT(capInstance, null, capsTag.getTag(capability.getName()));
                return capInstance;
            }
        }
        return null;
    }
    public static ItemStack clearAllCapabilities(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return stack;
        }

        NBTTagCompound nbt = stack.getTagCompound();

        // 移除 ForgeCaps 标签
        nbt.removeTag("ForgeCaps");

        // 如果 NBT 为空，则移除整个 TagCompound
        if (nbt.isEmpty()) {
            stack.setTagCompound(null);
        }

        return stack;
    }

    /**
     * 清除 ItemStack 的指定 Capability 数据
     */
    public static ItemStack clearCapability(
            ItemStack stack,
            Capability<?> capability
    ) {
        if (stack.isEmpty() || capability == null || !stack.hasTagCompound()) {
            return stack;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagCompound capsTag = nbt.getCompoundTag("ForgeCaps");

        // 移除指定的 Capability 数据
        capsTag.removeTag(capability.getName());

        // 如果 ForgeCaps 为空，则移除整个标签
        if (capsTag.isEmpty()) {
            nbt.removeTag("ForgeCaps");
        }

        // 如果 NBT 为空，则移除整个 TagCompound
        if (nbt.isEmpty()) {
            stack.setTagCompound(null);
        }

        return stack;
    }

    public static <T extends CapData<R>, R> void updateCapAndNBT(ItemStack stack, Capability<T> activeLabel, R data) {
        stack.getCapability(activeLabel, null).setNullable(data);
        AbstarctCapabilityProvider.updateSingleCapabilityToNBT(stack, activeLabel);
    }

    public static <T extends CapData<R>, R> T  getCapFromNBT(ItemStack stack, Capability<T> activeLabel) {
        AbstarctCapabilityProvider.readSingleCapabilityFromNBT(stack, activeLabel);
        return stack.getCapability(activeLabel, null);

    }
}
