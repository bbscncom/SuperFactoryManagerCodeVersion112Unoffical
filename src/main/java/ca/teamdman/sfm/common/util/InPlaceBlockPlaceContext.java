package ca.teamdman.sfm.common.util;

import my.BlockPlaceContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class InPlaceBlockPlaceContext extends BlockPlaceContext {
    public InPlaceBlockPlaceContext(
            EntityPlayer player,
            EnumHand hand,
            ItemStack itemStack,
            RayTraceResult hitResult
    ) {
        super(player.world, player, hand, itemStack, hitResult);
        this.replaceClicked = true;
    }

    public InPlaceBlockPlaceContext(World world, @Nullable EntityPlayer player, EnumHand hand, ItemStack itemStack, RayTraceResult hitResult) {
        super(world, player, hand, itemStack, hitResult);
        this.replaceClicked = true;
    }
}
