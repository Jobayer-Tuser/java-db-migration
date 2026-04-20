package me.jobayeralmahmud.javamigrations.migrations;

import java.util.ArrayList;

public class ForeignKeyDefinition {
    public enum KeyType {
        INTEGER("BIGINT UNSIGNED"),
        UUID("BINARY(16)");

        private final String sqlType;
        KeyType(String sqlType) {
            this.sqlType = sqlType;
        }
    }

    /**
     * Core properties.
     */
    private String owningTable;
    private final String columnName;
    private final KeyType keyType;

    /**
     * Modifiers.
     */
    private boolean nullable = false;
    private boolean unique = false;
    private String afterColumn;
    private Object defaultValue;

    /**
     * referencesTable fields.
     */
    private boolean referencesTable = false;
    private String referencedTable = null;
    private String referencedColumn = "id";
    private String onUpdate = "CASCADE";
    private String onDelete = "RESTRICT";

    ForeignKeyDefinition(String columnName, KeyType keyType) {
        this.columnName = columnName;
        this.keyType = keyType;
    }

    public void owningTable(String tableName) {
        this.owningTable = tableName;
    }

    public ForeignKeyDefinition nullable() {
        this.nullable = true;
        return this;
    }

    public ForeignKeyDefinition unique() {
        this.unique = true;
        return this;
    }

    public ForeignKeyDefinition after(String columnName) {
        this.afterColumn = columnName;
        return this;
    }

    public ForeignKeyDefinition defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ForeignKeyDefinition referencesTable(String table) {
        this.referencesTable = true;
        this.referencedTable = table;
        return this;
    }

    public ForeignKeyDefinition referencesColumn(String column) {
        this.referencedColumn = column;
        return this;
    }

    public ForeignKeyDefinition onDeleteCascade() {
        this.onDelete = "CASCADE";
        return this;
    }

    public ForeignKeyDefinition onDeleteSetNull() {
        this.onDelete = "SET NULL";
        return this;

    }

    public ForeignKeyDefinition onDeleteRestrict() {
        this.onDelete = "RESTRICT";
        return this;
    }

    public ForeignKeyDefinition onUpdateCascade() {
        this.onUpdate = "CASCADE";
        return this;
    }

    public ForeignKeyDefinition onUpdateSetNull() {
        this.onUpdate = "SET NULL";
        return this;
    }

    public ForeignKeyDefinition onUpdateRestrict() {
        this.onUpdate = "RESTRICT";
        return this;
    }

    public String afterColumn() {
        return afterColumn;
    }

    public String getSqlDefinition() {
        var parts = new ArrayList<String>();

        parts.add(columnName + " " + keyType.sqlType);
        parts.add(nullable ? "DEFAULT NULL" : "NOT NULL");
        if (unique) parts.add("UNIQUE");
        else if (defaultValue != null) parts.add("DEFAULT '" + defaultValue + "'");

        return String.join(" ", parts);
    }

    public String getConstraintSql() {
        if (!referencesTable) return null;
        if (referencedTable == null) {
            throw new IllegalStateException(String.format("Foreign key %s is referencesTable but has no referenced table, referencesTable( %s ) or .referencesTable( %s ) to set it.", columnName, referencedTable, referencedTable));

        }
        return String.format(
            "CONSTRAINT fk_%s_%s FOREIGN KEY (%s) REFERENCES %s (%s) ON UPDATE %s ON DELETE %s",
            owningTable, columnName, columnName, referencedTable, referencedColumn, onUpdate, onDelete
        );
    }
}