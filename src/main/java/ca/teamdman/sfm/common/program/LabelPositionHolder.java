package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.capability.AbstarctCapabilityProvider;
import ca.teamdman.sfm.common.capability.CapData;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class LabelPositionHolder implements CapData<LabelPositionHolder> {
    public LabelPositionHolder data;
    public Map<String, HashSet<BlockPos>> labels;

    public Map<String, HashSet<BlockPos>> Labels() {
        return labels;
    }

    public void setLabels(Map<String, HashSet<BlockPos>> labels) {
        this.labels = labels;
    }


    public LabelPositionHolder() {
        this.data = this;
        this.labels = new HashMap<>();
    }

    private LabelPositionHolder(LabelPositionHolder other) {
        this();
        other.Labels().forEach((key, value) -> this.Labels().put(key, new HashSet<>(value)));
    }

    public static boolean encode(
            LabelPositionHolder labelPositionHolder,
            PacketBuffer friendlyByteBuf
    ) {
        int size = labelPositionHolder.Labels().size();
        if(size ==0) return false;
        friendlyByteBuf.writeVarInt(size);
        for (Map.Entry<String, ? extends Set<BlockPos>> entry : labelPositionHolder.Labels().entrySet()) {
            String label = entry.getKey();
            Set<BlockPos> positions = entry.getValue();
            friendlyByteBuf.writeString(label);
            friendlyByteBuf.writeVarInt(positions.size());
            positions.forEach(friendlyByteBuf::writeBlockPos);
        }
        return true;
    }

    public static LabelPositionHolder decode(PacketBuffer friendlyByteBuf) {
        LabelPositionHolder rtn = LabelPositionHolder.empty();
        int size = friendlyByteBuf.readVarInt();
        for (int i = 0; i < size; i++) {
            String label = friendlyByteBuf.readString(32767);
            int positionsSize = friendlyByteBuf.readVarInt();
            HashSet<BlockPos> positions = new HashSet<>();
            for (int j = 0; j < positionsSize; j++) {
                positions.add(friendlyByteBuf.readBlockPos());
            }
            rtn.Labels().put(label, positions);
        }
        return rtn;
    }


    public static LabelPositionHolder from(ItemStack stack) {
        LabelPositionHolder fromNBT = AbstarctCapabilityProvider.readSingleCapabilityFromNBT(stack, SFMDataComponents.LABEL_POSITION_HOLDER);
        LabelPositionHolder localCap = stack.getCapability(SFMDataComponents.LABEL_POSITION_HOLDER, null);
        if (fromNBT==null) {
            return localCap;
        } else{
            localCap.data=fromNBT.data;
            return fromNBT.data;
        }
    }

    public static LabelPositionHolder empty() {
        return new LabelPositionHolder();
    }

    public LabelPositionHolder save(ItemStack stack) {
        LabelPositionHolder copy = new LabelPositionHolder(this);
        stack.getCapability(SFMDataComponents.LABEL_POSITION_HOLDER, null).data = copy;
        AbstarctCapabilityProvider.updateSingleCapabilityToNBT(stack, SFMDataComponents.LABEL_POSITION_HOLDER);
        return this;
    }

    public static void clearNbt(ItemStack stack) {
        stack.getCapability(SFMDataComponents.LABEL_POSITION_HOLDER, null).clear();
        AbstarctCapabilityProvider.updateSingleCapabilityToNBT(stack, SFMDataComponents.LABEL_POSITION_HOLDER);
    }

    public boolean contains(
            String label,
            BlockPos pos
    ) {
        HashSet<BlockPos> positionsForLabel = this.Labels().get(label);
        if (positionsForLabel == null) {
            return false;
        } else {
            return positionsForLabel.contains(pos);
        }
    }

    public LabelPositionHolder toggle(
            String label,
            BlockPos pos
    ) {
        if (contains(label, pos)) {
            remove(label, pos);
        } else {
            add(label, pos);
        }
        return this;
    }

    public Set<BlockPos> getPositions(String label) {
        return Labels().getOrDefault(label, new HashSet<>());
    }

    public Set<BlockPos> getPositionsMut(String label) {
        return Labels().computeIfAbsent(label, s -> new HashSet<>());
    }

    public LabelPositionHolder addAll(
            String label,
            Collection<BlockPos> positions
    ) {
        getPositionsMut(label).addAll(positions);
        return this;
    }

    public LabelPositionHolder addReferencedLabel(String label) {
        getPositionsMut(label);
        return this;
    }


    public List<ITextComponent> asHoverText() {
        ArrayList<ITextComponent> rtn = new ArrayList<ITextComponent>();
        if (Labels().isEmpty()) return rtn;
        rtn.add(LocalizationKeys.DISK_ITEM_TOOLTIP_LABEL_HEADER
                .getComponent()
                .setStyle(new Style().setUnderlined(true)));
        for (Map.Entry<String, HashSet<BlockPos>> entry : Labels().entrySet()) {
            rtn.add(LocalizationKeys.DISK_ITEM_TOOLTIP_LABEL.getComponent(
                    entry.getKey(),
                    entry.getValue().size()
            ).setStyle(new Style().setColor(TextFormatting.GRAY)));
        }
        return rtn;
    }


    public String toDebugString() {
        int total = 0;
        StringBuilder rtn = new StringBuilder();
        for (Map.Entry<String, HashSet<BlockPos>> entry : Labels().entrySet()) {
            rtn.append("-- * ").append(entry.getKey()).append(" - ").append(entry.getValue().size()).append(" positions\n");
            total += entry.getValue().size();
        }
        return "-- LabelPositionHolder - " + total + " total labels\n" + rtn;
    }

    public LabelPositionHolder removeAll(BlockPos value) {
        Labels().values().forEach(list -> list.remove(value));
        return this;
    }

    public LabelPositionHolder prune() {
        Labels().entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return this;
    }

    public void clear() {
        Labels().clear();
        this.data.labels.clear();
    }

    @Override
    public void set(LabelPositionHolder data) {
        this.data=data;
    }

    @Override
    public LabelPositionHolder get() {
        return this.data;
    }

    public LabelPositionHolder add(
            String label,
            BlockPos position
    ) {
        getPositionsMut(label).add(position);
        return this;
    }

    public LabelPositionHolder remove(
            String label,
            BlockPos pos
    ) {
        getPositionsMut(label).remove(pos);
        return this;
    }

    public LabelPositionHolder removeIf(BiPredicate<String, BlockPos> predicate) {
        Labels().forEach((key, value) -> value.removeIf(pos -> predicate.test(key, pos)));
        return this;
    }

    public LabelPositionHolder removeIf(Predicate<String> predicate) {
        Labels().keySet().removeIf(predicate);
        return this;
    }

    public LabelPositionHolder forEach(BiConsumer<String, BlockPos> consumer) {
        Labels().forEach((key, value) -> value.forEach(pos -> consumer.accept(key, pos)));
        return this;
    }

    public LabelPositionHolder toOwned() {
        return new LabelPositionHolder(this);
    }

    public Set<String> getLabels(BlockPos pos) {
        return Labels().entrySet().stream()
                .filter(entry -> entry.getValue().contains(pos))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }


    public boolean isEmpty() {
        return Labels().isEmpty();
    }
}
