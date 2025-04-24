package my;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPlaceContext extends UseOnContext {
    private final BlockPos relativePos;
    protected boolean replaceClicked = true;

    public BlockPlaceContext(EntityPlayer player, EnumHand hand, ItemStack itemStack, RayTraceResult hitResult) {
        this(player.world, player, hand, itemStack, hitResult);
    }

    public BlockPlaceContext(UseOnContext pContext) {
        this(pContext.getWorld(), pContext.getPlayer(), pContext.getHand(), pContext.getItemInHand(), pContext.getHitResult());
    }

    public BlockPlaceContext(World pLevel, @Nullable EntityPlayer pPlayer, EnumHand pHand, ItemStack pItemStack, RayTraceResult pHitResult) {
        super(pLevel, pPlayer, pHand, pItemStack, pHitResult);
        EnumFacing sideHit = pHitResult.sideHit;
        BlockPos blockPos = pHitResult.getBlockPos();
        this.relativePos=new BlockPos(blockPos.getX() + sideHit.getXOffset(), blockPos.getY() + sideHit.getYOffset(), blockPos.getZ() + sideHit.getZOffset());
        this.replaceClicked = pLevel.getBlockState(pHitResult.getBlockPos()).getBlock().isReplaceable(this.getWorld(),this.getClickedPos());
    }

    public static BlockPlaceContext at(BlockPlaceContext pContext, BlockPos pPos, EnumFacing pDirection) {
        return new BlockPlaceContext(
            pContext.getWorld(),
            pContext.getPlayer(),
            pContext.getHand(),
            pContext.getItemInHand(),
            new RayTraceResult(
                    RayTraceResult.Type.BLOCK,
                new Vec3d(
                    (double)pPos.getX() + 0.5 + (double)pDirection.getXOffset() * 0.5,
                    (double)pPos.getY() + 0.5 + (double)pDirection.getYOffset() * 0.5,
                    (double)pPos.getZ() + 0.5 + (double)pDirection.getZOffset() * 0.5
                ),
                pDirection,
                pPos
            )
        );
    }

    @Override
    public BlockPos getClickedPos() {
        return this.replaceClicked ? super.getClickedPos() : this.relativePos;
    }

    public boolean canPlace() {
        return this.replaceClicked || this.getWorld().getBlockState(this.getClickedPos()).getBlock().isReplaceable(this.getWorld(), this.getClickedPos());
    }

    public boolean replacingClickedOnBlock() {
        return this.replaceClicked;
    }

//    public EnumFacing getNearestLookingDirection() {
//        return EnumFacing.getDirectionFromEntityLiving(this.getClickedPos(),this.getPlayer());
//    }
//
//    public EnumFacing getNearestLookingVerticalDirection() {
//        return EnumFacing.getFacingFromEntity(this.getPlayer(), EnumFacing.Axis.Y);
//    }

    public EnumFacing[] getNearestLookingDirections() {
        EnumFacing[] directions = EnumFacing.values();
        if (this.replaceClicked) {
            return directions;
        } else {
            EnumFacing face = this.getHitResult().sideHit;
            int i = 0;

            while (i < directions.length && directions[i] != face.getOpposite()) {
                i++;
            }

            if (i > 0) {
                System.arraycopy(directions, 0, directions, 1, i);
                directions[0] = face.getOpposite();
            }

            return directions;
        }
    }
}
