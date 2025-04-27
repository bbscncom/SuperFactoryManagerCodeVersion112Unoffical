package ca.teamdman.sfm.common.cablenetwork;

import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.logging.TranslatableLogger;
import ca.teamdman.sfm.common.util.NotStored;
import ca.teamdman.sfm.common.util.SFMDirections;
import ca.teamdman.sfm.common.util.SFMStreamUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import my.Tools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CableNetwork {
    protected final World world;
    protected final LongSet CABLE_POSITIONS = new LongOpenHashSet();
    protected final CapabilityCache CAPABILITY_CACHE = new CapabilityCache();

    public CableNetwork(World world) {
        this.world = world;
    }

    public static boolean isCable(
            @Nullable World world,
            @NotStored BlockPos cablePos
    ) {
        if (world == null) return false;
        return world
                .getBlockState(cablePos)
                .getBlock() instanceof ICableBlock;
    }

    public void rebuildNetwork(@NotStored BlockPos start) {
        CABLE_POSITIONS.clear();
        CAPABILITY_CACHE.clear();
        discoverCables(getWorld(), start).forEach(this::addCable);
    }

    public void rebuildNetworkFromCache(
            @NotStored BlockPos start,
            CableNetwork other
    ) {
        CABLE_POSITIONS.clear();
        CAPABILITY_CACHE.clear();

        // discover connected cables
        List<BlockPos> cables = SFMStreamUtils.<BlockPos, BlockPos>getRecursiveStream((current, next, results) -> {
            results.accept(current);
            BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
            for (EnumFacing d : SFMDirections.DIRECTIONS) {
                target.setPos(current).move(d);
                if (other.containsCablePosition(target)) {
                    next.accept(new BlockPos(target));
                }
            }
        }, start).collect(Collectors.toList());

        // restore cable positions
        for (BlockPos cablePos : cables) {
            CABLE_POSITIONS.add(cablePos.toLong());
        }

        // restore capabilities
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        LongSet seenCapabilityPositions = new LongOpenHashSet();
        for (BlockPos cablePos : cables) {
            for (EnumFacing direction : SFMDirections.DIRECTIONS) {
                target.setPos(cablePos).move(direction);
                boolean firstVisit = seenCapabilityPositions.add(target.toLong());
                if (firstVisit) {
                    CAPABILITY_CACHE.overwriteFromOther(target, other.CAPABILITY_CACHE);
                }
            }
        }
    }

    public static Stream<BlockPos> discoverCables(
            World world,
            @NotStored BlockPos startPos
    ) {
        return SFMStreamUtils.getRecursiveStream((current, next, results) -> {
            results.accept(current);
            BlockPos.PooledMutableBlockPos target = BlockPos.PooledMutableBlockPos.retain();
            try {
                for (EnumFacing d : SFMDirections.DIRECTIONS) {
                    target.setPos(current).move(d);
                    if (isCable(world, target)) {
                        next.accept(target.toImmutable());
                    }
                }
            } finally {
                target.release();
            }
        }, startPos);
    }

    public void addCable(@NotStored BlockPos pos) {
        CABLE_POSITIONS.add(pos.toLong());
    }

    public World getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "CableNetwork{level="
                + getWorld().provider.getDimension()
                + ", #cables="
                + getCableCount()
                + ", #cache="
                + CAPABILITY_CACHE.size()
                + "}";
    }

    public boolean isAdjacentToCable(@NotStored BlockPos pos) {
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        for (EnumFacing direction : SFMDirections.DIRECTIONS) {
            target.setPos(pos).move(direction);
            if (containsCablePosition(target)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsCablePosition(@NotStored BlockPos pos) {
        return CABLE_POSITIONS.contains(pos.toLong());
    }

    public <CAP> @Nullable CAP getCapability(
            Capability<CAP> capKind,
            @NotStored BlockPos pos,
            @Nullable EnumFacing direction,
            TranslatableLogger logger
    ) {
        @javax.annotation.Nullable Capability found = CAPABILITY_CACHE.getCapability(pos, capKind, direction);
        if (found != null) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (direction==null && Tools.isCantNullDirection(tileEntity))
                direction = EnumFacing.NORTH;
            CAP capability = tileEntity != null ? tileEntity.getCapability(capKind, direction) : null;
//            CAP cap = found.getCapability(pos,capKind,direction);
            if (capability != null) {
                @Nullable EnumFacing finalDirection = direction;
                logger.trace(x -> x.accept(LocalizationKeys.LOG_CAPABILITY_CACHE_HIT.get(
                        pos,
                        capKind.getName(),
                        finalDirection
                )));
                return capability;
            } else {
                @Nullable EnumFacing finalDirection1 = direction;
                logger.trace(x -> x.accept(LocalizationKeys.LOG_CAPABILITY_CACHE_HIT_INVALID.get(
                        pos,
                        capKind.getName(),
                        finalDirection1
                )));
            }
        } else {
            @Nullable EnumFacing finalDirection2 = direction;
            logger.trace(x -> x.accept(LocalizationKeys.LOG_CAPABILITY_CACHE_MISS.get(pos, capKind.getName(), finalDirection2)));
        }

        if (!isAdjacentToCable(pos)) {
            logger.warn(x -> x.accept(LocalizationKeys.LOGS_MISSING_ADJACENT_CABLE.get(pos)));
            return null;
        }
        TileEntity tileEntity = world.getTileEntity(pos);

        //thermal has is true ,but get and extract null exception
        //((IEnergyStorage)tileEntity.getCapability(capKind, direction)).extractEnergy(1,true)
        if (direction == null && Tools.isCantNullDirection(tileEntity))
            direction = EnumFacing.NORTH;
        CAP capability = tileEntity != null && tileEntity.hasCapability(capKind, direction) ? tileEntity.getCapability(capKind, direction) : null;

        if (capability != null) {
            if (!(getWorld() instanceof WorldServer)) {
                return null;
            }
            /*found = new Capability()
                    capKind,
                    worldServer,
                    pos,
                    direction,
                    () -> true,
                    () ->  CAPABILITY_CACHE.remove(pos, capKind, direction)
            );*/
            CAPABILITY_CACHE.putCapability(pos, capKind, direction, capKind);
        } else {
            @Nullable EnumFacing finalDirection3 = direction;
            logger.warn(x -> x.accept(LocalizationKeys.LOGS_EMPTY_CAPABILITY.get(pos, capKind.getName(), finalDirection3)));
        }
        return capability;
    }

    public int getCableCount() {
        return CABLE_POSITIONS.size();
    }

    public void mergeNetwork(CableNetwork other) {
        CABLE_POSITIONS.addAll(other.CABLE_POSITIONS);
        CAPABILITY_CACHE.putAll(other.CAPABILITY_CACHE);
    }

    public boolean isEmpty() {
        return CABLE_POSITIONS.isEmpty();
    }

    public Stream<BlockPos> getCablePositions() {
        return CABLE_POSITIONS.stream().mapToLong(Long::new).mapToObj(BlockPos::fromLong);
    }

    public LongSet getCablePositionsRaw() {
        return CABLE_POSITIONS;
    }

    public Stream<BlockPos> getCapabilityProviderPositions() {
        return CAPABILITY_CACHE.getPositions();
    }

    public void bustCacheForChunk(Chunk chunk) {
        CAPABILITY_CACHE.bustCacheForChunk(chunk);
    }

    protected List<CableNetwork> withoutCable(@NotStored BlockPos cablePos) {
        CABLE_POSITIONS.remove(cablePos.toLong());
        List<CableNetwork> branches = new ArrayList<>();
        BlockPos.PooledMutableBlockPos target = BlockPos.PooledMutableBlockPos.retain();
        try {
            for (EnumFacing direction : SFMDirections.DIRECTIONS) {
                target.setPos(cablePos).move(direction);
                if (!containsCablePosition(target)) continue;
                if (branches.stream().anyMatch(n -> n.containsCablePosition(target))) continue;
                CableNetwork branchNetwork = new CableNetwork(this.getWorld());
                branchNetwork.rebuildNetworkFromCache(target, this);
                branches.add(branchNetwork);
            }
        } finally {
            target.release();
        }
        return branches;
    }
}
