package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.item.LabelGunItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SFMItems {

    public static ItemBlock MANAGER_ITEM;
    public static ItemBlock CABLE_ITEM;
    public static Item DISK_ITEM;
    public static Item LABEL_GUN_ITEM;

    public static void init() {
        MANAGER_ITEM = register("manager", SFMBlocks.MANAGER_BLOCK);
        CABLE_ITEM = register("cable", SFMBlocks.CABLE_BLOCK);
        DISK_ITEM = register("disk", new DiskItem());
        LABEL_GUN_ITEM = register("labelgun", new LabelGunItem());
        registerItemModels();
    }

    private static <T> T register(String name, Object obj) {
        if (obj instanceof Block) {
            ItemBlock itemBlock = new ItemBlock((Block) obj);
            itemBlock.setRegistryName(SFM.MOD_ID, name);
            itemBlock.setTranslationKey("sfm." + name);
            ForgeRegistries.ITEMS.register(itemBlock);
            return (T) itemBlock;
        } else if (obj instanceof Item) {
            Item item = (Item) obj;
            item.setRegistryName(SFM.MOD_ID, name);
            item.setTranslationKey("sfm." + name);
            ForgeRegistries.ITEMS.register(item);
            return (T) item;
        }
        throw new IllegalArgumentException("不支持的对象类型: " + obj.getClass().getName());
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        ModelLoader.setCustomModelResourceLocation(
                MANAGER_ITEM,    // 你的物品对象
                0,                     // metadata
                new ModelResourceLocation(SFM.MOD_ID+":manager", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                CABLE_ITEM,    // 你的物品对象
                0,                     // metadata
                new ModelResourceLocation(SFM.MOD_ID+":cable", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                LABEL_GUN_ITEM,    // 你的物品对象
                0,                     // metadata
                new ModelResourceLocation(SFM.MOD_ID+":labelgun", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                DISK_ITEM,    // 你的物品对象
                0,                     // metadata
                new ModelResourceLocation(SFM.MOD_ID+":disk", "inventory")
        );
    }
}
