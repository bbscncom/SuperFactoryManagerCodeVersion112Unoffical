package ca.teamdman.sfm.client.handler;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.ClientRayCastHelpers;
import ca.teamdman.sfm.client.gui.screen.SFMFontUtils;
import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.net.ServerboundContainerExportsInspectionRequestPacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import javafx.scene.input.MouseButton;
import my.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


@Mod.EventBusSubscriber(modid = SFM.MOD_ID, value = Side.CLIENT)
public class ContainerScreenInspectorHandler {
    private static boolean visible = false;
    private static @Nullable GuiContainer lastScreen = null;
    private static final Button exportInspectorButton = new SFMButtonBuilder()
            .setSize(100, 20)
            .setPosition(5, 50)
            .setText(LocalizationKeys.CONTAINER_INSPECTOR_SHOW_EXPORTS_BUTTON)
            .setOnPress((button) -> {
                @Nullable TileEntity lookBlockEntity = ClientRayCastHelpers.getLookBlockEntity();
                if (lastScreen != null && lookBlockEntity != null) {
                    SFMPackets.sendToServer(new ServerboundContainerExportsInspectionRequestPacket(
                            lastScreen.inventorySlots.windowId,
                            lookBlockEntity.getPos()
                    ));
                }
            })
            .build();

    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        boolean shouldCapture = Minecraft.getMinecraft().currentScreen instanceof GuiContainer;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);
        int scaledWidth = res.getScaledWidth();
        int scaledHeight = res.getScaledHeight();

        int mouseX = Mouse.getX() * scaledWidth / mc.displayWidth;
        int mouseY = scaledHeight - Mouse.getY() * scaledHeight / mc.displayHeight - 1;
        if (shouldCapture && visible && exportInspectorButton.clicked(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            exportInspectorButton.playDownSound(mc);
            exportInspectorButton.onClick(mouseX, mouseY, 0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!visible) return;
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer screen = (GuiContainer) event.getGui();
            lastScreen = screen;
            Container menu = screen.inventorySlots;
            int containerSlotCount = 0;
            int inventorySlotCount = 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 350);


            // draw the button
            exportInspectorButton.render(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());


            // draw index on each slot
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            for (Object slotObj : menu.inventorySlots) {
                Slot slot = (Slot) slotObj;
                int colour;
                if (slot.inventory instanceof net.minecraft.entity.player.InventoryPlayer) {
                    colour = 0xFFFF00; // yellow
                    inventorySlotCount++;
                } else {
                    colour = 0xFFF;
                    containerSlotCount++;
                }
                font.drawString(
                        Integer.toString(slot.getSlotIndex()),
                        screen.getGuiLeft() + slot.xPos,
                        screen.getGuiTop() + slot.yPos,
                        colour
                );
            }

            // draw centered notices
            {
                ITextComponent notice = LocalizationKeys.CONTAINER_INSPECTOR_NOTICE_1
                        .getComponent()
                        .setStyle(new Style().setColor(TextFormatting.GOLD));
                int offset = font.getStringWidth(notice.getUnformattedText()) / 2;
                SFMFontUtils.draw(
                        notice,
                        screen.width / 2 - offset,
                        5,
                        0xFFFFFF,
                        true
                );
            }

            {
                ITextComponent notice = LocalizationKeys.CONTAINER_INSPECTOR_NOTICE_2.getComponent(
                        SFMKeyMappings.CONTAINER_INSPECTOR_KEY
                                .getKeyDescription()
//                                .withStyle(ChatFormatting.AQUA)
                ).setStyle(new Style().setColor(TextFormatting.GOLD));
                int offset = font.getStringWidth(notice.getUnformattedText()) / 2;
                SFMFontUtils.draw(
                        notice,
                        screen.width / 2 - offset,
                        16,
                        0xFFFFFF,
                        true
                );
            }

            // draw text for slot totals
            SFMFontUtils.draw(
                    LocalizationKeys.CONTAINER_INSPECTOR_CONTAINER_SLOT_COUNT.getComponent(
                            new TextComponentString(String.valueOf(containerSlotCount)).setStyle(new Style().setColor(TextFormatting.BLUE))
                    ),
                    5,
                    25,
                    0xFFFFFF,
                    true
            );
            SFMFontUtils.draw(
                    LocalizationKeys.CONTAINER_INSPECTOR_INVENTORY_SLOT_COUNT.getComponent(
                            new TextComponentString(String.valueOf(inventorySlotCount)).setStyle(new Style().setColor(TextFormatting.YELLOW))
                    ),
                    5,
                    40,
                    0xFFFFFF,
                    true
            );

            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public static void onKeyDown(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        // Handle Ctrl+I hotkey to toggle overlay
        KeyBinding toggleKey = SFMKeyMappings.CONTAINER_INSPECTOR_KEY;
        boolean toggleKeyPressed = Keyboard.isKeyDown(toggleKey.getKeyCode()) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        if (toggleKeyPressed) {
            visible = !visible;
            event.setCanceled(true);
            return;
        }

        // Handle ~ hotkey to inspect hovered item
        KeyBinding activateKey = SFMKeyMappings.ITEM_INSPECTOR_KEY;
//         activateKeyPressed = activateKey.isActiveAndMatches(InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode()));
        if (activateKey.isKeyDown()) {
            // This doesn't work when activated hovering a JEI item.
            if (event.getGui() instanceof GuiContainer) {
                GuiContainer acs = ((GuiContainer) event.getGui());
                Slot slotUnderMouse = acs.getSlotUnderMouse();
                if (slotUnderMouse != null) {
                    ItemStack hoveredStack = slotUnderMouse.getStack();
                    if (!hoveredStack.isEmpty()) {
                        SFMScreenChangeHelpers.showItemInspectorScreen(hoveredStack);
                    }
                }
            }
        }
    }
}
