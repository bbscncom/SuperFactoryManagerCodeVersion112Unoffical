package my.fromnew;

import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StackWalker {
    public interface StackFrame {
        String getClassName();
        String getMethodName();
        Class<?> getDeclaringClass();
        default MethodType getMethodType() {
            throw new UnsupportedOperationException();
        }
        default String getDescriptor() {
            throw new UnsupportedOperationException();
        }
        int getByteCodeIndex();
        String getFileName();
        int getLineNumber();
        boolean isNativeMethod();
        StackTraceElement toStackTraceElement();
    }

    public enum Option {
        RETAIN_CLASS_REFERENCE,
        SHOW_REFLECT_FRAMES,
        SHOW_HIDDEN_FRAMES
    }

    private static final StackWalker DEFAULT_WALKER = new StackWalker(EnumSet.noneOf(Option.class));

    private final Set<Option> options;
    private final boolean retainClassRef;

    private StackWalker(Set<Option> options) {
        this.options = Collections.unmodifiableSet(new HashSet<>(options));
        this.retainClassRef = options.contains(Option.RETAIN_CLASS_REFERENCE);
    }

    public static StackWalker getInstance() {
        return DEFAULT_WALKER;
    }

    public static StackWalker getInstance(Option option) {
        return getInstance(EnumSet.of(option));
    }

    public static StackWalker getInstance(Set<Option> options) {
        if (options.isEmpty()) {
            return DEFAULT_WALKER;
        }
        return new StackWalker(options);
    }

    public <T> T walk(Function<? super Stream<StackFrame>, ? extends T> function) {
        Objects.requireNonNull(function);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackFrame> frames = Arrays.stream(stackTrace)
                .skip(2) // Skip getStackTrace and walk methods
                .map(this::toStackFrame)
                .collect(Collectors.toList());
        return function.apply(frames.stream());
    }

    public void forEach(Consumer<? super StackFrame> action) {
        Objects.requireNonNull(action);
        walk(s -> {
            s.forEach(action);
            return null;
        });
    }

    public Class<?> getCallerClass() {
        if (!retainClassRef) {
            throw new UnsupportedOperationException("RETAIN_CLASS_REFERENCE option not set");
        }

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length < 3) {
            throw new IllegalStateException("No caller frame found");
        }

        try {
            return Class.forName(stackTrace[2].getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private StackFrame toStackFrame(StackTraceElement element) {
        return new StackFrame() {
            @Override
            public String getClassName() {
                return element.getClassName();
            }

            @Override
            public String getMethodName() {
                return element.getMethodName();
            }

            @Override
            public Class<?> getDeclaringClass() {
                if (!retainClassRef) {
                    throw new UnsupportedOperationException();
                }
                try {
                    return Class.forName(element.getClassName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public int getByteCodeIndex() {
                return -1; // Not available in Java 8
            }

            @Override
            public String getFileName() {
                return element.getFileName();
            }

            @Override
            public int getLineNumber() {
                return element.getLineNumber();
            }

            @Override
            public boolean isNativeMethod() {
                return element.isNativeMethod();
            }

            @Override
            public StackTraceElement toStackTraceElement() {
                return element;
            }
        };
    }
}