package ca.teamdman.sfm.common.resourcetype;


import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FluidResourceType extends RegistryBackedResourceType<FluidStack, FluidExtend, IFluidHandler> {
    public static FluidRegistryWrapper fluidRegistryWrapper = FluidRegistryWrapper.INSTANCE;
    public static boolean isWrapper = false;

    public FluidResourceType() {
        super(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    @Override
    public IForgeRegistry<FluidExtend> getRegistry() {
        if (!isWrapper) {
            Map<String, Fluid> regs = FluidRegistry.getRegisteredFluids();
            fluidRegistryWrapper.wrapper(regs);
            isWrapper = true;
        }
        return fluidRegistryWrapper;
    }

    @Override
    public FluidExtend getItem(FluidStack fluidStack) {
        return FluidExtend.wrapper(fluidStack.getFluid());
    }

    @Override
    public FluidStack copy(FluidStack fluidStack) {
        return fluidStack.copy();
    }

    @Override
    public Stream<ResourceLocation> getTagsForStack(FluidStack fluidStack) {
        //noinspection deprecation
        //todo 1.12 没有原生fluid tag 暂时返回空
//        return fluidStack.getFluid().builtInRegistryHolder().tags().map(TagKey::location);
        return Stream.empty();
    }

    @Override
    protected FluidStack setCount(FluidStack fluidStack, long amount) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        fluidStack.amount = finalAmount;
        return fluidStack;
    }

    @Override
    public long getAmount(FluidStack stack) {
        if (stack == null) return 0;
        return stack.amount;
    }

    @Override
    public FluidStack getStackInSlot(IFluidHandler cap, int slot) {
        FluidStack contents = cap.getTankProperties()[slot].getContents();
        return contents == null ? null : contents;
    }

    @Override
    public FluidStack extract(IFluidHandler handler, int slot, long amount_long, boolean simulate) {
        FluidStack in = getStackInSlot(handler, slot);
        if (in == null) {
            return null;
        }
        FluidStack toExtract = new FluidStack(
                in.getFluid(), // 直接获取流体对象
                (int) Math.min(amount_long, Integer.MAX_VALUE)
        );
        return handler.drain(
                toExtract,
                !simulate
        );
    }

    @Override
    public FluidStack insert(IFluidHandler handler, int slot, FluidStack stack, boolean simulate) {
        // 1.12 版本替换方案
        int inserted = handler.fill(stack, simulate);
        int remainder = stack.amount - inserted;
        return new FluidStack(stack.getFluid(), remainder); // 移除componentsPatch
    }

    @Override
    public boolean matchesStackType(Object o) {
        return o instanceof FluidStack;
    }

    @Override
    public boolean matchesCapabilityType(Object o) {
        return o instanceof IFluidHandler;
    }


    @Override
    public long getMaxStackSize(FluidStack fluidStack) {
        return Integer.MAX_VALUE;
    }


    @Override
    public int getSlots(IFluidHandler handler) {
        IFluidTankProperties[] props = handler.getTankProperties();
        return props == null ? 0 : props.length;
    }

    @Override
    public long getMaxStackSizeForSlot(IFluidHandler handler, int slot) {
        IFluidTankProperties[] props = handler.getTankProperties();
        if (props == null || slot < 0 || slot >= props.length) {
            return 0;
        }
        return props[slot].getCapacity(); // 容量就是最大“堆叠”大小（以 mB 为单位）
    }


    @Override
    public boolean isEmpty(FluidStack stack) {
        if (stack == null) return true;
        return stack.amount == 0;
    }


    @Override
    public FluidStack getEmptyStack() {
        return null;
    }
}
