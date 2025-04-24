package ca.teamdman.sfm.common.registry;


import ca.teamdman.sfm.common.util.SFMDirections;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

;

public class SFMBlockCapabilities {

    public static boolean hasAnyCapabilityAnyDirection(World world,BlockPos pos) {
        return SFMResourceTypes.getCapabilities().anyMatch(cap -> {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity==null)return false;
            for (EnumFacing direction : SFMDirections.DIRECTIONS_WITH_NULL) {
                if (tileEntity.hasCapability(cap,direction)&&tileEntity.getCapability(cap, direction) != null) {
                    return true;
                }
            }
            return false;
        });
    }

    public static boolean hasAnyCapability(World world, BlockPos pos, @Nullable EnumFacing direction) {
        return SFMResourceTypes.getCapabilities().anyMatch(cap -> world.getTileEntity(pos).getCapability(cap, direction) != null);
    }
}
