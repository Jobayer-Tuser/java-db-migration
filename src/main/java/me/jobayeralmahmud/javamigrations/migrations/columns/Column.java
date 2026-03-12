package me.jobayeralmahmud.javamigrations.migrations.columns;

public abstract class Column<T extends Column<T>> {
    protected String name;
    protected boolean unique = false;
    protected boolean nullable = false;
    protected String afterColumn = null;
    protected Object defaultValue = null;

    public Column(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public T nullable() {
        this.nullable = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T after(String columnName) {
        this.afterColumn = columnName;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T unique() {
        this.unique = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T defaultValue(Object value) {
        this.defaultValue = value;
        return (T) this;
    }

    public String afterColumn() {
        return afterColumn;
    }

    // Subclasses define only the SQL type token, e.g. "VARCHAR(255)", "BIGINT
    // UNSIGNED"
    protected abstract String sqlType();

    // Base class assembles the full column definition — no
    // formatNullable()/formatDefault() needed in subclasses
    public String getDefinition() {
        return name + " " + sqlType() + formatNullable() + formatDefault() + (unique ? " UNIQUE" : "");
    }

    protected String formatNullable() {
        return nullable ? " DEFAULT NULL" : " NOT NULL";
    }

    protected String formatDefault() {
        if (defaultValue == null)
            return "";
        if (defaultValue instanceof String)
            return " DEFAULT '" + defaultValue + "'";
        return " DEFAULT " + defaultValue;
    }
}