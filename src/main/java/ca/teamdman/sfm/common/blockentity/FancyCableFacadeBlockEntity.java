package ca.teamdman.sfm.common.blockentity;

import ca.teamdman.sfm.client.render.ModelProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class FancyCableFacadeBlockEntity extends CommonFacadeBlockEntity {
    public static final ModelProperty FACADE_DIRECTION = new ModelProperty("FACADE_DIRECTION",null);

    public FancyCableFacadeBlockEntity(
            BlockPos pos,
            IBlockState state
    ) {
        super(state);
    }

//    @Override
//    public IModelState getModelData() {
//        if (getFacadeData() != null) {
//            return IModelState.builder()
//                    .with(IFacadeTileEntity.FACADE_BLOCK_STATE_MODEL_PROPERTY, getFacadeData().facadeBlockState())
//                    .with(FACADE_DIRECTION, getFacadeData().facadeDirection)
//                    .build();
//        }
//        return ModelData.EMPTY;
//    }

}
