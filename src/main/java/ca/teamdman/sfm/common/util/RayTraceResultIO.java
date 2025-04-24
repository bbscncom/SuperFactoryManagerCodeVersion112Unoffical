package ca.teamdman.sfm.common.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class RayTraceResultIO {
    // 固定字节数：Vec3d (24) + EnumFacing (1) + BlockPos (12) = 37 字节
    private static final int FIXED_BYTE_SIZE = 37;

    public static void writeFixed(ByteBuf buf, RayTraceResult result) {
        if (result == null) {
            // 如果结果为 null，写入默认值
            buf.writeDouble(0).writeDouble(0).writeDouble(0); // Vec3d
            buf.writeByte(EnumFacing.DOWN.getIndex()); // EnumFacing
            buf.writeLong(BlockPos.ORIGIN.toLong()); // BlockPos
            return;
        }

        // 写入 Vec3d
        buf.writeDouble(result.hitVec.x)
           .writeDouble(result.hitVec.y)
           .writeDouble(result.hitVec.z);

        // 写入 EnumFacing
        buf.writeByte(result.sideHit.getIndex());

        // 写入 BlockPos
        buf.writeLong(result.getBlockPos().toLong());
    }

    public static RayTraceResult readFixed(ByteBuf buf) {
        // 读取 Vec3d
        Vec3d hitVec = new Vec3d(
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble()
        );

        // 读取 EnumFacing
        EnumFacing sideHit = EnumFacing.byIndex(buf.readByte());

        // 读取 BlockPos
        BlockPos blockPos = BlockPos.fromLong(buf.readLong());

        return new RayTraceResult(hitVec, sideHit, blockPos);
    }

    public static int getFixedByteSize() {
        return FIXED_BYTE_SIZE;
    }
}