package ca.teamdman.sfm.common.resourcetype;

import com.google.common.collect.HashBiMap;
import my.Tools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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

    public static final HashBiMap<String, Fluid> fluids = HashBiMap.create();
    public static final HashBiMap<ResourceLocation, FluidExtend> fluidsWrapper = HashBiMap.create();


    public void wrapper(String key, Fluid fluid) {
        fluids.put(key, fluid);
        fluidsWrapper.put(new ResourceLocation(key), FluidExtend.wrapper(fluid));
    }

    public void wrapper(Map<String, Fluid> map) {
        fluids.putAll(map);
        map.forEach((s, fluid) -> fluidsWrapper.put(new ResourceLocation(s), FluidExtend.wrapper(fluid)));
    }

    // === 核心注册方法 ===
    @Override
    public void register(@Nonnull FluidExtend value) {
        throw new UnsupportedOperationException("wrapper only for read");
    }

    @Override
    public void registerAll(FluidExtend... values) {
        throw new UnsupportedOperationException("wrapper only for read");
    }

    // === 查询方法 ===
    @Override
    public boolean containsKey(@Nonnull ResourceLocation key) {
        return fluidsWrapper.get(key) != null;
    }

    @Override
    public boolean containsValue(@Nonnull FluidExtend value) {

        return fluidsWrapper.inverse().get(value) != null;
    }

    @Nullable
    @Override
    public FluidExtend getValue(@Nonnull ResourceLocation key) {
        return fluidsWrapper.get(key);
    }

    @Nullable
    @Override
    public ResourceLocation getKey(@Nonnull FluidExtend value) {
        if(fluids.get(value.getName())!=null){
            return new ResourceLocation(value.getName());
        }
        return null;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getKeys() {
        return fluidsWrapper.keySet();
//        return FluidRegistry.getRegisteredFluids().keySet().stream()
//                .map(ResourceLocation::new)
//                .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public List<FluidExtend> getValues() {
        return new ArrayList<>(fluidsWrapper.values());
    }

    private Fluid getFluids(ResourceLocation resourceLocation) {
        return fluids.get(resourceLocation.getPath());
    }

    @Nonnull
    @Override
    public Set<Map.Entry<ResourceLocation, FluidExtend>> getEntries() {
        return fluidsWrapper.entrySet();
    }

    // === 迭代器 ===
    @Nonnull
    @Override
    public Iterator<FluidExtend> iterator() {
        return fluidsWrapper.values().iterator();
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
        throw new UnsupportedOperationException("1.20 to 1.12,should not used");
    }

}