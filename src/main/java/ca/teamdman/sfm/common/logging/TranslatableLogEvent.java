package ca.teamdman.sfm.common.logging;

import ca.teamdman.sfm.common.util.SFMTranslationUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;

import java.nio.charset.StandardCharsets;

public class TranslatableLogEvent {
    private final Level level;
    private final Instant instant;
    private final TextComponentTranslation contents;

    public TranslatableLogEvent(Level level, Instant instant, TextComponentTranslation contents) {
        this.level = level;
        this.instant = instant;
        this.contents = contents;
    }

    public Level level() {
        return level;
    }

    public Instant instant() {
        return instant;
    }

    public TextComponentTranslation contents() {
        return contents;
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(level.name());
        buf.writeLong(instant.getEpochMillisecond());
        buf.writeInt(instant.getNanoOfMillisecond());
        SFMTranslationUtils.encodeTranslation(contents, new PacketBuffer(buf));
    }

    public static TranslatableLogEvent decode(PacketBuffer buf) {
        Level level = Level.getLevel(buf.readString(999));
        long epochMillisecond = buf.readLong();
        int epochNano = buf.readInt();
        TextComponentTranslation contents = SFMTranslationUtils.decodeTranslation(buf);

        MutableInstant instant = new MutableInstant();
        instant.initFromEpochMilli(epochMillisecond, epochNano);

        return new TranslatableLogEvent(level, instant, contents);
    }
}
