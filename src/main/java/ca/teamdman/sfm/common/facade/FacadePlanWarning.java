package ca.teamdman.sfm.common.facade;

import ca.teamdman.sfm.common.localization.LocalizationEntry;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import net.minecraft.util.text.ITextComponent;

import java.util.Random;

public class FacadePlanWarning {

        public ITextComponent confirmTitle;
        public ITextComponent confirmMessage;
        public ITextComponent confirmYes;
        public ITextComponent confirmNo;
    private static final LocalizationEntry[] CONFIRM_YES_VARIANTS = new LocalizationEntry[]{
            LocalizationKeys.CONFIRM_FUNNY_YES_1,
            LocalizationKeys.CONFIRM_FUNNY_YES_2,
            LocalizationKeys.CONFIRM_FUNNY_YES_3,
            LocalizationKeys.CONFIRM_FUNNY_YES_4,
            LocalizationKeys.CONFIRM_FUNNY_YES_5,
            LocalizationKeys.CONFIRM_FUNNY_YES_6,
            };
    private static final LocalizationEntry[] CONFIRM_NO_VARIANTS = new LocalizationEntry[]{
            LocalizationKeys.CONFIRM_FUNNY_NO_1,
            LocalizationKeys.CONFIRM_FUNNY_NO_2,
            LocalizationKeys.CONFIRM_FUNNY_NO_3,
            LocalizationKeys.CONFIRM_FUNNY_NO_4,
            LocalizationKeys.CONFIRM_FUNNY_NO_5,
            LocalizationKeys.CONFIRM_FUNNY_NO_6,
            };

    public FacadePlanWarning(ITextComponent confirmTitle, ITextComponent confirmMessage, ITextComponent confirmYes, ITextComponent confirmNo) {
        this.confirmTitle=confirmTitle;
        this.confirmMessage=confirmMessage;
        this.confirmYes=confirmYes;
        this.confirmNo=confirmNo;
    }

    public static FacadePlanWarning of(
            ITextComponent confirmTitle,
            ITextComponent confirmMessage
    ) {
        Random random = new Random();
        ITextComponent confirmYes = CONFIRM_YES_VARIANTS[random.nextInt(CONFIRM_YES_VARIANTS.length)].getComponent();
        ITextComponent confirmNo = CONFIRM_NO_VARIANTS[random.nextInt(CONFIRM_NO_VARIANTS.length)].getComponent();
        return new FacadePlanWarning(confirmTitle, confirmMessage, confirmYes, confirmNo);
    }
}
