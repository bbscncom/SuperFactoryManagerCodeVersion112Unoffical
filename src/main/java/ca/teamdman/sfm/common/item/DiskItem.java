package ca.teamdman.sfm.common.item;

import ca.teamdman.sfm.client.ClientKeyHelpers;
import ca.teamdman.sfm.client.ProgramSyntaxHighlightingHelper;
import ca.teamdman.sfm.client.gui.screen.ProgramEditScreenOpenContext;
import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.capability.*;
import ca.teamdman.sfm.common.linting.ProgramLinter;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.net.ServerboundDiskItemSetProgramPacket;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMCreativeTabs;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMEnvironmentUtils;
import ca.teamdman.sfm.common.util.SFMItemUtils;
import ca.teamdman.sfml.ast.Program;
import my.Tools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DiskItem extends Item {

    public DiskItem() {
        super();
        this.setCreativeTab(SFMCreativeTabs.SFMTAB);
    }

    public static String getProgram(ItemStack stack) {
//        String data = stack.getCapability(SFMDataComponents.PROGRAM_DATA, null).getData();
        // 获取 ItemStack 的 TagCompound
//        NBTTagCompound tag = stack.getTagCompound();
//        if (tag != null && tag.hasKey("programData", 8)) {  // 8 表示字符串类型
//            return tag.getString("programData");
//        }
//        return "";
        AbstarctCapabilityProvider.readSingleCapabilityFromNBT(stack, SFMDataComponents.PROGRAM_DATA);
        return stack.getCapability(SFMDataComponents.PROGRAM_DATA, null).get();
    }

    public static void setProgram(
            ItemStack stack,
            String programString
    ) {
        // 确保 TagCompound 存在，如果没有则创建一个新的
//        NBTTagCompound tag = stack.getTagCompound();
//        if (tag == null) {
//            tag = new NBTTagCompound();
//            stack.setTagCompound(tag);
//        }
//        programString = programString.replaceAll("\r", "");
//        tag.setString("programData", programString);
        stack.getCapability(SFMDataComponents.PROGRAM_DATA, null).set(programString);
        AbstarctCapabilityProvider.updateSingleCapabilityToNBT(stack,SFMDataComponents.PROGRAM_DATA);
    }

    public static void pruneIfDefault(ItemStack stack) {
        if (getProgram(stack).isEmpty() && LabelPositionHolder.from(stack).isEmpty()) {
            clearData(stack);
        }
    }

    public static void clearData(ItemStack stack) {
        Tools.clearCapsNBT(stack,
                SFMDataComponents.WARNINGS,
                SFMDataComponents.ERRORS,
                SFMDataComponents.PROGRAM_DATA,
                SFMDataComponents.LABEL_POSITION_HOLDER);
        AbstarctCapabilityProvider.clearAllCapabilities(stack);
    }

    public static @Nullable Program compileAndUpdateErrorsAndWarnings(
            ItemStack stack,
            @Nullable ManagerBlockEntity manager
    ) {
        if (manager != null) {
            manager.logger.info(x -> x.accept(LocalizationKeys.PROGRAM_COMPILE_FROM_DISK_BEGIN.get()));
        }
        AtomicReference<Program> rtn = new AtomicReference<>(null);
        Program.compile(
                getProgram(stack),
                successProgram -> {
                    ArrayList<TextComponentTranslation> warnings = ProgramLinter.gatherWarnings(
                            successProgram,
                            LabelPositionHolder.from(stack),
                            manager
                    );

                    // Log to disk
                    if (manager != null) {
                        manager.logger.info(x -> x.accept(LocalizationKeys.PROGRAM_COMPILE_SUCCEEDED_WITH_WARNINGS.get(
                                successProgram.name(),
                                warnings.size()
                        )));
                        manager.logger.warn(warnings::forEach);
                    }

                    // Update disk properties
                    setProgramName(stack, successProgram.name());
                    setWarnings(stack, warnings);
                    setErrors(stack, Collections.emptyList());

                    // Track result
                    rtn.set(successProgram);
                },
                errors -> {
                    List<TextComponentTranslation> warnings = Collections.emptyList();

                    // Log to disk
                    if (manager != null) {
                        manager.logger.error(x -> x.accept(LocalizationKeys.PROGRAM_COMPILE_FAILED_WITH_ERRORS.get(
                                errors.size())));
                        manager.logger.error(errors::forEach);
                    }

                    // Update disk properties
                    setWarnings(stack, warnings);
                    setErrors(stack, errors);
                }
        );
        return rtn.get();
    }

    public static List<TextComponentTranslation> getErrors(ItemStack stack) {
        AbstarctCapabilityProvider.readSingleCapabilityFromNBT(stack,SFMDataComponents.ERRORS);
        Errors errors = Tools.getOrDefault(stack, SFMDataComponents.ERRORS, new Errors());
        return errors.data;
    }

    public static void setErrors(
            ItemStack stack,
            List<TextComponentTranslation> errors
    ) {
        stack.getCapability(SFMDataComponents.ERRORS, null).data = errors;
        AbstarctCapabilityProvider.updateSingleCapabilityToNBT(stack,SFMDataComponents.ERRORS);
    }

    public static List<TextComponentTranslation> getWarnings(ItemStack stack) {
        AbstarctCapabilityProvider.readSingleCapabilityFromNBT(stack,SFMDataComponents.WARNINGS);
        Warnings warnings = Tools.getOrDefault(stack, SFMDataComponents.WARNINGS, new Warnings());
        return warnings.data;
    }

    public static void setWarnings(
            ItemStack stack,
            List<TextComponentTranslation> warnings
    ) {
        stack.getCapability(SFMDataComponents.WARNINGS, null).data = warnings;
        AbstarctCapabilityProvider.updateSingleCapabilityToNBT(stack,SFMDataComponents.WARNINGS);
    }

    public static void setProgramName(
            ItemStack stack,
            String name
    ) {
        if (!name.isEmpty()) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setString("item_name", name);
            stack.setTagCompound(nbtTagCompound);
        }
    }

    public static String getProgramName(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            return tagCompound.getString("item_name");
        }
        return "disk";
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world,
            EntityPlayer pPlayer,
            EnumHand pUsedHand
    ) {
        ItemStack stack = pPlayer.getHeldItem(pUsedHand);
        if (world.isRemote) {
            SFMScreenChangeHelpers.showProgramEditScreen(new ProgramEditScreenOpenContext(
                    getProgram(stack),
                    LabelPositionHolder.from(stack),
                    newProgramString -> SFMPackets.sendToServer(new ServerboundDiskItemSetProgramPacket(
                            newProgramString,
                            pUsedHand
                    ))
            ));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (SFMEnvironmentUtils.isClient()) {
            if (ClientKeyHelpers.isKeyDownInScreenOrWorld(SFMKeyMappings.MORE_INFO_TOOLTIP_KEY))
                return I18n.translateToLocal(this.getTranslationKey(stack));
        }
        String name = getProgramName(stack);
        if (name.isEmpty()) return I18n.translateToLocal(this.getTranslationKey(stack));
        Style style = new Style().setColor(TextFormatting.AQUA);
        return new TextComponentString(name).setStyle(style).getFormattedText();

    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        List<ITextComponent> textComponentStrings = new ArrayList<>();
        String program = DiskItem.getProgram(stack);
        if (SFMItemUtils.isClientAndMoreInfoKeyPressed() && !program.isEmpty()) {
            // show the program
            textComponentStrings.add(SFMItemUtils.getRainbow(getItemStackDisplayName(stack).length()));
            textComponentStrings.addAll(ProgramSyntaxHighlightingHelper.withSyntaxHighlighting(program, false));
        } else {
            textComponentStrings.addAll(LabelPositionHolder.from(stack).asHoverText());
            getErrors(stack)
                    .stream()
                    .map(line -> line.setStyle(new Style().setColor(TextFormatting.RED)))
                    .forEach(iTextComponent -> textComponentStrings.add(iTextComponent));
            getWarnings(stack)
                    .stream()
                    .map(line -> line.setStyle(new Style().setColor(TextFormatting.YELLOW)))
                    .forEach(iTextComponent -> textComponentStrings.add(iTextComponent));
            if (!program.isEmpty()) {
                SFMItemUtils.appendMoreInfoKeyReminderTextIfOnClient(textComponentStrings);
            }
        }
        if (!program.isEmpty()) {
            textComponentStrings.add(new TextComponentString(LocalizationKeys.DISK_EDIT_IN_HAND_TOOLTIP.getStub()).setStyle(new Style().setColor(TextFormatting.GRAY)));
        }
        tooltip.addAll(textComponentStrings.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()));
    }


    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new DiskCapProvider();
    }
}
