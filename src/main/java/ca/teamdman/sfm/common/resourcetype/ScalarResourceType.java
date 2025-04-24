package ca.teamdman.sfm.common.resourcetype;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public abstract class ScalarResourceType<STACK, CAP> extends ResourceType<STACK, Class<STACK>, CAP> {
    public final ResourceLocation registryKey;
    public final Class<STACK> item;

    public ScalarResourceType(
            Capability<CAP> capability,
            ResourceLocation registryKey,
            Class<STACK> item
    ) {
        super(capability);
        this.registryKey = registryKey;
        this.item = item;
    }

    @Override
    public ResourceLocation getRegistryKeyForStack(STACK stack) {
        return registryKey;
    }

    @Override
    public ResourceLocation getRegistryKeyForItem(Class<STACK> item) {
        return registryKey;
    }

    @Override
    public @Nullable Class<STACK> getItemFromRegistryKey(ResourceLocation location) {
        if (location.equals(registryKey)) {
            return item;
        }
        return null;
    }

    @Override
    public Set<ResourceLocation> getRegistryKeys() {
        return Collections.singleton(registryKey);
    }

    @Override
    public Collection<Class<STACK>> getItems() {
        return Collections.singletonList(item);
    }

    @Override
    public boolean registryKeyExists(ResourceLocation location) {
        return location.equals(registryKey);
    }

    @Override
    public Class<STACK> getItem(STACK stack) {
        return item;
    }

    @Override
    public boolean matchesStackType(Object o) {
        return item.isInstance(o);
    }
}
