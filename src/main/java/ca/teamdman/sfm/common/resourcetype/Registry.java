package ca.teamdman.sfm.common.resourcetype;

import net.minecraft.util.ResourceLocation;

import java.util.Set;
import java.util.stream.Stream;

public interface Registry<T> {
    /**
     * 获取注册表中所有键的集合
     */
    Set<ResourceLocation> keySet();

    /**
     * 根据键获取对应的值
     */
    T get(ResourceLocation location);

    /**
     * 根据值获取对应的键
     */
    ResourceLocation getKey(T item);
    Stream<T> stream();

    boolean containsKey(ResourceLocation location);
}