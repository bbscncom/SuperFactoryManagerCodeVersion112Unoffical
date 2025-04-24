package ca.teamdman.sfm.common.item;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.ClientKeyHelpers;
import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import ca.teamdman.sfm.client.handler.LabelGunKeyMappingHandler;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.capability.AbstarctCapabilityProvider;
import ca.teamdman.sfm.common.capability.ActiveLabel;
import ca.teamdman.sfm.common.capability.LabelGunCapProvider;
import ca.teamdman.sfm.common.capability.LabelGunViewMod;
import ca.teamdman.sfm.common.net.ServerboundLabelGunUsePacket;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMCreativeTabs;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMItemUtils;
import my.Tools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SFM.MOD_ID)
public class LabelGunItem extends Item {
    public LabelGunItem() {
        this.setMaxStackSize(1);
        this.setCreativeTab(SFMCreativeTabs.SFMTAB);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new LabelGunCapProvider();
    }

    public static void setActiveLabel(
            ItemStack stack,
            @Nullable String label
    ) {
        if (label == null || label.isEmpty()) {
            clearActiveLabel(stack);
        } else {
            LabelPositionHolder.from(stack).addReferencedLabel(label).save(stack);
            AbstarctCapabilityProvider.updateCapAndNBT(stack,SFMDataComponents.ACTIVE_LABEL,label);
        }
    }

    public static String getActiveLabel(ItemStack stack) {

        return AbstarctCapabilityProvider.getCapFromNBT(stack,SFMDataComponents.ACTIVE_LABEL).getNotNull();
    }

    public static String getNextLabel(
            ItemStack gun,
            int change
    ) {
        List<String> labels = LabelPositionHolder
                .from(gun)
                .labels
                .keySet()
                .stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        if (labels.isEmpty()) return "";
        String currentLabel = getActiveLabel(gun);

        int currentLabelIndex = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i).equals(currentLabel)) {
                currentLabelIndex = i;
                break;
            }
        }

        int nextLabelIndex = currentLabelIndex + change;
        // ensure going negative wraps around
        nextLabelIndex = ((nextLabelIndex % labels.size()) + labels.size()) % labels.size();

        return labels.get(nextLabelIndex);
    }


    public static void clearActiveLabel(
            ItemStack gun
    ) {
        Tools.clearCapsNBT(gun,SFMDataComponents.ACTIVE_LABEL);
    }

    /**
     * Returns the current enum mode for the label gun item.
     */
    public static LabelGunViewMode getViewMode(ItemStack stack) {
        return AbstarctCapabilityProvider.getCapFromNBT(stack, SFMDataComponents.LABEL_GUN_VIEW_MODE)
                .getNotNull();
    }

    /**
     * Sets the view mode in NBT.
     */
    public static void setViewMode(ItemStack stack, LabelGunViewMode mode) {
        AbstarctCapabilityProvider.updateCapAndNBT(stack,SFMDataComponents.LABEL_GUN_VIEW_MODE,mode);
    }

    public static void cycleViewMode(ItemStack stack) {
        LabelGunViewMode current = getViewMode(stack);
        int nextOrdinal = (current.ordinal() + 1) % LabelGunViewMode.values().length;
        setViewMode(stack, LabelGunViewMode.values()[nextOrdinal]);
    }


    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        ArrayList<ITextComponent> textComponentStrings = new ArrayList<>();
        if (SFMItemUtils.isClientAndMoreInfoKeyPressed()) {
//            Options options = Minecraft.getMinecraft();
//
//            textComponentStrings.add(
//                    LocalizationKeys.LABEL_GUN_ITEM_TOOLTIP_1.getComponent(
//                            options.keyUse.getTranslatedKeyMessage().plainCopy().withStyle(ChatFormatting.AQUA)
//                    ).withStyle(ChatFormatting.GRAY)
//            );
//            textComponentStrings.add(
//                    LocalizationKeys.LABEL_GUN_ITEM_TOOLTIP_2.getComponent(
//                            options.keyUse.getTranslatedKeyMessage().plainCopy().withStyle(ChatFormatting.AQUA)
//                    ).withStyle(ChatFormatting.GRAY)
//            );
//            textComponentStrings.add(
//                    LocalizationKeys.LABEL_GUN_ITEM_TOOLTIP_3.getComponent(
//                            new TextComponentString("Control").withStyle(ChatFormatting.AQUA)
//                    ).withStyle(ChatFormatting.GRAY)
//            );
//            textComponentStrings.add(
//                    LocalizationKeys.LABEL_GUN_ITEM_TOOLTIP_4.getComponent(
//                            options.keyPickItem.getTranslatedKeyMessage().plainCopy().withStyle(ChatFormatting.AQUA)
//                    ).withStyle(ChatFormatting.GRAY)
//            );
//            textComponentStrings.add(
//                    LocalizationKeys.LABEL_GUN_ITEM_TOOLTIP_5.getComponent(
//                            SFMKeyMappings.CYCLE_LABEL_VIEW_KEY
//                                    .getKeyDescription())
//            );
        } else {
            SFMItemUtils.appendMoreInfoKeyReminderTextIfOnClient(textComponentStrings);
        }
        textComponentStrings.addAll(LabelPositionHolder.from(stack).asHoverText());

        tooltip.addAll(textComponentStrings.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()));

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world,
            EntityPlayer player,
            EnumHand hand
    ) {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote) {
            SFMScreenChangeHelpers.showLabelGunScreen(stack, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return I18n.translateToLocal("item.sfm.labelgun");
//        String name = getActiveLabel(stack);
//        if (name.isEmpty()) return super.getItemStackDisplayName(stack);
//        Style style = new Style().setColor(TextFormatting.AQUA);
//        return new TextComponentString(name).setStyle(style).getFormattedText();
    }

    public static void clearAll(ItemStack stack) {
        LabelPositionHolder.clearNbt(stack);
        LabelGunItem.setActiveLabel(stack, null);
    }

    public enum LabelGunViewMode {
        SHOW_ALL,
        SHOW_ONLY_ACTIVE_LABEL_AND_TARGETED_BLOCK,
        SHOW_ONLY_TARGETED_BLOCK;

    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            boolean pickBlock = ClientKeyHelpers.isKeyDownInWorld(SFMKeyMappings.LABEL_GUN_PICK_BLOCK_MODIFIER_KEY);
            SFMPackets.sendToServer(new ServerboundLabelGunUsePacket(
                    hand,
                    pos,
                    GuiScreen.isCtrlKeyDown(),
                    pickBlock,
                    GuiScreen.isShiftKeyDown()
            ));

            if (pickBlock) {
                LabelGunKeyMappingHandler.setExternalDebounce();
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
