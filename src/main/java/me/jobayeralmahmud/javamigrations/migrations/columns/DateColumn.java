package me.jobayeralmahmud.javamigrations.migrations.columns;

public class DateColumn extends Column<DateColumn> {
    public DateColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "DATE";
    }
}
