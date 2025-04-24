package ca.teamdman.sfm.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SFMContainerUtil {
    public static boolean isUseableByPlayer(TileEntity blockEntity, EntityPlayer player) {
        World world = blockEntity.getWorld();
        if (world == null) return false;
        BlockPos pos = blockEntity.getPos();
        if (world.getTileEntity(pos) != blockEntity) return false;
        double dist = player.getDistanceSq(
                (double) pos.getX() + 0.5D,
                (double) pos.getY() + 0.5D,
                (double) pos.getZ() + 0.5D
        );
        return dist <= 64.0D;
    }
}
