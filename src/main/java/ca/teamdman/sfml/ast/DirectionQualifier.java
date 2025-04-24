package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.util.SFMDirections;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class DirectionQualifier implements ASTNode, Iterable<EnumFacing> {
    public static final DirectionQualifier NULL_DIRECTION = new DirectionQualifier(EnumSet.noneOf(EnumFacing.class));
    public static final DirectionQualifier EVERY_DIRECTION = new DirectionQualifier(EnumSet.allOf(EnumFacing.class));
    
    private final EnumSet<EnumFacing> directions;

    public DirectionQualifier(EnumSet<EnumFacing> directions) {
        this.directions = directions;
    }

    public EnumSet<EnumFacing> directions() {
        return directions;
    }

    public static EnumFacing lookup(Side side) {
        switch (side) {
            case TOP: return EnumFacing.UP;
            case BOTTOM: return EnumFacing.DOWN;
            case NORTH: return EnumFacing.NORTH;
            case SOUTH: return EnumFacing.SOUTH;
            case EAST: return EnumFacing.EAST;
            case WEST: return EnumFacing.WEST;
            default: throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    public static String directionToString(@Nullable EnumFacing direction) {
        if (direction == null) return "";
        switch (direction) {
            case UP: return "TOP";
            case DOWN: return "BOTTOM";
            case NORTH: return "NORTH";
            case SOUTH: return "SOUTH";
            case EAST: return "EAST";
            case WEST: return "WEST";
            default: return "";
        }
    }

    public Stream<EnumFacing> stream() {
        if (this == EVERY_DIRECTION)
            return Stream.concat(directions.stream(), Stream.<EnumFacing>builder().add(null).build());
        if (directions.isEmpty()) return Stream.<EnumFacing>builder().add(null).build();
        return directions.stream();
    }

    @Override
    public Iterator<EnumFacing> iterator() {
        if (this == EVERY_DIRECTION) {
            return new SFMDirections.NullableDirectionIterator();
        }
        if (directions.isEmpty()) {
            return new SFMDirections.SingleNullDirectionIterator();
        }
        return directions.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectionQualifier)) return false;
        DirectionQualifier that = (DirectionQualifier) o;
        return Objects.equals(directions, that.directions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(directions);
    }
}
