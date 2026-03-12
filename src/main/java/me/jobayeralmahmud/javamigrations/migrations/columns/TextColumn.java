package me.jobayeralmahmud.javamigrations.migrations.columns;

public class TextColumn extends Column<TextColumn> {
    public TextColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "TEXT";
    }
}
