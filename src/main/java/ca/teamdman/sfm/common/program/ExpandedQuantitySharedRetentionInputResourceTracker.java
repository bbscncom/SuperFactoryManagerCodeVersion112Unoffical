package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfm.common.util.NotStored;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.ResourceLimit;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("DuplicatedCode")
public class ExpandedQuantitySharedRetentionInputResourceTracker implements IInputResourceTracker {
    private final ResourceLimit resource_limit;
    private final ResourceIdSet exclusions;
    private final Object2ObjectOpenHashMap<ResourceType<?, ?, ?>, Object2LongOpenHashMap<ResourceLocation>>
            transferred_by_item = new Object2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<Int2LongArrayMap> retention_obligations_by_pos_by_slot = new Long2ObjectOpenHashMap<>();
    private long retention_obligation_progress = 0;

    public ExpandedQuantitySharedRetentionInputResourceTracker(
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
        long transferred_for_item = 0;
        Object2LongOpenHashMap<ResourceLocation> transferred_for_resource_type = transferred_by_item.get(type);
        if (transferred_for_resource_type != null) {
            ResourceLocation item_id = type.getRegistryKeyForStack(stack);
            transferred_for_item = transferred_for_resource_type.getLong(item_id);
        }
        return transferred_for_item >= can_transfer;
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
    public <STACK, ITEM, CAP> long getMaxTransferable(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack
    ) {
        long max_transfer = resource_limit.limit().quantity().number().value();
        long transferred_for_item = 0;
        Object2LongOpenHashMap<ResourceLocation> transferred_for_resource_type = transferred_by_item.get(resourceType);
        if (transferred_for_resource_type != null) {
            ResourceLocation item_id = resourceType.getRegistryKeyForStack(stack);
            transferred_for_item = transferred_for_resource_type.getLong(item_id);
        }
        return max_transfer - transferred_for_item;
    }

    @Override
    public <STACK, ITEM, CAP> void trackTransfer(
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack,
            long amount
    ) {
        ResourceLocation item_id = resourceType.getRegistryKeyForStack(stack);
        transferred_by_item.computeIfAbsent(resourceType, k -> new Object2LongOpenHashMap<>())
                .addTo(item_id, amount);
    }

}
