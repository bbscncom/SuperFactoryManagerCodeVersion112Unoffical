package ca.teamdman.sfm.client.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.render.CableFacadeBlockModelWrapper;
import ca.teamdman.sfm.common.registry.SFMBlocks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;


@Mod.EventBusSubscriber(modid = SFM.MOD_ID, value = Side.CLIENT)
public class SFMBlockModelWrappers {
    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
        for (ModelResourceLocation location : event.getModelRegistry().getKeys()) {
            ResourceLocation registryName = SFMBlocks.CABLE_FACADE_BLOCK.getRegistryName();
            //todo 这里registryName不知道何时赋值，null报错,也不知道这里有设么用
            if(registryName==null)return;
            if (location.getPath().equals(registryName.getPath())) {
                event.getModelRegistry().putObject(location, new CableFacadeBlockModelWrapper(event.getModelRegistry().getObject(location)));
            }
//            if (location.getPath().equals(SFMBlocks.FANCY_CABLE_FACADE_BLOCK.getRegistryName().getResourcePath())) {
//                event.getModelRegistry().putObject(location, new FancyCableFacadeBlockModelWrapper(event.getModelRegistry().getObject(location)));
//            }
        }
    }
}