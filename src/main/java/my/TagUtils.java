package my;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.stream.Stream;

public class TagUtils {
    // 添加静态缓存
    private static final Map<Item, Set<String>> ITEM_TO_ORES_CACHE = new HashMap<>();

    // 初始化缓存的方法
    public static void initOreDictionaryCache() {
        if(!ITEM_TO_ORES_CACHE.isEmpty())return;
        for (String oreName : OreDictionary.getOreNames()) {
            for (ItemStack stack : OreDictionary.getOres(oreName)) {
                Item item = stack.getItem();
                ITEM_TO_ORES_CACHE.computeIfAbsent(item, k -> new HashSet<>()).add(oreName);
            }
        }
    }

    public static Stream<ResourceLocation> getTagsForStack(ItemStack itemStack) {
        initOreDictionaryCache();
        if (itemStack.isEmpty()) {
            return Stream.empty();
        }

        // 1.12 版本使用缓存查询
        Set<String> oreNames = ITEM_TO_ORES_CACHE.getOrDefault(itemStack.getItem(), Collections.emptySet());
        Stream<ResourceLocation> itemTagKeys = oreNames.stream().map(ResourceLocation::new);

        // 获取方块标签（如果有）
        Stream<ResourceLocation> blockTagKeys = Stream.empty();
        Block block = Block.getBlockFromItem(itemStack.getItem());
        if (block != null && block != Blocks.AIR) {
            // 如果有需要，也可以为方块添加类似的缓存机制
            //todo 是否block的tag是以block开始的
            blockTagKeys = oreNames.stream()
                    .filter(name -> name.startsWith("block"))
                    .map(ResourceLocation::new);
        }

        return Stream.concat(itemTagKeys, blockTagKeys);
    }
}
