package ca.teamdman.sfm.common.util;

import ca.teamdman.sfm.client.ClientKeyHelpers;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SFMItemUtils {
    public static void appendMoreInfoKeyReminderTextIfOnClient(List<ITextComponent> lines) {
        if (SFMEnvironmentUtils.isClient()) {
            lines.add(
                    LocalizationKeys.GUI_ADVANCED_TOOLTIP_HINT.getComponent(
                                new TextComponentString(Keyboard.getKeyName(SFMKeyMappings.MORE_INFO_TOOLTIP_KEY.getKeyCode()))
                                        .setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText()
                            )
            );
        }
    }

    public static boolean isClientAndMoreInfoKeyPressed() {
        return SFMEnvironmentUtils.isClient() && ClientKeyHelpers.isKeyDownInScreenOrWorld(SFMKeyMappings.MORE_INFO_TOOLTIP_KEY);
    }

    public static ITextComponent getRainbow(int length) {
//         start = Component.empty();
//        ChatFormatting[] rainbowColors = new ChatFormatting[]{
//                ChatFormatting.DARK_RED,
//                ChatFormatting.RED,
//                ChatFormatting.GOLD,
//                ChatFormatting.YELLOW,
//                ChatFormatting.DARK_GREEN,
//                ChatFormatting.GREEN,
//                ChatFormatting.DARK_AQUA,
//                ChatFormatting.AQUA,
//                ChatFormatting.DARK_BLUE,
//                ChatFormatting.BLUE,
//                ChatFormatting.DARK_PURPLE,
//                ChatFormatting.LIGHT_PURPLE
//        };
//        int rainbowColorsLength = rainbowColors.length;
//        int fullCycleLength = 2 * rainbowColorsLength - 2;
//        for (int i = 0; i < length - 2; i++) {
//            int cyclePosition = i % fullCycleLength;
//            int adjustedIndex = cyclePosition < rainbowColorsLength
//                                ? cyclePosition
//                                : fullCycleLength - cyclePosition;
//            ChatFormatting color = rainbowColors[adjustedIndex];
//            start = start.append(new TextComponentString("=").withStyle(color));
//        }
//        return start;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append("=");
        }
        return new TextComponentString(new String(builder));
    }

    @MCVersionDependentBehaviour
    public static boolean isSameItem(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a,b);
    }
    public static boolean isSameItemSameTags(ItemStack a, ItemStack b) {
        return isSameItem(a, b) && isSameItemSameTags(a, b);
    }
    public static boolean isSameItemSameAmount(ItemStack a, ItemStack b) {
        return isSameItem(a,b) && a.getCount() == b.getCount();
    }
}
