package ca.teamdman.sfm.common.block;

import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import ca.teamdman.sfm.common.facade.FacadeTransparency;
import ca.teamdman.sfm.common.registry.SFMBlocks;
import ca.teamdman.sfm.common.registry.SFMCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CableBlock extends Block implements ICableBlock,IFacadableBlock {
    public CableBlock() {
        super(Material.IRON);
        setHardness(1.0F);
        setSoundType(SoundType.METAL);
        this.setCreativeTab(SFMCreativeTabs.SFMTAB);
        this.setTranslationKey("sfm.cable");
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockAdded(worldIn, pos, state);

            CableNetworkManager.onCablePlaced(worldIn, pos);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);

        if (state.getBlock() instanceof ICableBlock) {
            CableNetworkManager.onCableRemoved(world, pos);
        }
    }

    //todo NETWORK_TOOL_ITEM被我删了，大概这里可以删掉
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

//        if (player.getHeldItemOffhand().getItem() == SFMItems.NETWORK_TOOL_ITEM.get()) {
//            if (pLevel.isClientSide() && pHand == EnumHand.MAIN_HAND) {
//                ServerboundFacadePacket msg = new ServerboundFacadePacket(
//                        pHitResult,
//                        FacadeSpreadLogic.fromParts(Screen.hasControlDown(), Screen.hasAltDown()),
//                        pPlayer.getMainHandItem(),
//                        EnumHand.MAIN_HAND
//                );
//                if (ClientKeyHelpers.isKeyDownInWorld(SFMKeyMappings.TOGGLE_NETWORK_TOOL_OVERLAY_KEY)) {
//                    // we don't want to toggle the overlay if we're using alt-click behaviour
//                    NetworkToolKeyMappingHandler.setExternalDebounce();
//                }
//                ClientFacadeWarningHelper.sendFacadePacketFromClientWithConfirmationIfNecessary(msg);
//                return ItemInteractionResult.CONSUME;
//            }
//            return ItemInteractionResult.SUCCESS;
//        }
//        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return false;
    }

    @Override
    public IFacadableBlock getNonFacadeBlock() {
       return  SFMBlocks.CABLE_BLOCK;
    }

    @Override
    public IFacadableBlock getFacadeBlock() {
        return SFMBlocks.CABLE_FACADE_BLOCK;
    }

    @Override
    public IBlockState getStateForPlacementByFacadePlan(World world, BlockPos pos, @Nullable FacadeTransparency facadeTransparency) {
        return getDefaultState();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return super.getRenderLayer();
    }
}
