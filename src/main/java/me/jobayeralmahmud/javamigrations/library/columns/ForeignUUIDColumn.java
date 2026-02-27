package me.jobayeralmahmud.javamigrations.library.columns;

public class ForeignUUIDColumn extends Column<ForeignUUIDColumn> {

    private String referencedTable;
    private String referencedColumn = "id";
    private String onUpdate = "RESTRICT";
    private String onDelete = "RESTRICT";
    private boolean constrained = false;
    private String owningTable;

    public ForeignUUIDColumn(String name) {
        super(name);
    }

    /** Set the owning table (injected by Blueprint). */
    public void setTable(String tableName) {
        this.owningTable = tableName;
    }

    /**
     * Infer the referenced table from the column name, e.g. role_id → roles.
     */
    public ForeignUUIDColumn constrained() {
        this.constrained = true;
        String base = name.endsWith("_id") ? name.substring(0, name.length() - 3) : name;
        this.referencedTable = base + "s";
        return this;
    }

    /**
     * Explicitly name the referenced table.
     */
    public ForeignUUIDColumn constrained(String table) {
        this.constrained = true;
        this.referencedTable = table;
        return this;
    }

    public ForeignUUIDColumn references(String column) {
        this.referencedColumn = column;
        return this;
    }

    public ForeignUUIDColumn onUpdateCascade() {
        this.onUpdate = "CASCADE";
        return this;
    }

    public ForeignUUIDColumn onUpdateSetNull() {
        this.onUpdate = "SET NULL";
        return this;
    }

    public ForeignUUIDColumn onUpdateRestrict() {
        this.onUpdate = "RESTRICT";
        return this;
    }

    public ForeignUUIDColumn onDeleteCascade() {
        this.onDelete = "CASCADE";
        return this;
    }

    public ForeignUUIDColumn onDeleteSetNull() {
        this.onDelete = "SET NULL";
        return this;
    }

    public ForeignUUIDColumn onDeleteRestrict() {
        this.onDelete = "RESTRICT";
        return this;
    }

    @Override
    protected String sqlType() {
        return " BINARY(16)";
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
