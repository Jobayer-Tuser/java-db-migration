package me.jobayeralmahmud.javamigrations.migrations.columns;

public class TimeStampColumn extends Column<TimeStampColumn> {
    public TimeStampColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "TIMESTAMP";
    }
}
