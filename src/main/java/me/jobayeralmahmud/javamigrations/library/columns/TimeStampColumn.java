package me.jobayeralmahmud.javamigrations.library.columns;

public class TimeStampColumn extends Column<TimeStampColumn> {
    public TimeStampColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "TIMESTAMP";
    }
}
