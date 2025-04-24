package ca.teamdman.sfm.common.blockentity;

import ca.teamdman.sfm.common.facade.FacadeData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.Nullable;

public abstract class CommonFacadeBlockEntity extends TileEntity implements IFacadeTileEntity {
    protected @Nullable FacadeData facadeData = null;
    public IBlockState blockState;

    private IBlockState modelState;
    public CommonFacadeBlockEntity(
            IBlockState state
    ) {
        this.blockState=state;
    }
    public IBlockState getModelState() {
        return modelState;
    }

    public void setModelState(IBlockState state) {
        this.modelState = state;
    }

    @Override
    public @Nullable FacadeData getFacadeData() {
        return facadeData;
    }

    @Override
    public void updateFacadeData(
            FacadeData newFacadeData
    ) {
        if (newFacadeData.equals(facadeData)) return;
        this.facadeData = newFacadeData;
        markDirty();
        if (world != null) {
            world.notifyBlockUpdate(pos,blockState,blockState,3);
        }
    }


    @Override
    public @Nullable SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
//        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
    }

    public NBTTagCompound getUpdateTag() {
        NBTTagCompound pTag = new NBTTagCompound();
        writeToNBT(pTag);
        return pTag;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
        if (facadeData != null) {
            facadeData.save(nbtTagCompound);
        }
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        FacadeData tried = FacadeData.load(world, compound);
        if (tried != null) {
            this.facadeData = tried;
            world.markBlockRangeForRenderUpdate(pos,pos);
        }
    }

}
