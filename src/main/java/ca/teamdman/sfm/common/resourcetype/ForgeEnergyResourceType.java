package ca.teamdman.sfm.common.resourcetype;


import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyResourceType extends IntegerResourceType<IEnergyStorage> {
    public ForgeEnergyResourceType() {
        super(CapabilityEnergy.ENERGY, new ResourceLocation("forge", "energy"));
    }

    @Override
    public Integer extract(
            IEnergyStorage iEnergyStorage,
            int slot,
            long amount,
            boolean simulate
    ) {
        int finalAmount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
        return iEnergyStorage.extractEnergy(finalAmount, simulate);
    }

    @Override
    public int getSlots(IEnergyStorage handler) {
        return 1;
    }

    @Override
    public Integer insert(
            IEnergyStorage iEnergyStorage,
            int slot,
            Integer stack,
            boolean simulate
    ) {
        int accepted = iEnergyStorage.receiveEnergy(stack, simulate);
        return stack - accepted;
    }

    @Override
    public boolean matchesCapabilityType(Object o) {
        return o instanceof IEnergyStorage;
    }

    @Override
    public long getMaxStackSizeForSlot(
            IEnergyStorage iEnergyStorage,
            int slot
    ) {
        int maxStackSize = iEnergyStorage.getMaxEnergyStored();
        if (maxStackSize == Integer.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return maxStackSize;
    }

    @Override
    public Integer getStackInSlot(
            IEnergyStorage iEnergyStorage,
            int slot
    ) {
        return iEnergyStorage.getEnergyStored();
    }
}
