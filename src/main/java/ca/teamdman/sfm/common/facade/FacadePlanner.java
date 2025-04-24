package ca.teamdman.sfm.common.facade;

import ca.teamdman.sfm.common.block.IFacadableBlock;
import ca.teamdman.sfm.common.blockentity.IFacadeTileEntity;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.net.ServerboundFacadePacket;
import ca.teamdman.sfm.common.util.ObjectUtils;
import ca.teamdman.sfm.common.util.SFMStreamUtils;
import ca.teamdman.sfm.common.util.Stored;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FacadePlanner {
    public static @Nullable IFacadePlan getFacadePlan(
            EntityPlayer player,
            World world,
            ServerboundFacadePacket msg
    ) {
        // preconditions
        BlockPos hitPos = msg.hitResult().getBlockPos();
        if (!world.isBlockLoaded(hitPos)) return null;
        IBlockState hitBlockState = world.getBlockState(hitPos);
        Block hitBlock = hitBlockState.getBlock();
        if (!(hitBlock instanceof IFacadableBlock)) return null;
        IFacadableBlock hitFacadable = (IFacadableBlock) hitBlock;
        Item paintItem = msg.paintStack().getItem();

        boolean paintingWithAir = paintItem == Items.AIR;
        if (paintingWithAir) {
            return new ClearFacadesFacadePlan(
                    getPositions(world, msg, hitPos, hitBlock)
            );
        }

        @Nullable Block renderBlock = Block.getBlockFromItem(paintItem);
        if (renderBlock == Blocks.AIR) return null;

        if (renderBlock instanceof IFacadableBlock) {
            IFacadableBlock renderFacadable = (IFacadableBlock) renderBlock;
            boolean isSameShape = hitFacadable.getNonFacadeBlock() == renderFacadable.getNonFacadeBlock();
            if (isSameShape) {
                // Clear facades
                return new ClearFacadesFacadePlan(
                        getPositions(world, msg, hitPos, hitBlock)
                );
            } else {
                // Change facade type
                return new ChangeWorldBlockFacadePlan(
                        renderFacadable.getFacadeBlock(),
                        getPositions(world, msg, hitPos, hitBlock)
                );
            }
        }

        IBlockState renderBlockState = ObjectUtils.requireNonNullElse(
                renderBlock.getStateForPlacement(
                        world,
                        msg.hitResult().getBlockPos(),
                        msg.hitResult().sideHit,
                        0.5f, 0.5f, 0.5f,
                        msg.paintStack().getMetadata(),
                        player,
                        msg.paintHand()
                ),
                renderBlock.getDefaultState()
        );
        FacadeTransparency facadeTransparency = renderBlockState.isOpaqueCube() ? FacadeTransparency.OPAQUE : FacadeTransparency.TRANSLUCENT;
        return new ApplyFacadesFacadePlan(
                new FacadeData(
                        renderBlockState,
                        msg.hitResult().sideHit,
                        FacadeTextureMode.FILL
                ),
                facadeTransparency,
                getPositions(world, msg, hitPos, hitBlock)
        );
    }

    private static @NotNull Set<BlockPos> getPositions(
            World world,
            ServerboundFacadePacket msg,
            @Stored BlockPos hitPos,
            Block hitBlock
    ) {
        Set<BlockPos> positions = new HashSet<>();
        switch (msg.spreadLogic()) {
            case SINGLE:
                positions.add(hitPos);
                break;
            case NETWORK:
                positions = CableNetwork.discoverCables(world, hitPos).collect(Collectors.toSet());
                break;
            case NETWORK_GLOBAL_SAME_PAINT:
                if (world.getTileEntity(hitPos) instanceof IFacadeTileEntity) {
                    IFacadeTileEntity startFacadeBlockEntity = (IFacadeTileEntity) world.getTileEntity(hitPos);
                    FacadeData existingFacadeData = startFacadeBlockEntity.getFacadeData();
                    Class<?> existingFacadeBlockEntityClass = startFacadeBlockEntity.getClass();
                    positions = CableNetwork.discoverCables(world, hitPos)
                            .filter(cablePos -> {
                                if (world.getTileEntity(cablePos) instanceof IFacadeTileEntity
                                        && world.getTileEntity(cablePos).getClass().equals(existingFacadeBlockEntityClass)) {
                                    IFacadeTileEntity otherFacadeBlockEntity = (IFacadeTileEntity) world.getTileEntity(cablePos);
                                    return Objects.equals(otherFacadeBlockEntity.getFacadeData(), existingFacadeData);
                                }
                                return false;
                            }).collect(Collectors.toSet());
                } else {
                    positions = CableNetwork.discoverCables(world, hitPos)
                            .filter(checkPos -> world.getBlockState(checkPos).getBlock() == hitBlock)
                            .filter(checkPos -> !(world.getTileEntity(checkPos) instanceof IFacadeTileEntity))
                            .collect(Collectors.toSet());
                }
                break;
            case NETWORK_CONTIGUOUS_SAME_PAINT:
                Set<BlockPos> cablePositions = CableNetwork.discoverCables(world, hitPos).collect(Collectors.toSet());
                if (world.getTileEntity(hitPos) instanceof IFacadeTileEntity) {
                    IFacadeTileEntity startFacadeBlockEntity = (IFacadeTileEntity) world.getTileEntity(hitPos);
                    Class<?> existingFacadeBlockEntityClass = startFacadeBlockEntity.getClass();
                    FacadeData existingFacadeData = startFacadeBlockEntity.getFacadeData();
                    positions = SFMStreamUtils.<BlockPos, BlockPos>getRecursiveStream(
                            (current, next, results) -> {
                                results.accept(current);
                                SFMStreamUtils.get3DNeighboursIncludingKittyCorner(current)
                                        .filter(neighbour -> {
                                            if (!cablePositions.contains(neighbour)) {
                                                return false;
                                            }
                                            if (world.getTileEntity(neighbour) instanceof IFacadeTileEntity
                                                    && world.getTileEntity(neighbour).getClass().equals(existingFacadeBlockEntityClass)) {
                                                IFacadeTileEntity otherCableFacadeBlockEntity = (IFacadeTileEntity) world.getTileEntity(neighbour);
                                                return Objects.equals(otherCableFacadeBlockEntity.getFacadeData(), existingFacadeData);
                                            }
                                            return false;
                                        })
                                        .forEach(next);
                            },
                            hitPos
                    ).collect(Collectors.toSet());
                } else {
                    positions = SFMStreamUtils.<BlockPos, BlockPos>getRecursiveStream(
                            (current, next, results) -> {
                                results.accept(current);
                                SFMStreamUtils.get3DNeighboursIncludingKittyCorner(current)
                                        .filter(neighbour -> {
                                            if (!cablePositions.contains(neighbour)) {
                                                return false;
                                            }
                                            Block neighbourBlock = world.getBlockState(neighbour).getBlock();
                                            return neighbourBlock == hitBlock;
                                        })
                                        .forEach(next);
                            },
                            hitPos
                    ).collect(Collectors.toSet());
                }
                break;
        }
        positions.removeIf(pos -> world.getTileEntity(pos) instanceof ManagerBlockEntity);
        return positions;
    }
}
