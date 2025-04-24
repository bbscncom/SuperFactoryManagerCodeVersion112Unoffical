package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.common.config.SFMServerConfig;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.registry.SFMResourceTypes;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import com.google.common.collect.Lists;
import my.GuiEventListener;
import my.Renderable;
import my.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class ExamplesScreen extends GuiScreenExtend{
    private final BiConsumer<String, Map<String, String>> CALLBACK;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<Renderable> renderables = new ArrayList<>();

    public ExamplesScreen(BiConsumer<String, Map<String, String>> callback) {
//        super(LocalizationKeys.EXAMPLES_GUI_TITLE.getComponent());
        CALLBACK = callback;
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        super.drawScreen(pMouseX, pMouseY, pPartialTick);

        for (Renderable renderable : this.renderables) {
            renderable.render(pMouseX, pMouseY, pPartialTick);
        }

        ITextComponent warning1 = LocalizationKeys.EXAMPLES_GUI_WARNING_1.getComponent();

        SFMFontUtils.draw(
                warning1,
                this.width / 2 - this.fontRenderer.getStringWidth(warning1.getUnformattedText()) / 2,
                20,
                0xffffff,
                false
        );
        ITextComponent warning2 = LocalizationKeys.EXAMPLES_GUI_WARNING_2.getComponent();

        SFMFontUtils.draw(
                warning2,
                this.width / 2 - this.fontRenderer.getStringWidth(warning2.getUnformattedText()) / 2,
                30,
                0xffffff,
                false
        );
    }

    @Override
    public void initGui() {
        super.initGui();
        renderables.clear();
        children.clear();
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        List<ResourceLocation> files = Tools.listSfmlResources("sfm", "template_programs");
        List<ResourceLocation> collect = files.stream()
                .filter((rl) -> rl.getPath().endsWith(".sfml") || rl.getPath().endsWith(".sfm"))
                .collect(Collectors.toList());

        Map<String, String> templatePrograms = new HashMap<>();
        for (ResourceLocation rl : collect) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceManager.getResource(rl).getInputStream(), StandardCharsets.UTF_8));
                String programString = reader.lines().collect(Collectors.joining("\n"));
                if (programString.contains("$REPLACE_RESOURCE_TYPES_HERE$")) {
                    List<? extends String> disallowedResourceTypesForTransfer = SFMServerConfig.disallowedResourceTypesForTransfer;
                    String replacement = SFMResourceTypes.registry().keySet()
                            .stream()
                            .map(ResourceLocation::getPath)
                            .map(e -> {
                                String text = "";
                                if (disallowedResourceTypesForTransfer.contains(e))
                                    text += "-- (disallowed in config) ";
                                text += "INPUT " + e + ":: FROM a";
                                return text;
                            })
                            .collect(Collectors.joining("\n    "));
                    programString = programString.replace("$REPLACE_RESOURCE_TYPES_HERE$", replacement);
                }
                String finalProgram = programString;
                ProgramBuilder.build(programString)
                        .caseSuccess((program, metadata) -> templatePrograms.put(
                                program.name().isEmpty() ? rl.toString() : program.name(),
                                finalProgram
                        ))
                        .caseFailure(result -> templatePrograms.put(
                                String.format("(compile failed) %s", rl.toString()),
                                finalProgram
                        ));
                Program.compile(
                        programString,
                        successProgram -> templatePrograms.put(
                                successProgram.name().isEmpty() ? rl.toString() : successProgram.name(),
                                finalProgram
                        ),
                        failure -> templatePrograms.put(
                                String.format("(compile failed) %s", rl.toString()),
                                finalProgram
                        )
                );
            } catch (IOException ignored) {
            }
        }

        // add picker buttons
        {
            int i = 0;
            int buttonWidth = templatePrograms.keySet()
                    .stream()
                    .mapToInt(this.fontRenderer::getStringWidth)
                    .max().orElse(50) + 10;
            int buttonHeight = 15;
            int paddingX = 3;
            int paddingY = 3;
            int buttonsPerRow = this.width / (buttonWidth + paddingX);
            for (Map.Entry<String, String> entry : templatePrograms
                    .entrySet()
                    .stream()
                    .sorted((o1, o2) -> Comparator.<String>naturalOrder().compare(o1.getKey(), o2.getKey()))
                    .collect(Collectors.toList())) {
                int x = (this.width - (buttonWidth + paddingX) * Math.min(buttonsPerRow, templatePrograms.size())) / 2
                        + paddingX
                        + (i % buttonsPerRow) * (
                        buttonWidth
                                + paddingX
                );
                int y = 50 + (i / buttonsPerRow) * (buttonHeight + paddingY);
                this.addRenderableWidget(
                        new SFMButtonBuilder()
                                .setText(new TextComponentString(entry.getKey()))
                                .setOnPress(btn -> {
//                                    onGuiClosed();
                                    CALLBACK.accept(entry.getValue(), templatePrograms);
                                })
                                .setPosition(x, y)
                                .setSize(buttonWidth, buttonHeight)
                                .build()
                );
                i++;
            }
        }
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T pWidget) {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends GuiEventListener> T addWidget(T pListener) {
        this.children.add(pListener);
        return pListener;
    }
}
