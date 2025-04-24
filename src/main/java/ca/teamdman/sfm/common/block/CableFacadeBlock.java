package ca.teamdman.sfm.common.block;

import ca.teamdman.sfm.common.blockentity.CableFacadeBlockEntity;
import ca.teamdman.sfm.common.facade.FacadeTransparency;
import ca.teamdman.sfm.common.registry.SFMCreativeTabs;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CableFacadeBlock extends CableBlock implements IFacadableBlock {
    public CableFacadeBlock() {
        super();
//        todo
        this.setCreativeTab(SFMCreativeTabs.SFMTAB);
        this.setDefaultState(this.blockState.getBaseState().withProperty(
                FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY, FacadeTransparency.OPAQUE));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new CableFacadeBlockEntity(state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state.getProperties().get(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY) ==FacadeTransparency.TRANSLUCENT;
    }
// propagatesSkylightDown
    @Override
    public boolean isFullBlock(IBlockState state) {
        return state.getProperties().get(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY) !=FacadeTransparency.TRANSLUCENT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] properties = {FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY};
        return new BlockStateContainer(this, properties);
    }

    @Override
    public IBlockState getStateForPlacementByFacadePlan(
            World world,
            BlockPos pos,
            @Nullable FacadeTransparency facadeTransparency
    ) {
        IBlockState blockState = super.getStateForPlacementByFacadePlan(world, pos, facadeTransparency);
        if (facadeTransparency == null) {
            return blockState;
        }
        return blockState.withProperty(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY, facadeTransparency);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        FacadeTransparency transparency = state.getValue(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY);
        return transparency.ordinal(); // OPAQUE = 0, TRANSLUCENT = 1
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        FacadeTransparency transparency = FacadeTransparency.values()[meta % FacadeTransparency.values().length];
        return this.getDefaultState().withProperty(FacadeTransparency.FACADE_TRANSPARENCY_PROPERTY, transparency);
    }
}
