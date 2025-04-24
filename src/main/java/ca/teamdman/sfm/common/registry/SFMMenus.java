package ca.teamdman.sfm.common.registry;


import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.gui.screen.ManagerScreen;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.Nullable;


public class SFMMenus implements IGuiHandler {
    public static SFMMenus handler = new SFMMenus();

    public static final int MANAGER_GUI_ID = 2000;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        switch (ID) {
            case MANAGER_GUI_ID:
                return new ManagerContainerMenu(ID,player, (ManagerBlockEntity) world.getTileEntity(pos),pos);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == MANAGER_GUI_ID) {
            return new ManagerScreen(
                    new ManagerContainerMenu(ID,player,
                    (ManagerBlockEntity) world.getTileEntity(new BlockPos(x,y,z)),
                    new BlockPos(x, y, z)));
        }
        return null;
    }
}
