package me.jobayeralmahmud.javamigrations.library.columns;

public class TextColumn extends Column<TextColumn> {
    public TextColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "TEXT";
    }
}
