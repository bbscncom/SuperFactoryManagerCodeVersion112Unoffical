package ca.teamdman.sfm.common.resourcetype;

import my.Tools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 将 1.12.2 的 FluidRegistry 封装成类似 IForgeRegistry<Fluid> 的接口
 */
public class FluidRegistryWrapper implements IForgeRegistry<FluidExtend> {
    public static final FluidRegistryWrapper INSTANCE = new FluidRegistryWrapper();

    // === 核心注册方法 ===
    @Override
    public void register(@Nonnull FluidExtend value) {
        FluidRegistry.registerFluid(value);
    }

    @Override
    public void registerAll(FluidExtend... values) {
        for (FluidExtend fluid : values) {
            FluidRegistry.registerFluid(fluid);
        }
    }

    // === 查询方法 ===
    @Override
    public boolean containsKey(@Nonnull ResourceLocation key) {
        return FluidRegistry.getFluid(key.getPath()) != null;
    }

    @Override
    public boolean containsValue(@Nonnull FluidExtend value) {
        return FluidRegistry.getFluidName(value) != null;
    }

    @Nullable
    @Override
    public FluidExtend getValue(@Nonnull ResourceLocation key) {
        return FluidExtend.wrapper(FluidRegistry.getFluid(key.getPath()));
    }

    @Nullable
    @Override
    public ResourceLocation getKey(@Nonnull FluidExtend value) {
        String name = FluidRegistry.getFluidName(value);
        return name != null ? new ResourceLocation(name) : null;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getKeys() {
        return FluidRegistry.getRegisteredFluids().keySet().stream()
                .map(ResourceLocation::new)
                .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public List<FluidExtend> getValues() {
        return new ArrayList<>(getFluids(FluidRegistry.getRegisteredFluids().values()));
    }

    private List<FluidExtend> getFluids(Collection<Fluid> values) {
        return values.stream().map(FluidExtend::wrapper).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public Set<Map.Entry<ResourceLocation, FluidExtend>> getEntries() {
        return FluidRegistry.getRegisteredFluids().entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(new ResourceLocation(e.getKey()), FluidExtend.wrapper(e.getValue())))
                .collect(Collectors.toSet());
    }

    // === 迭代器 ===
    @Nonnull
    @Override
    public Iterator<FluidExtend> iterator() {
        return FluidRegistry.getRegisteredFluids().values().stream().map(FluidExtend::wrapper).collect(Collectors.toList()).iterator();
    }

    // === 其他必需方法 ===
    @Nonnull
    @Override
    public Class<FluidExtend> getRegistrySuperType() {
        return FluidExtend.class;
    }

    @Nullable
    @Override
    public <T> T getSlaveMap(@Nonnull ResourceLocation slaveMapName, @Nonnull Class<T> type) {
        return null; // 1.12.2 不支持 SlaveMap
    }

}