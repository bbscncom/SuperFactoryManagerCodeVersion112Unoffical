package ca.teamdman.sfm.client.export;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.registry.SFMResourceTypes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class ClientExportHelper {

    private static final Object registryReaderLock = new Object();

    public static Collection<ItemStack> gatherItems() {
        assert Minecraft.getMinecraft().player != null;
        assert Minecraft.getMinecraft().world != null;
        NonNullList<ItemStack> list = NonNullList.create();
        CreativeTabs.SEARCH.displayAllRelevantItems(list);
        return list;
    }

    // https://github.com/TeamDman/tell-me-my-items/blob/6fb767f0145abebff503b87a10a1810ca24580b9/mod/src/main/java/ca/teamdman/tellmemyitems/TellMeMyItems.java#L36
    public static void dumpItems(@Nullable EntityPlayer player) throws IOException {
        // manually build JSON array
        JsonArray jsonArray = new JsonArray();

        Collection<ItemStack> items = gatherItems();
        for (ItemStack stack : items) {
            JsonObject jsonObject = new JsonObject();

            // Add the id field
            // todo item.getRegistryName 不知道是否是注册id sfm:manager_blcok 这些
            ResourceLocation id = stack.getItem().getRegistryName();
            jsonObject.addProperty("id", id.toString());

            // Add the data field if it exists
            // TODO: NBT here
//            if (stack.getShareTag() != null) {
//                jsonObject.addProperty("data", stack.getShareTag().toString());
//            }

            // Add the tags
            JsonArray tags = new JsonArray();
            SFMResourceTypes.ITEM.get().getTagsForStack(stack).map(ResourceLocation::toString).forEach(tags::add);
            jsonObject.add("tags", tags);

            // Add the tooltip field
            String tooltip = stack
                    .getTooltip(player, ITooltipFlag.TooltipFlags.ADVANCED)
                    .stream()
                    .reduce((line1, line2) -> line1 + "\n" + line2)
                    .orElse("");
            jsonObject.addProperty("tooltip", tooltip);

            jsonArray.add(jsonObject);
        }

        // serialize to JSON with pretty printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String content = gson.toJson(jsonArray);

        // ensure folder exists
        File gameDir = Minecraft.getMinecraft().gameDir;
        Path folder = Paths.get(gameDir.toString(), SFM.MOD_ID);
        Files.createDirectories(folder);

        // write to file
        File itemFile = new File(folder.toFile(), "items.json");
        try (FileOutputStream str = new FileOutputStream(itemFile)) {
            str.write(content.getBytes(StandardCharsets.UTF_8));
        }
        SFM.LOGGER.info("Exported item data to {}", itemFile);
        assert Minecraft.getMinecraft().player != null;
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format(
                "Exported %d items to \"%s\"",
                items.size(),
                itemFile.getAbsolutePath()
        )));
    }
}
