package ca.teamdman.sfm.common.capability;

import java.util.Objects;

public class ProgramData implements  CapData<String> {
    private String data="";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramData that = (ProgramData) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public void clear() {
        this.data="";
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
