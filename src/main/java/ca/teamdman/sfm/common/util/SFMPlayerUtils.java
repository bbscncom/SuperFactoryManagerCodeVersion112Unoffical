package ca.teamdman.sfm.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class SFMPlayerUtils {
    @MCVersionDependentBehaviour
    public static World getWorld(EntityPlayer player) {
        return player.world;
    }
}
