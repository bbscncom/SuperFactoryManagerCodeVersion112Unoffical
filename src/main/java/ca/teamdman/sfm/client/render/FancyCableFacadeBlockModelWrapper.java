package ca.teamdman.sfm.client.render;

import ca.teamdman.sfm.common.blockentity.FancyCableFacadeBlockEntity;
import ca.teamdman.sfm.common.blockentity.IFacadeTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FancyCableFacadeBlockModelWrapper extends BakedModelWrapper<IBakedModel> {

    public FancyCableFacadeBlockModelWrapper(IBakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        Minecraft minecraft = Minecraft.getMinecraft();

        BlockStateWrapper mimicState = (BlockStateWrapper) state.getValue(IFacadeTileEntity.FACADE_BLOCK_STATE_MODEL_PROPERTY);
        EnumFacing mimicDirection = (EnumFacing) state.getValue(FancyCableFacadeBlockEntity.FACADE_DIRECTION);

        if (mimicState != null && side == null && mimicDirection != null) {
            List<BakedQuad> originalQuads = originalModel.getQuads(state, null, new Random(rand).nextLong());

            IBakedModel mimicModel = minecraft.getBlockRendererDispatcher().getModelForState((IBlockState) mimicState);
            List<BakedQuad> mimicQuads = mimicModel.getQuads((IBlockState) mimicState, mimicDirection, new Random(rand).nextLong());
            TextureAtlasSprite sprite = mimicQuads.isEmpty() ? null : mimicQuads.get(0).getSprite();

            if (sprite != null) {
                List<BakedQuad> resultQuads = new ArrayList<>(originalQuads.size());
                for (BakedQuad originalQuad : originalQuads) {
                    resultQuads.add(new RetexturedBakedQuad(
                            originalQuad,
                            sprite
                    ));
                }
                return resultQuads;
            }
        }
        return new ArrayList<>();
    }

}
