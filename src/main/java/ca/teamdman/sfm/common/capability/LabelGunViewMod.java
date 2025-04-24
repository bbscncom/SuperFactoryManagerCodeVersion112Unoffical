package ca.teamdman.sfm.common.capability;

import ca.teamdman.sfm.common.item.LabelGunItem;

import java.util.Objects;

public class LabelGunViewMod implements CapData<LabelGunItem.LabelGunViewMode> {
    public LabelGunItem.LabelGunViewMode data= LabelGunItem.LabelGunViewMode.SHOW_ALL;

    public LabelGunViewMod(LabelGunItem.LabelGunViewMode data) {
        this.data = data;
    }

    public LabelGunViewMod() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelGunViewMod that = (LabelGunViewMod) o;
        return data == that.data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public void clear() {
        this.data=LabelGunItem.LabelGunViewMode.SHOW_ALL;
    }

    @Override
    public void set(LabelGunItem.LabelGunViewMode data) {
        this.data=data;
    }

    @Override
    public LabelGunItem.LabelGunViewMode get() {
        return this.data;
    }
}
