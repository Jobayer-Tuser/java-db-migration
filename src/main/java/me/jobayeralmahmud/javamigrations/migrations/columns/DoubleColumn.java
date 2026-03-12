package me.jobayeralmahmud.javamigrations.migrations.columns;

public class DoubleColumn extends Column<DoubleColumn> {
    public DoubleColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "DOUBLE";
    }
}
