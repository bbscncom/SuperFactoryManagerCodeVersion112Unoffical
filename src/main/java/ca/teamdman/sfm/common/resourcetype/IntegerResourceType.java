package ca.teamdman.sfm.common.resourcetype;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import java.util.stream.Stream;

public abstract class IntegerResourceType<CAP> extends ScalarResourceType<Integer, CAP> {
    public IntegerResourceType(
            Capability<CAP> capability,
            ResourceLocation registryKey
    ) {
        super(capability, registryKey, Integer.class);
    }

    @Override
    public long getAmount(Integer stack) {
        return stack;
    }

    @Override
    public long getMaxStackSize(Integer integer) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmpty(Integer stack) {
        return stack == 0;
    }

    @Override
    public Integer getEmptyStack() {
        return 0;
    }

    @Override
    public Stream<ResourceLocation> getTagsForStack(Integer integer) {
        return Stream.empty();
    }

    @Override
    public Integer copy(Integer integer) {
        return integer;
    }

    @Override
    protected Integer setCount(
            Integer stack,
            long amount
    ) {
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    @Override
    public Integer withCount(
            Integer integer,
            long count
    ) {
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }
}
