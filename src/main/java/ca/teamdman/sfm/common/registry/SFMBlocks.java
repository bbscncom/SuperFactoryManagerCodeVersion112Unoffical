package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.block.CableBlock;
import ca.teamdman.sfm.common.block.CableFacadeBlock;
import ca.teamdman.sfm.common.block.ManagerBlock;
import ca.teamdman.sfm.common.blockentity.CableFacadeBlockEntity;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import com.example.sfmbbs.Tags;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashSet;
import java.util.Set;

public class SFMBlocks {
//    @GameRegistry.ObjectHolder(SFM.MOD_ID + ":manager")
    public static ManagerBlock MANAGER_BLOCK = null;


//    @GameRegistry.ObjectHolder(SFM.MOD_ID + ":cable")
    public static  CableBlock CABLE_BLOCK = null;

//    @GameRegistry.ObjectHolder(SFM.MOD_ID + ":cable_facade")
    public static  CableFacadeBlock CABLE_FACADE_BLOCK = null;


    public static Set<Block> getBlocks() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(MANAGER_BLOCK);
        blocks.add(CABLE_BLOCK);
        blocks.add(CABLE_FACADE_BLOCK);
        return blocks;
    }


    public static void register() {
        ManagerBlock manager = (ManagerBlock) new ManagerBlock().setRegistryName(SFM.MOD_ID, "manager");
        CableBlock cable = (CableBlock) new CableBlock().setRegistryName(SFM.MOD_ID, "cable");
        CableFacadeBlock cableFacade = (CableFacadeBlock) new CableFacadeBlock().setRegistryName(SFM.MOD_ID, "cable_facade");
        MANAGER_BLOCK=manager;
        CABLE_BLOCK=cable;
        CABLE_FACADE_BLOCK=cableFacade;
        ForgeRegistries.BLOCKS.register(manager);
        ForgeRegistries.BLOCKS.register(cable);
        ForgeRegistries.BLOCKS.register(cableFacade);
        GameRegistry.registerTileEntity(ManagerBlockEntity.class,new ResourceLocation(Tags.MOD_ID, "manager_block"));
        GameRegistry.registerTileEntity(CableFacadeBlockEntity.class,new ResourceLocation(Tags.MOD_ID, "cable_facade_block"));
    }
}
