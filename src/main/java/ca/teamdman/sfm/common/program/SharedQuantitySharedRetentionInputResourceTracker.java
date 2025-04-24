package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfm.common.util.NotStored;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.ResourceLimit;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;

public class SharedQuantitySharedRetentionInputResourceTracker implements IInputResourceTracker {
    private final ResourceLimit resource_limit;
    private final ResourceIdSet exclusions;
    private final Long2ObjectOpenHashMap<Int2LongArrayMap> retention_obligations_by_pos_by_slot = new Long2ObjectOpenHashMap<>();
    private long transferred = 0;
    private long retention_obligation_progress = 0;

    public SharedQuantitySharedRetentionInputResourceTracker(
            ResourceLimit resourceLimit,
            ResourceIdSet exclusions
    ) {
        this.resource_limit = resourceLimit;
        this.exclusions = exclusions;
    }

    @Override
    public <STACK, CAP, ITEM> boolean isDone(
            ResourceType<STACK, ITEM, CAP> type,
            STACK stack
    ) {
        long can_transfer = resource_limit.limit().quantity().number().value();
        return transferred >= can_transfer;
    }

    @Override
    public <STACK, ITEM, CAP> long getRetentionObligationForSlot(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack,
            @NotStored BlockPos pos,
            int slot
    ) {
        Int2LongArrayMap posEntry = retention_obligations_by_pos_by_slot.get(pos.toLong());
        if (posEntry == null) {
            return 0;
        }
        return posEntry.getOrDefault(slot, 0L);
    }

    @Override
    public <STACK, ITEM, CAP> long getRemainingRetentionObligation(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack
    ) {
        return resource_limit.limit().retention().number().value() - retention_obligation_progress;
    }

    @Override
    public <STACK, ITEM, CAP> void trackRetentionObligation(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack,
            int slot,
            @NotStored BlockPos pos,
            long promise
    ) {
        this.retention_obligation_progress += promise;
        this.retention_obligations_by_pos_by_slot
                .computeIfAbsent(pos.toLong(), k -> new Int2LongArrayMap())
                .merge(slot, promise, Long::sum);
    }

    @Override
    public ResourceLimit getResourceLimit() {
        return resource_limit;
    }

    @Override
    public ResourceIdSet getExclusions() {
        return exclusions;
    }


    @Override
    public <STACK, ITEM, CAP> long getMaxTransferable(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack
    ) {
        return resource_limit.limit().quantity().number().value() - transferred;
    }

    @Override
    public <STACK, ITEM, CAP> void trackTransfer(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack,
            long amount
    ) {
        transferred += amount;
    }

    @Override
    public String toString() {
        return "SharedQuantitySharedRetentionInputResourceTracker@"
               + Integer.toHexString(System.identityHashCode(this))
               + "{"
               +
               "TRANSFERRED="
               + transferred
               +
               ", RETENTION_OBLIGATION_PROGRESS="
               + retention_obligation_progress
               +
               ", RESOURCE_LIMIT="
               + resource_limit
               +
               ", EXCLUSIONS="
               + exclusions
               +
               "}";
    }
}
