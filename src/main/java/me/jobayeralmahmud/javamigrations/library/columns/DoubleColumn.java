package me.jobayeralmahmud.javamigrations.library.columns;

public class DoubleColumn extends Column<DoubleColumn> {
    public DoubleColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "DOUBLE";
    }
}
