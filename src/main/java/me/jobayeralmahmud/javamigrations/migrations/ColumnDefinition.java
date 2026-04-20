package me.jobayeralmahmud.javamigrations.migrations;

import java.util.ArrayList;

public class ColumnDefinition {

    // Core properties.
    private final String name;
    private final Object dataType;

    // Modifiers.
    private boolean nullable = false;
    private boolean unique = false;
    private boolean unsigned = false;
    private boolean primaryKey = false;
    private boolean autoIncrement = false;
    private boolean defaultCurrentTimestamp = false;
    private boolean onUpdateCurrentTimestamp = false;

    // Default value can be of any type (String, Number, Boolean, etc.)
    private Object defaultValue;
    private String afterColumn;

    ColumnDefinition(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    ColumnDefinition(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    /**
     * Fluent modifiers.
     * 
     * @return this for chaining
     */
    public ColumnDefinition nullable() {
        this.nullable = true;
        return this;
    }

    public ColumnDefinition unique() {
        this.unique = true;
        return this;
    }

    public ColumnDefinition unsigned() {
        this.unsigned = true;
        return this;
    }

    public ColumnDefinition primaryKey() {
        this.primaryKey = true;
        return this;
    }

    public ColumnDefinition autoIncrement() {
        this.autoIncrement = true;
        return this;
    }

    public ColumnDefinition defaultCurrentTimestamp() {
        this.defaultCurrentTimestamp = true;
        return this;
    }

    public ColumnDefinition onUpdateCurrentTimestamp() {
        this.onUpdateCurrentTimestamp = true;
        return this;
    }

    public ColumnDefinition defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ColumnDefinition after(String columnName) {
        this.afterColumn = columnName;
        return this;
    }

    public String afterColumn() {
        return afterColumn;
    }

    // Render the SQL definition
    public String getSqlDefinition() {
        var parts = new ArrayList<String>();

        parts.add(name + " " + dataType);

        if (unsigned)
            parts.add("UNSIGNED");
        if (autoIncrement)
            parts.add("AUTO_INCREMENT");
        if (primaryKey)
            parts.add("PRIMARY KEY");
        parts.add(nullable ? "DEFAULT NULL" : "NOT NULL");
        if (unique)
            parts.add("UNIQUE");
        if (defaultCurrentTimestamp)
            parts.add("DEFAULT CURRENT_TIMESTAMP");
        else if (defaultValue != null)
            parts.add("DEFAULT " + defaultValue);
        if (onUpdateCurrentTimestamp)
            parts.add("ON UPDATE CURRENT_TIMESTAMP");

        return String.join(" ", parts);
    }
}