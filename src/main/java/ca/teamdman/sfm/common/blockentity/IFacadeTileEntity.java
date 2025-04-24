package ca.teamdman.sfm.common.blockentity;

import ca.teamdman.sfm.client.render.BlockStateWrapper;
import ca.teamdman.sfm.client.render.ModelProperty;
import ca.teamdman.sfm.common.facade.FacadeData;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

public interface IFacadeTileEntity {
    ModelProperty FACADE_BLOCK_STATE_MODEL_PROPERTY=new ModelProperty("FACADE_BLOCK_STATE_MODEL_PROPERTY", BlockStateWrapper.class);


    void updateFacadeData(FacadeData newFacadeData);


     IBlockState getModelState();

     void setModelState(IBlockState state);

     @Nullable FacadeData getFacadeData() ;
}
