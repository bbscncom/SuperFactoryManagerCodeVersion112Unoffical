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

public class UseOnContext {
    @Nullable
    private final EntityPlayer player;
    private final EnumHand hand;
    private final RayTraceResult hitResult;
    private final World world;
    private final ItemStack itemStack;

    public UseOnContext(EntityPlayer pPlayer, EnumHand pHand, RayTraceResult pHitResult) {
        this(pPlayer.world, pPlayer, pHand, pPlayer.getHeldItem(pHand), pHitResult);
    }

    public UseOnContext(World pLevel, @Nullable EntityPlayer pPlayer, EnumHand pHand, ItemStack pItemStack, RayTraceResult pHitResult) {
        this.player = pPlayer;
        this.hand = pHand;
        this.hitResult = pHitResult;
        this.itemStack = pItemStack;
        this.world = pLevel;
    }

    protected final RayTraceResult getHitResult() {
        return this.hitResult;
    }

    public BlockPos getClickedPos() {
        return this.hitResult.getBlockPos();
    }

    public EnumFacing getClickedFace() {
        return this.hitResult.sideHit;
    }

    public Vec3d getClickLocation() {
        return this.hitResult.hitVec;
    }

//    public boolean isInside() {
//        return this.hitResult.isInside();
//    }

    public ItemStack getItemInHand() {
        return this.itemStack;
    }

    @Nullable
    public EntityPlayer getPlayer() {
        return this.player;
    }

    public EnumHand getHand() {
        return this.hand;
    }

    public World getWorld() {
        return this.world;
    }

    public EnumFacing getHorizontalDirection() {
        return this.player == null ? EnumFacing.NORTH : this.player.getHorizontalFacing();
    }

//    public boolean isSecondaryUseActive() {
//        return this.player != null && this.player.isSecondaryUseActive();
//    }
//
//    public float getRotation() {
//        return this.player == null ? 0.0F : this.player.getYRot();
//    }
}
