package ca.teamdman.sfm.common.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SFMTranslationUtils {
    public static final int MAX_TRANSLATION_ELEMENT_LENGTH = 10240;

    public static TextComponentTranslation deserializeTranslation(NBTTagCompound tag) {
        String key = tag.getString("key");
        List<String> list = new ArrayList<>();
        tag.getTagList("args", Constants.NBT.TAG_STRING)
                .forEach(nbtBase -> {
                    list.add(((NBTTagString) nbtBase).getString());
                });
        return getTranslatableContents(key, list);
    }

    public static NBTTagCompound serializeTranslation(ITextComponent component) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("key", component.getUnformattedText());
        NBTTagList args = new NBTTagList();
        // 1.12.2 的 ITextComponent 不支持直接获取参数，这里返回空列表
        tag.setTag("args", args);
        return tag;
    }

    public static NBTTagCompound serializeTranslation(TextComponentTranslation contents) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("key", contents.getKey());
        NBTTagList args = new NBTTagList();
        for (Object arg : contents.getFormatArgs()) {
            args.appendTag(new NBTTagString(arg.toString()));
        }
        tag.setTag("args", args);
        return tag;
    }

    public static void encodeTranslation(
            TextComponentTranslation contents,
            PacketBuffer buf
    ) {
//        MAX_TRANSLATION_ELEMENT_LENGTH
        buf.writeString(contents.getKey() );
        buf.writeVarInt(contents.getFormatArgs().length);
        for (Object arg : contents.getFormatArgs()) {
//            MAX_TRANSLATION_ELEMENT_LENGTH
            buf.writeString(String.valueOf(arg));
        }
    }

    public static TextComponentTranslation decodeTranslation(PacketBuffer buf) {
        String key = buf.readString(MAX_TRANSLATION_ELEMENT_LENGTH);
        int argCount = buf.readVarInt();
        Object[] args = new Object[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = buf.readString(MAX_TRANSLATION_ELEMENT_LENGTH);
        }
        return getTranslatableContents(key, args);
    }

    @MCVersionDependentBehaviour
    public static TextComponentTranslation getTranslatableContents(
            String key,
            Object... args
    ) {
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Number || arg instanceof Boolean || arg instanceof String) {
                newArgs[i] = arg;
            } else if (arg == null) {
                newArgs[i] = "null";
            } else {
//                SFM.LOGGER.warn(
//                        "Invalid argument type for translation argument {} key '{}': {}",
//                        i,
//                        key,
//                        arg.getClass().getName(),
//                        new IllegalArgumentException()
//                );
                newArgs[i] = arg.toString();
            }
        }
        return new TextComponentTranslation(key, null, newArgs);
    }

    /**
     * Helper method to avoid noisy git merges between versions
     */
    public static TextComponentTranslation getTranslatableContents(String key) {
        return getTranslatableContents(key, new Object[]{});
    }
}
