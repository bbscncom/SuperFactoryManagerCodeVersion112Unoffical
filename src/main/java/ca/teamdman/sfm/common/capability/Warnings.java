package ca.teamdman.sfm.common.capability;

import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Warnings implements CapData<List<TextComponentTranslation>> {
    public List<TextComponentTranslation> data = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warnings warnings = (Warnings) o;
        return Objects.equals(data, warnings.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public void clear() {
        this.data = new ArrayList<>();
    }

    @Override
    public void set(List<TextComponentTranslation> data) {
        this.data=data;
    }

    @Override
    public List<TextComponentTranslation> get() {
        return this.data;
    }
}
