package ca.teamdman.sfm.common.cablenetwork;

import ca.teamdman.sfm.common.util.NotStored;
import ca.teamdman.sfm.common.util.SFMDirections;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public class CapabilityCache {
    // Position => Capability => EnumFacing => LazyOptional
    // We don't use an EnumMap here for EnumFacing because we need to support the null key
    private final Long2ObjectMap<Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>>> CACHE = new Long2ObjectOpenHashMap<>();
    // Chunk position => Set of Block positions
    private final Long2ObjectMap<LongArraySet> CHUNK_TO_BLOCK_POSITIONS = new Long2ObjectOpenHashMap<>();

    public void clear() {
        CACHE.clear();
        CHUNK_TO_BLOCK_POSITIONS.clear();
    }

    public int size() {
        return CACHE.values().stream().flatMap(x -> x.values().stream()).mapToInt(SFMDirections.NullableDirectionEnumMap::size).sum();
    }

    public void overwriteFromOther(@NotStored BlockPos pos, CapabilityCache other) {
        Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>> found = other.CACHE.get(pos.toLong());
        if (found != null) {
            CACHE.put(pos.toLong(), new Object2ObjectOpenHashMap<>(found));
        }
        addToChunkMap(pos);
    }

    public <CAP> @Nullable Capability getCapability(
            @NotStored BlockPos pos,
            Capability<CAP> capKind,
            @Nullable EnumFacing direction
    ) {
        Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>> capMap = CACHE.get(pos.toLong());
        if (capMap != null) {
            SFMDirections.NullableDirectionEnumMap<Capability> dirMap = capMap.get(capKind);
            if (dirMap != null) {
                Capability found = dirMap.get(direction);
                if (found == null) {
                    return null;
                } else {
                    //noinspection unchecked
                    return found;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void putAll(CapabilityCache other) {
        ObjectSet<Long2ObjectMap.Entry<Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>>>> entries = other.CACHE.long2ObjectEntrySet();
        for (Long2ObjectMap.Entry<Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>>> entry : entries) {
            long pos = entry.getLongKey();
            Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>> capMap = entry.getValue();
            for (Map.Entry<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>> e : capMap.entrySet()) {
                Capability<?> capKind = e.getKey();

                SFMDirections.NullableDirectionEnumMap<Capability> dirMap = e.getValue();
                for (EnumFacing direction : SFMDirections.DIRECTIONS) {
                    Capability cap = dirMap.get(direction);
                    if (cap != null) {
                        putCapability(BlockPos.fromLong(pos), (Capability) capKind, direction, cap);
                    }
                }
            }
        }
    }

    public Stream<BlockPos> getPositions() {
        return CACHE.keySet().stream().mapToLong(Long::new).mapToObj(BlockPos::fromLong);
    }

    public void remove(
            @NotStored BlockPos pos,
            Capability<?> capKind,
            @Nullable EnumFacing direction
    ) {
        Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>> capMap = CACHE.get(pos.toLong());
        if (capMap != null) {
            SFMDirections.NullableDirectionEnumMap<Capability> dirMap = capMap.get(capKind);
            if (dirMap != null) {
                dirMap.remove(direction);
                if (dirMap.isEmpty()) {
                    capMap.remove(capKind);
                    if (capMap.isEmpty()) {
                        CACHE.remove(pos.toLong());
                    }
                }
                removeFromChunkMap(pos);
            }
        }
    }

    public <CAP> void putCapability(
            @NotStored BlockPos pos,
            Capability<CAP> capKind,
            @Nullable EnumFacing direction,
            Capability<CAP> cap
    ) {
        Object2ObjectOpenHashMap<Capability<?>, SFMDirections.NullableDirectionEnumMap<Capability>> capMap = CACHE.computeIfAbsent(pos.toLong(), k -> new Object2ObjectOpenHashMap<>());
        SFMDirections.NullableDirectionEnumMap<Capability> dirMap = capMap.computeIfAbsent(capKind, k -> new SFMDirections.NullableDirectionEnumMap<>());
        dirMap.put(direction, cap);
        addToChunkMap(pos);
    }

    public void bustCacheForChunk(Chunk chunk) {
        long chunkKey = asLong(chunk.x,chunk.z);
        LongArraySet blockPositions = CHUNK_TO_BLOCK_POSITIONS.get(chunkKey);
        if (blockPositions != null) {
            for (long blockPos : blockPositions) {
                CACHE.remove(blockPos);
            }
            CHUNK_TO_BLOCK_POSITIONS.remove(chunkKey);
        }
    }

    private void addToChunkMap(@NotStored BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long chunkKey = asLong(chunkPos.x,chunkPos.z);
        long blockPos = pos.toLong();
        CHUNK_TO_BLOCK_POSITIONS.computeIfAbsent(chunkKey, k -> new LongArraySet()).add(blockPos);
    }

    public static long asLong(int pX, int pZ) {
        return (long)pX & 4294967295L | ((long)pZ & 4294967295L) << 32;
    }
    private void removeFromChunkMap(@NotStored BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long chunkKey = asLong(chunkPos.x,chunkPos.z);
        long blockPos = pos.toLong();
        LongArraySet blockPosSet = CHUNK_TO_BLOCK_POSITIONS.get(chunkKey);
        if (blockPosSet != null) {
            blockPosSet.remove(blockPos);
            if (blockPosSet.isEmpty()) {
                CHUNK_TO_BLOCK_POSITIONS.remove(chunkKey);
            }
        }
    }
}
