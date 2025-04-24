package ca.teamdman.sfm.common.localization;

import ca.teamdman.sfm.common.util.SFMTranslationUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Objects;
import java.util.function.Supplier;

public final class LocalizationEntry {
    private final Supplier<String> key;
    private final Supplier<String> value;

    public LocalizationEntry(Supplier<String> key, Supplier<String> value) {
        this.key = key;
        this.value = value;
    }

    public LocalizationEntry(String key, String value) {
        this(() -> key, () -> value);
    }

    public TextComponentTranslation get(Object... args) {
        return SFMTranslationUtils.getTranslatableContents(key.get(), args);
    }

    public TextComponentTranslation get() {
        return SFMTranslationUtils.getTranslatableContents(key.get());
    }

    public String getString() {
        return I18n.format(key.get());
    }

    public String getString(Object... args) {
        return I18n.format(key.get(), args);
    }

    public String getStub() {
        return value.get();
    }

    public ITextComponent getComponent() {
        return new TextComponentTranslation(key.get());
    }

    public ITextComponent getComponent(Object... args) {
        return new TextComponentTranslation(key.get(), args);
    }

    public Supplier<String> key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalizationEntry that = (LocalizationEntry) o;
        return Objects.equals(key.get(), that.key.get()) &&
                Objects.equals(value.get(), that.value.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key.get(), value.get());
    }

    @Override
    public String toString() {
        return "LocalizationEntry[" +
                "key=" + key.get() + ", " +
                "value=" + value.get() + ']';
    }
}