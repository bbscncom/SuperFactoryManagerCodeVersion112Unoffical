package ca.teamdman.sfm.client.diagnostics;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.text.SimpleDateFormat;
import java.util.List;

public class SFMClientDiagnostics {
    public static String getDiagnosticsSummary(
            ItemStack diskStack
    ) {
        StringBuilder content = new StringBuilder();
        try {
            content
                    .append("-- Diagnostic info --\n");

            content.append("-- Program:\n")
                    .append(DiskItem.getProgram(diskStack))
                    .append("\n\n");

            content.append("-- DateTime: ")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm.ss").format(new java.util.Date()))
                    .append('\n');

            content
                    .append("-- Game Version: ")
                    .append("Minecraft ")
                    .append(Minecraft.getMinecraft().getVersion())
                    .append(" (")
                    .append(Minecraft.getMinecraft().getVersion())
                    .append("/")
                    .append(ClientBrandRetriever.getClientModName())
                    .append(")")
                    .append('\n');
//            FMLCommonHandler.instance().getModName()
            content.append("-- Forge Version: ")
                    .append(Loader.instance().getIndexedModList().get("forge").getVersion())
                    .append('\n');

            content.append("-- SFM Version: ")
                    .append(Loader.instance().getIndexedModList().get(SFM.MOD_ID).getVersion())
                    .append('\n');

            List<TextComponentTranslation> errors = DiskItem.getErrors(diskStack);
            if (!errors.isEmpty()) {
                content.append("\n-- Errors\n");
                for (ITextComponent error : errors) {
                    content.append("-- * ").append(error.getUnformattedText()).append("\n");
                }
            }

            List<TextComponentTranslation> warnings = DiskItem.getWarnings(diskStack);
            if (!warnings.isEmpty()) {
                content.append("\n-- Warnings\n");
                for (ITextComponent warning : warnings) {
                    content.append("-- * ").append(warning.getUnformattedText()).append("\n");
                }
            }

            LabelPositionHolder labels = LabelPositionHolder.from(diskStack);
            content.append("\n-- Labels\n").append(labels.toDebugString());
        } catch (Throwable t) {
            SFM.LOGGER.error("Failed gathering diagnostic info, returning partial results. Error: ", t);
        }
        return content.toString();
    }
}
