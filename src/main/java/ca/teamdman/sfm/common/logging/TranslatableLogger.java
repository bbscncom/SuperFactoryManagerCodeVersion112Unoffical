package ca.teamdman.sfm.common.logging;

import ca.teamdman.sfm.SFM;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.time.Instant;

import java.util.*;
import java.util.function.Consumer;

public class TranslatableLogger {
    private static final LoggerContext CONTEXT = new LoggerContext(SFM.MOD_ID);
    private final Logger logger;
    private Level logLevel = Level.OFF;

    public TranslatableLogger(String name) {
        this.logger = CONTEXT.getLogger(name);

        Configuration configuration = CONTEXT.getConfiguration();
        configuration.removeLogger(name);
        LoggerConfig config = new LoggerConfig(name, logLevel, false);
        configuration.addLogger(name, config);

        TranslatableAppender appender = TranslatableAppender.createAppender(name);

        config.removeAppender(name);
        config.addAppender(appender, Level.TRACE, null);
        appender.start();
    }

    public Level getLogLevel() {
        return CONTEXT.getConfiguration().getLoggerConfig(logger.getName()).getLevel();
    }

    public void setLogLevel(Level level) {
        LoggerConfig found = CONTEXT.getConfiguration().getLoggerConfig(logger.getName());
        found.setLevel(level);
        this.logLevel = level;
        CONTEXT.updateLoggers();
    }

    public void info(TextComponentTranslation contents) {
        if (this.logLevel.isLessSpecificThan(Level.INFO)) {
            logger.info(contents.getUnformattedText());
        }
    }

    public void info(Consumer<Consumer<TextComponentTranslation>> logger) {
        if (this.logLevel.isLessSpecificThan(Level.INFO)) {
            logger.accept(this::info);
        }
    }

    public void warn(TextComponentTranslation contents) {
        if (this.logLevel.isLessSpecificThan(Level.WARN)) {
            logger.warn(contents.getUnformattedText());
        }
    }

    public void warn(Consumer<Consumer<TextComponentTranslation>> logger) {
        if (this.logLevel.isLessSpecificThan(Level.WARN)) {
            logger.accept(this::warn);
        }
    }

    public void error(TextComponentTranslation contents) {
        if (this.logLevel.isLessSpecificThan(Level.ERROR)) {
            logger.error(contents.getUnformattedText());
        }
    }

    public void error(Consumer<Consumer<TextComponentTranslation>> logger) {
        if (this.logLevel.isLessSpecificThan(Level.ERROR)) {
            logger.accept(this::error);
        }
    }

    public void debug(TextComponentTranslation contents) {
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) {
            logger.debug(contents.getUnformattedText());
        }
    }

    public void debug(Consumer<Consumer<TextComponentTranslation>> logger) {
        if (this.logLevel.isLessSpecificThan(Level.DEBUG)) {
            logger.accept(this::debug);
        }
    }

    public void trace(TextComponentTranslation contents) {
        if (this.logLevel.isLessSpecificThan(Level.TRACE)) {
            logger.trace(contents.getUnformattedText());
        }
    }

    public void trace(Consumer<Consumer<TextComponentTranslation>> logger) {
        if (this.logLevel.isLessSpecificThan(Level.TRACE)) {
            logger.accept(this::trace);
        }
    }

    public ArrayDeque<TranslatableLogEvent> getLogs() {
        return new ArrayDeque<>(getContents());
    }

    public void clear() {
        getContents().clear();
    }

    public static boolean comesAfter(Instant a, Instant b) {
        return a.getEpochSecond() > b.getEpochSecond()
               || (a.getEpochSecond() == b.getEpochSecond()
                   && a.getNanoOfSecond() > b.getNanoOfSecond());
    }

    public static ArrayDeque<TranslatableLogEvent> decode(ByteBuf buf) {
        int size = buf.readInt();
        ArrayDeque<TranslatableLogEvent> contents = new ArrayDeque<>(size);
        for (int i = 0; i < size; i++) {
            contents.add(TranslatableLogEvent.decode(buf));
        }
        return contents;
    }

    public static void encodeAndDrain(Collection<TranslatableLogEvent> logs, ByteBuf buf) {
        int maxReadableBytes = 32600;
        ByteBuf chunk = Unpooled.buffer();
        int count = 0;
        for (Iterator<TranslatableLogEvent> iterator = logs.iterator(); iterator.hasNext(); ) {
            TranslatableLogEvent entry = iterator.next();
            ByteBuf check = Unpooled.buffer();
            entry.encode(check);
            if (check.readableBytes() + chunk.readableBytes() + buf.readableBytes() >= maxReadableBytes) {
                break;
            }
            chunk.writeBytes(check);
            iterator.remove();
            count += 1;
        }

        buf.writeInt(count);
        buf.writeBytes(chunk);
    }

    public ArrayDeque<TranslatableLogEvent> getLogsAfter(Instant instant) {
        List<TranslatableLogEvent> contents = getContents();
        ArrayDeque<TranslatableLogEvent> toSend = new ArrayDeque<>();
        ListIterator<TranslatableLogEvent> iter = contents.listIterator(contents.size());
        while (iter.hasPrevious()) {
            TranslatableLogEvent entry = iter.previous();
            if (comesAfter(entry.instant(), instant)) {
                toSend.addFirst(entry);
            } else {
                break;
            }
        }
        return toSend;
    }

    public void pruneSoWeDontEatAllTheRam() {
        List<TranslatableLogEvent> contents = getContents();
        if (contents.size() > 10_000) {
            int overage = contents.size() - 10_000;
            int to_prune = overage + 500;
            contents.subList(0, to_prune).clear();
        }
    }

    private LinkedList<TranslatableLogEvent> getContents() {
        Map<String, Appender> appenders = CONTEXT.getConfiguration().getLoggerConfig(logger.getName()).getAppenders();
        if (appenders.containsKey(logger.getName())) {
            Appender appender = appenders.get(logger.getName());
            if (appender instanceof TranslatableAppender) {
                return ((TranslatableAppender) appender).contents;
            }
        }
        return new LinkedList<>();
    }
}
