package ca.teamdman.sfm.common.util;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

public class SFMDirections {
    /// Optimization to avoid creating a new array every time
    public static final EnumFacing[] DIRECTIONS = EnumFacing.values();
    /// Optimization to avoid creating a new array every time. Null is position 0
    public static final EnumFacing[] DIRECTIONS_WITH_NULL = new EnumFacing[]{
            null,
            EnumFacing.NORTH,
            EnumFacing.SOUTH,
            EnumFacing.EAST,
            EnumFacing.WEST,
            EnumFacing.UP,
            EnumFacing.DOWN
    };

    public static class NullableDirectionIterator implements Iterator<EnumFacing> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < DIRECTIONS_WITH_NULL.length;
        }

        @Override
        public @Nullable EnumFacing next() {
            if (hasNext()) {
                return DIRECTIONS_WITH_NULL[index++];
            }
            throw new NoSuchElementException();
        }
    }

    public static class SingleNullDirectionIterator implements Iterator<EnumFacing> {
        private boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public @Nullable EnumFacing next() {
            if (hasNext) {
                hasNext = false;
                return null;
            }
            throw new NoSuchElementException();
        }
    }

    public static class NullableDirectionEnumMap<T> {
        private final T[] buckets;

        public NullableDirectionEnumMap() {
            //noinspection unchecked
            this.buckets = (T[]) new Object[DIRECTIONS_WITH_NULL.length];
        }

        @SuppressWarnings("unused")
        public boolean containsKey(@Nullable EnumFacing direction) {
            return buckets[keyFor(direction)] != null;
        }

        public void forEach(BiConsumer<EnumFacing, T> callback) {
            for (EnumFacing direction : DIRECTIONS_WITH_NULL) {
                T value = buckets[keyFor(direction)];
                if (value != null) {
                    callback.accept(direction, value);
                }
            }
        }

        public void remove(@Nullable EnumFacing direction) {
            buckets[keyFor(direction)] = null;
        }

        public boolean isEmpty() {
            for (T bucket : buckets) {
                if (bucket != null) {
                    return false;
                }
            }
            return true;
        }

        public void put(
                @Nullable EnumFacing direction,
                T value
        ) {
            buckets[keyFor(direction)] = value;
        }

        public @Nullable T get(@Nullable EnumFacing direction) {
            return buckets[keyFor(direction)];
        }

        public int size() {
            int count = 0;
            for (T bucket : buckets) {
                if (bucket != null) {
                    count++;
                }
            }
            return count;
        }

        private int keyFor(@Nullable EnumFacing direction) {
            return direction == null ? 0 : direction.ordinal() + 1;
        }
    }
}
