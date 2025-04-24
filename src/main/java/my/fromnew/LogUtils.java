package my.fromnew;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.spi.LoggerContext;

import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.apache.logging.log4j.Level.*;

public class LogUtils {
    public static final String FATAL_MARKER_ID = "FATAL";
//    public static final Marker FATAL_MARKER = MarkerFactory.getMarker(FATAL_MARKER_ID);
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static boolean isLoggerActive() {
        final LoggerContext loggerContext = LogManager.getContext();
        if (loggerContext instanceof LifeCycle) {
            LifeCycle lifeCycle = (LifeCycle) loggerContext;
            return !lifeCycle.isStopped();
        }
        return true; // Sensible default? In worst case, no logs - so not a huge loss
    }

//    public static void configureRootLoggingLevel(final Level level) {
//        final LoggerContext ctx = LogManager.getContext(false);
//        final Configuration config = ctx.getConfiguration();
//        final LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
//        loggerConfig.setLevel(convertLevel(level));
//        ctx.updateLoggers();
//    }

    private static Level convertLevel(final Level level) {
        if (level.equals(INFO)) {
            return INFO;
        } else if (level.equals(WARN)) {
            return WARN;
        } else if (level.equals(DEBUG)) {
            return DEBUG;
        } else if (level.equals(ERROR)) {
            return ERROR;
        } else if (level.equals(TRACE)) {
            return TRACE;
        }
        throw new IllegalArgumentException("Unexpected value: " + level);
    }

    public static Object defer(final Supplier<Object> result) {
        class ToString {
            @Override
            public String toString() {
                return result.get().toString();
            }
        }

        return new ToString();
    }

    /**
     * Caller sensitive, DO NOT WRAP
     */
    public static Logger getLogger() {
        return java.util.logging.Logger.getLogger(STACK_WALKER.getCallerClass().getName());
    }
}
