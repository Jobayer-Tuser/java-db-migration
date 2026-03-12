package me.jobayeralmahmud.javamigrations.migrations.columns;

public class StringColumn extends Column<StringColumn> {
    private final int length;

    public StringColumn(String name, int length) {
        super(name);
        this.length = length;
    }

    public StringColumn(String name) {
        this(name, 255);
    }

    @Override
    protected String sqlType() {
        return "VARCHAR(" + length + ")";
    }
}
