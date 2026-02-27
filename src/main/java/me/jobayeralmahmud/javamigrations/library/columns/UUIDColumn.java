package me.jobayeralmahmud.javamigrations.library.columns;

public class UUIDColumn extends Column<UUIDColumn> {

    public UUIDColumn(String name) { super(name); }

    @Override
    protected String sqlType() {
        return "BINARY(16)";
    }
}