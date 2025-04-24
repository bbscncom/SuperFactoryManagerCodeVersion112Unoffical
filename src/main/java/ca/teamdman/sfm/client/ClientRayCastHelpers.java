package ca.teamdman.sfm.client;

import ca.teamdman.sfm.common.util.SFMEnvironmentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClientRayCastHelpers {
    public static @Nullable TileEntity getLookBlockEntity() {
        if (!SFMEnvironmentUtils.isClient()) {
            throw new RuntimeException("getLookBlockEntity must be called on client");
        }
        World world = Minecraft.getMinecraft().world;
        if (world == null) return null;
        RayTraceResult rayTraceResult = Minecraft.getMinecraft().objectMouseOver;
        if (rayTraceResult == null) return null;
        if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return null;
        BlockPos pos = rayTraceResult.getBlockPos();
        return world.getTileEntity(pos);
    }
}
