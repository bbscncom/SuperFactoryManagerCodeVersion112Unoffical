package ca.teamdman.sfm.common.resourcetype;

import my.Tools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class FluidExtend extends Fluid implements IForgeRegistryEntry<FluidExtend> {


    public FluidExtend(String fluidName, ResourceLocation still, ResourceLocation flowing, Color color) {
        super(fluidName, still, flowing, color);
    }

    public static FluidExtend wrapper(Fluid fluid){
        FluidExtend fluidExtend = new FluidExtend(fluid.getName(), fluid.getStill(), fluid.getFlowing(), Tools.intToColor(fluid.getColor()));
        return fluidExtend;
    }
    @Override
    public FluidExtend setRegistryName(ResourceLocation name) {
        throw new UnsupportedOperationException("wrapper only for read");
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(getUnlocalizedName());
    }

    @Override
    public Class<FluidExtend> getRegistryType() {
        return (Class<FluidExtend>) this.getClass();
    }
}
