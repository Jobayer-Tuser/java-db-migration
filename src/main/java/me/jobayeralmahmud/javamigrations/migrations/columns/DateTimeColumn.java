package me.jobayeralmahmud.javamigrations.migrations.columns;

public class DateTimeColumn extends Column<DateTimeColumn> {
    public DateTimeColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "DATETIME(6)";
    }
}
