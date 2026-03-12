package me.jobayeralmahmud.javamigrations.migrations.columns;

public class UUIDColumn extends Column<UUIDColumn> {

    public UUIDColumn(String name) { super(name); }

    @Override
    protected String sqlType() {
        return "BINARY(16)";
    }
}