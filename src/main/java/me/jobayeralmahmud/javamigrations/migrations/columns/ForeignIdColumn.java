package me.jobayeralmahmud.javamigrations.migrations.columns;

/**
 * Represents a BIGINT UNSIGNED foreign key column with an optional constraint.
 * Follows the convention FK_{table}_{column} for constraint naming.
 */
public class ForeignIdColumn extends Column<ForeignIdColumn> {

    private String referencedTable;
    private String referencedColumn = "id";
    private String onUpdate = "RESTRICT";
    private String onDelete = "RESTRICT";
    private boolean constrained = false;
    private String owningTable;

    public ForeignIdColumn(String name) {
        super(name);
    }

    /** Set the owning table (injected by Blueprint). */
    public void setTable(String tableName) {
        this.owningTable = tableName;
    }

    /**
     * Infer the referenced table from the column name, e.g. role_id → roles.
     */
    public ForeignIdColumn constrained() {
        this.constrained = true;
        String base = name.endsWith("_id") ? name.substring(0, name.length() - 3) : name;
        this.referencedTable = base + "s";
        return this;
    }

    /**
     * Explicitly name the referenced table.
     */
    public ForeignIdColumn constrained(String table) {
        this.constrained = true;
        this.referencedTable = table;
        return this;
    }

    public ForeignIdColumn references(String column) {
        this.referencedColumn = column;
        return this;
    }

    public ForeignIdColumn onUpdateCascade() {
        this.onUpdate = "CASCADE";
        return this;
    }

    public ForeignIdColumn onUpdateSetNull() {
        this.onUpdate = "SET NULL";
        return this;
    }

    public ForeignIdColumn onUpdateRestrict() {
        this.onUpdate = "RESTRICT";
        return this;
    }

    public ForeignIdColumn onDeleteCascade() {
        this.onDelete = "CASCADE";
        return this;
    }

    public ForeignIdColumn onDeleteSetNull() {
        this.onDelete = "SET NULL";
        return this;
    }

    public ForeignIdColumn onDeleteRestrict() {
        this.onDelete = "RESTRICT";
        return this;
    }

    @Override
    protected String sqlType() {
        return "BIGINT UNSIGNED";
    }

    /**
     * Returns the CONSTRAINT SQL fragment, or null if no constraint is declared.
     */
    public String getConstraintSql() {
        if (!constrained)
            return null;
        return String.format(
                "CONSTRAINT FK_%s_%s FOREIGN KEY (%s) REFERENCES %s (%s) ON UPDATE %s ON DELETE %s",
                owningTable, name, name, referencedTable, referencedColumn, onUpdate, onDelete);
    }
}
