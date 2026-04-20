package me.jobayeralmahmud.javamigrations.migrations;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumDefinition {

    /**
     * Core properties.
     */
    private final String name;
    private final String[] options;

    /**
     * Modifiers.
     */
    private boolean nullable = false;
    private boolean unique = false;
    private String afterColumn = null;
    private String defaultValue = null;

    EnumDefinition(String columnName, String... options) {
        this.name = columnName;
        this.options = options;
    }

    EnumDefinition(String columnName, Enum<?>... options) {
        this.name = columnName;
        this.options = Arrays.stream(options).map(Enum::name).toArray(String[]::new);
    }

    public EnumDefinition nullable() {
        this.nullable = true;
        return this;
    }

    public EnumDefinition unique() {
        this.unique = true;
        return this;
    }

    public EnumDefinition after(String columnName) {
        this.afterColumn = columnName;
        return this;
    }

    public EnumDefinition defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String afterColumn() {
        return afterColumn;
    }

    public String getSqlDefinition() {
        String enumValues = Arrays.stream(options)
                .map(o -> "'" + o + "'")
                .collect(Collectors.joining(", "));

        var parts = new java.util.ArrayList<String>();

        parts.add(name + " ENUM(" + enumValues + ")");
        parts.add(nullable ? "DEFAULT NULL" : "NOT NULL");
        if (unique)
            parts.add("UNIQUE");
        if (defaultValue != null)
            parts.add("DEFAULT '" + defaultValue + "'");

        return String.join(" ", parts);
    }
}