package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.net.ServerboundManagerLogDesireUpdatePacket;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMPackets;
import my.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SFMScreenChangeHelpers {
    public static void setOrPushScreen(GuiScreen newScreen) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen current = mc.currentScreen;

        if (current == null) {
            mc.displayGuiScreen(newScreen);
        } else {
            // 模拟 pushGuiLayer：把当前 screen 存进 newScreen 中，等关闭时再返回
            if (newScreen instanceof IStackableScreen) {
                ((IStackableScreen)newScreen).setParent(current);
            }
            Minecraft.getMinecraft().displayGuiScreen(newScreen);
        }
    }

    public static void popScreen() {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen currentScreen = mc.currentScreen;
        if(currentScreen instanceof  IStackableScreen) {
            IStackableScreen stackableScreen = (IStackableScreen) currentScreen;
            if (stackableScreen.getParent() != null) {
                Minecraft.getMinecraft().displayGuiScreen(stackableScreen.getParent());
                return;
            }
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    public static void showLabelGunScreen(
            ItemStack stack,
            EnumHand hand
    ) {
        setOrPushScreen(new LabelGunScreen(stack, hand));
    }

    public static void showProgramEditScreen(
            ProgramEditScreenOpenContext context
    ) {
        ProgramEditorScreen screen = new ProgramEditorScreen(context);
        setOrPushScreen(screen);
        screen.scrollToTop();
    }

    public static void showTomlEditScreen(
            TomlEditScreenOpenContext context
    ) {
        ProgramEditorScreen screen = new TomlEditScreen(context);
        setOrPushScreen(screen);
        screen.scrollToTop();
    }

    public static void showProgramEditScreen(String initialContent) {
        ProgramEditScreenOpenContext openContext = new ProgramEditScreenOpenContext(
                initialContent,
                LabelPositionHolder.empty(),
                (x) -> {
                }
        );
        showProgramEditScreen(openContext);
    }

    public static void showExampleListScreen(
            String diskProgramString,
            LabelPositionHolder labelPositionHolder,
            Consumer<String> saveCallback
    ) {
        setOrPushScreen(new ExamplesScreen((chosenTemplate, templates) -> {
            ProgramEditorScreen screen = new ExampleEditScreen(new ExampleEditScreenOpenContext(
                    chosenTemplate,
                    diskProgramString,
                    templates,
                    labelPositionHolder,
                    saveCallback
            ));
            setOrPushScreen(screen);
            screen.scrollToTop();
        }));
    }

    public static void showLogsScreen(ManagerContainerMenu menu) {
        LogsScreen screen = new LogsScreen(menu);
        setOrPushScreen(screen);
        screen.scrollToBottom();
        SFMPackets.sendToServer(new ServerboundManagerLogDesireUpdatePacket(
                menu.windowId,
                menu.MANAGER_POSITION,
                true
        ));
    }

    // TODO: copy item id, not just NBT
    // TODO: replace with showing a screen with the data
    public static void showItemInspectorScreen(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && !tag.isEmpty()) {
            String content = tag.toString();
            Minecraft minecraft = Minecraft.getMinecraft();
            GuiScreen.setClipboardString(content);
            SFM.LOGGER.info("Copied {} characters to clipboard", content.length());
            if (minecraft.player != null) {
                minecraft.player.sendMessage(
                        LocalizationKeys.ITEM_INSPECTOR_COPIED_TO_CLIPBOARD.getComponent(
                                new TextComponentString(String.valueOf(content.length())).setStyle(new Style().setColor(TextFormatting.AQUA))
                        )
                );
            }
        }
    }

    public static void showChangelog() {
        String changelog = null;
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        List<ResourceLocation> files = Tools.listSfmlResources("sfm", "template_programs");
        List<ResourceLocation> collect = files.stream()
                .filter((rl) -> rl.getPath().endsWith(".sfml") || rl.getPath().endsWith(".sfm"))
                .collect(Collectors.toList());
        for (ResourceLocation resourceLocation : collect) {
            if (resourceLocation.getPath().equals("template_programs/changelog.sfml")) {
                try {
                    InputStream inputStream = resourceManager.getResource(resourceLocation).getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    changelog = bufferedReader.lines().collect(Collectors.joining("\n"));
                    break;
                } catch (Exception e) {
                    SFM.LOGGER.error("Failed to read changelog", e);
                }
            }
        }
        if (changelog == null) {
            SFM.LOGGER.error("Failed to find changelog");
            return;
        }
        ProgramEditorScreen screen = new ExampleEditScreen(new ExampleEditScreenOpenContext(
                changelog,
                changelog,
                Collections.singletonMap("changelog.sfml", changelog),
                LabelPositionHolder.empty(),
                newContent -> {
                }
        ));
        setOrPushScreen(screen);
        screen.scrollToTop();
    }
}
