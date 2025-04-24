package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public interface SFMMessage<REQ extends IMessage,REPLY extends IMessage> extends IMessage, IMessageHandler<REQ , REPLY>, SideContainer{
    static String truncate(
            String input,
            int maxLength
    ) {
        if (input.length() > maxLength) {
            SFM.LOGGER.warn(
                    "input too big, truncation has occurred! (len={}, max={}, over={})",
                    input.length(),
                    maxLength,
                    maxLength - input.length()
            );
            String truncationWarning = "\n...truncated";
            return input.substring(0, maxLength - truncationWarning.length()) + truncationWarning;
        }
        return input;
    }
}
