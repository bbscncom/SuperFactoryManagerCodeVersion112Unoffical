package ca.teamdman.sfm.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CableFacadeBlockModelWrapper extends BakedModelWrapper<IBakedModel> {


    public CableFacadeBlockModelWrapper(IBakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        Minecraft minecraft = Minecraft.getMinecraft();
        BlockStateWrapper mimicState = (BlockStateWrapper) state.getValue(ModelProperty.modelProperty);
        if (mimicState != null) {
            IBakedModel mimicModel = minecraft.getBlockRendererDispatcher().getModelForState(mimicState.blockState);
            return mimicModel.getQuads(mimicState.blockState, side, rand);
        }
        IBakedModel missingModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getMissingModel();
        return  missingModel.getQuads(state, side, rand);
    }

    //todo 不知道是干嘛的，可能是工具绑定的透明方块的渲染相关？有问题再改
//    @SuppressWarnings("DuplicatedCode")
//    @Override
//    public @NotNull ChunkRenderTypeSet getRenderTypes(
//            @NotNull BlockState cableBlockState,
//            @NotNull RandomSource rand,
//            @NotNull ModelData data
//    ) {
//        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//        BlockState paintBlockState = data.get(IFacadeTileEntity.FACADE_BLOCK_STATE_MODEL_PROPERTY);
//        if (paintBlockState == null) {
//            return cableBlockState.getValue(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY) == FacadeTransparency.TRANSLUCENT ? ALL : SOLID;
//        }
//        BakedModel bakedModel = blockRenderer.getBlockModel(paintBlockState);
//        return bakedModel.getRenderTypes(paintBlockState, rand, ModelData.EMPTY);
//    }

}
