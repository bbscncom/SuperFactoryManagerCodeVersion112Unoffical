package ca.teamdman.sfm.common.capability;

public interface CapData<T> {
    void clear();

    void set(T data);

    T get();

    default void setNullable(T data) {
        if (data == null)
            clear();
        else
            set(data);
    }

    default T getNotNull() {
        if (get() == null)
            clear();
        return get();
    }
}
