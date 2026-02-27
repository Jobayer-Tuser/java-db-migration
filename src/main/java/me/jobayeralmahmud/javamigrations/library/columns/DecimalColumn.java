package me.jobayeralmahmud.javamigrations.library.columns;

public class DecimalColumn extends Column<DecimalColumn> {
    private final int precision;
    private final int scale;

    public DecimalColumn(String name, int precision, int scale) {
        super(name);
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    protected String sqlType() {
        return String.format("DECIMAL(%d, %d)", precision, scale);
    }
}