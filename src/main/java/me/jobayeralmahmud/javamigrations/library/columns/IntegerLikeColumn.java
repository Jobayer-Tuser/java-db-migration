package me.jobayeralmahmud.javamigrations.library.columns;

public abstract class IntegerLikeColumn<T extends IntegerLikeColumn<T>> extends Column<T> {
    protected boolean unsigned = false;

    public IntegerLikeColumn(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    public T unsigned() {
        this.unsigned = true;
        return (T) this;
    }

    protected String unsignedSuffix() {
        return unsigned ? " UNSIGNED" : "";
    }
}
