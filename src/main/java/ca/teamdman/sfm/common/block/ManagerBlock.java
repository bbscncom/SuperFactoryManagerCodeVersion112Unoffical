package ca.teamdman.sfm.common.block;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.linting.ProgramLinter;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMCreativeTabs;
import ca.teamdman.sfm.common.registry.SFMMenus;
import ca.teamdman.sfml.ast.Program;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static net.minecraft.block.BlockDispenser.TRIGGERED;

public class ManagerBlock extends Block implements ICableBlock {
    public ManagerBlock(){
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TRIGGERED, false));
        this.setHardness(2.0F); // 相当于destroyTime(2)
        this.setSoundType(SoundType.METAL);
        this.setCreativeTab(SFMCreativeTabs.SFMTAB);
        this.setTranslationKey("sfm.manager");
    }



    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TRIGGERED);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new ManagerBlockEntity();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof ManagerBlockEntity)) return;
        if (!worldIn.isRemote) {
            boolean isPowered = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
            boolean debounce = state.getValue(TRIGGERED);
            if (isPowered && !debounce) {
                ((ManagerBlockEntity) te).trackRedstonePulseUnprocessed();
                worldIn.setBlockState(pos, state.withProperty(TRIGGERED, true), 4);
            } else if (!isPowered && debounce) {
                worldIn.setBlockState(pos, state.withProperty(TRIGGERED, false), 4);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        CableNetworkManager.onCablePlaced(world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof net.minecraft.inventory.IInventory) {
            net.minecraft.inventory.InventoryHelper.dropInventoryItems(world, pos,
                (net.minecraft.inventory.IInventory)te);
            world.updateComparatorOutputLevel(pos, this);
        }
        CableNetworkManager.onCableRemoved(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ManagerBlockEntity && player instanceof EntityPlayerMP) {
            ManagerBlockEntity manager = (ManagerBlockEntity) te;
            // update warnings on disk as we open the gui
            ItemStack disk = manager.getDisk();
            if (disk != null) {
                Program program = manager.getProgram();
                if (program != null) {
                    DiskItem.setWarnings(
                            disk,
                            ProgramLinter.gatherWarnings(program, LabelPositionHolder.from(disk), manager)
                    );
                }
            }
            if (!world.isRemote) { // 仅在服务端执行
                player.openGui(SFM.instance, SFMMenus.MANAGER_GUI_ID,player.world,pos.getX(),pos.getY(),pos.getZ());
            }
            return true;
        }
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TRIGGERED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TRIGGERED) ? 1 : 0;
    }
}
