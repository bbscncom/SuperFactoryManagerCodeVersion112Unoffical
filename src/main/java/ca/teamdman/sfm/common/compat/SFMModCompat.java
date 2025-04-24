package ca.teamdman.sfm.common.compat;

import ca.teamdman.sfm.common.util.NotStored;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class SFMModCompat {
    public static boolean isMekanismLoaded() {
        return isModLoaded("mekanism");
    }

    public static boolean isAE2Loaded() {
        return isModLoaded("ae2");
    }

    public static boolean isModLoaded(String modid) {
        return Loader.isModLoaded(modid);
    }

    public static boolean isMekanismBlock(
            World level,
            @NotStored BlockPos pos
    ) {
        Block block = level.getBlockState(pos).getBlock();
        ResourceLocation blockId = Block.REGISTRY.getNameForObject(block);
        assert blockId != null;
        return blockId.getNamespace().equals("mekanism");
    }
}
