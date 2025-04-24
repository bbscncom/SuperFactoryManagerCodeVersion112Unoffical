package ca.teamdman.sfm.common.capability;

import java.util.Objects;

public class ActiveLabel implements CapData<String> {
    public String data = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveLabel that = (ActiveLabel) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public void clear() {
        this.data = "";
    }

    @Override
    public void set(String data) {
        this.data=data;
    }

    @Override
    public String get() {
        return this.data;
    }
}
