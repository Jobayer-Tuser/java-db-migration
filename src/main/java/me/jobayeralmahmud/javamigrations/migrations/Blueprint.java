package me.jobayeralmahmud.javamigrations.migrations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Blueprint {

    private final String tableName;
    private final List<Object> columns             = new ArrayList<>();
    private final List<String> columnsToDrop       = new ArrayList<>();
    private final List<String> foreignKeysToDrop   = new ArrayList<>();
    private final List<String> multiColumnUniques  = new ArrayList<>();

    public Blueprint(String tableName) {
        this.tableName = tableName;
    }

    public void id() {
        addColumn("id", DataType.BIGINT).unsigned().autoIncrement().primaryKey();
    }

    public void uuid() {
        addColumn("id", DataType.BINARY(16)).primaryKey();
    }

    public ColumnDefinition uuid(String name) {
        return addColumn(name, DataType.BINARY(16));
    }

    public ColumnDefinition tinyInteger(String name) {
        return addColumn(name, DataType.TINYINT);
    }

    public ColumnDefinition integer(String name) {
        return addColumn(name, DataType.INT);
    }

    public ColumnDefinition bigInteger(String name) {
        return addColumn(name, DataType.BIGINT);
    }

    public ColumnDefinition decimal(String name, int precision, int scale) {
        return addColumn(name, DataType.DECIMAL(precision, scale));
    }

    public ColumnDefinition decimal(String name) {
        return decimal(name, 12, 2);
    }

    public ColumnDefinition numeric(String name, int precision, int scale) {
        return addColumn(name, DataType.NUMERIC(precision, scale));
    }

    public ColumnDefinition numeric(String name) {
        return numeric(name, 10, 0);
    }

    public ColumnDefinition double_(String name) {
        return addColumn(name, DataType.DOUBLE);
    }

    public ColumnDefinition string(String name, int length) {
        return addColumn(name, DataType.VARCHAR(length));
    }

    public ColumnDefinition string(String name) {
        return string(name, 255);
    }

    public ColumnDefinition text(String name) {
        return addColumn(name, DataType.TEXT);
    }

    public ColumnDefinition json(String name) {
        return addColumn(name, DataType.JSON);
    }

    public ColumnDefinition date(String name) {
        return addColumn(name, DataType.DATE);
    }

    public ColumnDefinition dateTime(String name) {
        return addColumn(name, DataType.DATETIME);
    }

    public ColumnDefinition timestamp(String name) {
        return addColumn(name, DataType.TIMESTAMP);
    }

    public ColumnDefinition bool(String name) {
        return addColumn(name, DataType.BOOLEAN);
    }

    /**
     * Creates a UUID foreign key column (BINARY(16)).
     */
    public ForeignKeyDefinition foreignUuid(String name) {
        var col = new ForeignKeyDefinition(name, ForeignKeyDefinition.KeyType.UUID);
        col.owningTable(this.tableName);
        columns.add(col);
        return col;
    }

    public ForeignKeyDefinition foreignId(String name) {
        var col = new ForeignKeyDefinition(name, ForeignKeyDefinition.KeyType.INTEGER);
        col.owningTable(this.tableName);
        columns.add(col);
        return col;
    }

    public EnumDefinition enumeration(String name, String... options) {
        var col = new EnumDefinition(name, options);
        columns.add(col);
        return col;
    }

    public EnumDefinition enumeration(String name, Enum<?>... values) {
        var col = new EnumDefinition(name, values);
        columns.add(col);
        return col;
    }

    public void timestamps() {
        addColumn("created_at", DataType.TIMESTAMP).nullable().defaultCurrentTimestamp();
        addColumn("updated_at", DataType.TIMESTAMP).nullable().defaultCurrentTimestamp().onUpdateCurrentTimestamp();
    }

    public void softDelete() {
        addColumn("deleted_at", DataType.TIMESTAMP).nullable();
    }

    public void unique(String... columnNames) {
        multiColumnUniques.add("UNIQUE (" + String.join(", ", columnNames) + ")");
    }

    public void dropColumn(String name) {
        columnsToDrop.add(name);
    }

    public void dropForeign(String columnName) {
        foreignKeysToDrop.add(String.format("fk_%s_%s", tableName, columnName));
    }

    public String getSql(String tableName) {
        Stream<String> definitions = columns.stream().map(this::getDefinition);
        Stream<String> constraints = columns.stream()
                .map(c -> {
                    if (c instanceof ForeignKeyDefinition f) return f.getConstraintSql();
                    return null;
                })
                .filter(Objects::nonNull);

        String finalQuery = Stream.concat(
                Stream.concat(definitions, constraints),
                multiColumnUniques.stream()
        ).collect(Collectors.joining(", "));

        return String.format("CREATE TABLE %s (%s)", tableName, finalQuery);
    }


    public List<String> getAlterationSql(String tableName) {
        List<String> statements = new ArrayList<>();

        foreignKeysToDrop.stream()
                .map(fk -> String.format("ALTER TABLE %s DROP FOREIGN KEY %s", tableName, fk))
                .forEach(statements::add);

        columnsToDrop.stream()
                .map(col -> String.format("ALTER TABLE %s DROP COLUMN %s", tableName, col))
                .forEach(statements::add);

        columns.forEach(column -> {
            String afterClause = getAfterClause(column);
            statements.add(String.format("ALTER TABLE %s ADD %s%s",
                    tableName, getDefinition(column), afterClause));

            if (column instanceof ForeignKeyDefinition f) {
                String constraint = f.getConstraintSql();
                if (constraint != null) {
                    statements.add(String.format("ALTER TABLE %s ADD %s", tableName, constraint));
                }
            }
        });

        return statements;
    }

    private ColumnDefinition addColumn(String name, DataType sqlType) {
        var col = new ColumnDefinition(name, sqlType);
        columns.add(col);
        return col;
    }

    private ColumnDefinition addColumn(String name, String sqlType) {
        var col = new ColumnDefinition(name, sqlType);
        columns.add(col);
        return col;
    }

    private String getDefinition(Object column) {
        return switch (column) {
            case String                 s -> s;
            case ColumnDefinition       c -> c.getSqlDefinition();
            case EnumDefinition         e -> e.getSqlDefinition();
            case ForeignKeyDefinition   f -> f.getSqlDefinition();
            default -> throw new IllegalStateException("Unknown column type: " + column.getClass());
        };
    }

    private String getAfterClause(Object column) {
       String after = switch (column) {
            case ColumnDefinition       c -> c.afterColumn();
            case EnumDefinition         e -> e.afterColumn() ;
            case ForeignKeyDefinition   f -> f.afterColumn();
            default -> null;
        };

       return after != null ? " AFTER " + after : "";
    }
}