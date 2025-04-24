package ca.teamdman.sfm.common.diagnostics;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.diagnostics.SFMClientDiagnostics;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.util.SFMEnvironmentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.text.SimpleDateFormat;
import java.util.List;

public class SFMDiagnostics {
    public static String getDiagnosticsSummary(
            ItemStack diskStack
    ) {
        if (SFMEnvironmentUtils.isClient()) {
            return SFMClientDiagnostics.getDiagnosticsSummary(diskStack);
        }

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
                    .append('\n');

            content.append("-- Forge Version: ")
                    .append(ForgeVersion.getVersion())
                    .append('\n');

            //noinspection CodeBlock2Expr
            ModContainer modContainer = Loader.instance().getIndexedModList().get(SFM.MOD_ID);
            if(modContainer!=null){
                content.append("-- SFM Version: ")
                        .append(modContainer.getVersion())
                        .append('\n');
            }

            List<TextComponentTranslation> errors = DiskItem.getErrors(diskStack);
            if (!errors.isEmpty()) {
                content.append("\n-- Errors\n");
                for (ITextComponent error : errors) {
                    content.append("-- * ").append(error.getFormattedText()).append("\n");
                }
            }

            List<TextComponentTranslation> warnings = DiskItem.getWarnings(diskStack);
            if (!warnings.isEmpty()) {
                content.append("\n-- Warnings\n");
                for (TextComponentTranslation warning : warnings) {
                    content.append("-- * ").append(warning.getFormattedText()).append("\n");
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
