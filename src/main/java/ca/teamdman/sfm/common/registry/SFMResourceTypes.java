package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.common.resourcetype.*;
import ca.teamdman.sfm.common.util.MCVersionDependentBehaviour;
import com.google.common.collect.HashBiMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SFMResourceTypes implements Registry<ResourceType> {
    private static final CapabilityManager register = CapabilityManager.INSTANCE;

    private static SFMResourceTypes obj;
    public static SFMResourceTypes single(){if(obj ==null){return obj=new SFMResourceTypes();} else return obj;}
    public static final HashBiMap<ResourceLocation, ResourceType> RESOURCE_TYPES = HashBiMap.create();

    public static Supplier register(String name, ResourceType<?, ?, ?> type) {
        RESOURCE_TYPES.put(new ResourceLocation("sfm",name), type);
        return ()->type;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return RESOURCE_TYPES.keySet();
    }

    @Override
    public ResourceType get(ResourceLocation location) {
        return RESOURCE_TYPES.get(location);
    }

    public ResourceLocation getKey(ResourceType type){
        return RESOURCE_TYPES.inverse().get(type);
    }

    @Override
    public Stream<ResourceType> stream() {
        return RESOURCE_TYPES.values().stream();
    }

    @Override
    public boolean containsKey(ResourceLocation location) {
        return RESOURCE_TYPES.containsKey(location);
    }

    public static final Supplier<ResourceType<ItemStack, Item, IItemHandler>> ITEM = register(
            "item",
            new ItemResourceType()
    );
    public static final Supplier<ResourceType<FluidStack, Fluid, IFluidHandler>> FLUID = register(
            "fluid",
            new FluidResourceType()
    );
    public static final Supplier<ResourceType<Integer, Class<Integer>, IEnergyStorage>> FORGE_ENERGY = register(
            "forge_energy",
            new ForgeEnergyResourceType()
    );

    //todo
//    static {
//        if (SFMModCompat.isMekanismLoaded()) {
//            SFMMekanismCompat.registerResourceTypes(new SFMResourceTypes());
//        }
//    }

    public static int getResourceTypeCount() {
        return RESOURCE_TYPES.size();
    }

    public static @Nullable ResourceType<?, ?, ?> fastLookup(
            ResourceLocation resourceTypeId
    ) {
        return RESOURCE_TYPES.get(resourceTypeId);
    }

    public static Stream<Capability<?>> getCapabilities() {
        return RESOURCE_TYPES.values().stream().map(resourceType -> resourceType.CAPABILITY_KIND);
    }

    @MCVersionDependentBehaviour
    public static SFMResourceTypes registry() {
        return single();
    }

    /* TODO: add support for new resource types
     * - mekanism heat
     * - botania mana
     * - ars nouveau source
     * - flux plugs
     * - PNC pressure
     * - PNC heat
     * - nature's aura aura
     * - create rotation
     */
}
