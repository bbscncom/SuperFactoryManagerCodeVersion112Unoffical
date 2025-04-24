package my;

import net.minecraft.util.EnumFacing;

public class MyBlockState {
    private final EnumFacing facing;
    private final boolean powered;
    private final int level;

    public MyBlockState() {
        this(EnumFacing.NORTH, false, 0);
    }

    public MyBlockState(EnumFacing facing, boolean powered, int level) {
        this.facing = facing;
        this.powered = powered;
        this.level = level;
    }

    // --- Getter ---
    public EnumFacing getFacing() {
        return facing;
    }

    public boolean isPowered() {
        return powered;
    }

    public int getLevel() {
        return level;
    }

    // --- 状态替换方法（链式）---
    public MyBlockState withFacing(EnumFacing newFacing) {
        return new MyBlockState(newFacing, this.powered, this.level);
    }

    public MyBlockState withPowered(boolean newPowered) {
        return new MyBlockState(this.facing, newPowered, this.level);
    }

    public MyBlockState withLevel(int newLevel) {
        return new MyBlockState(this.facing, this.powered, newLevel);
    }

    // --- 状态比较、调试用 ---
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MyBlockState)) return false;
        MyBlockState other = (MyBlockState) obj;
        return this.facing == other.facing && this.powered == other.powered && this.level == other.level;
    }

    @Override
    public int hashCode() {
        return facing.hashCode() * 31 + (powered ? 1 : 0) * 17 + level;
    }

    @Override
    public String toString() {
        return "MyBlockState[facing=" + facing + ", powered=" + powered + ", level=" + level + "]";
    }
}
