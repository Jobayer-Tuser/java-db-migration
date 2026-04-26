# Flyway Migration DSL

A fluent, Laravel-inspired DSL for writing Flyway Java-based migrations.  
Write expressive, readable migrations without raw SQL strings.

```java
@Component
public class S4__CreateProductImagesTable extends BaseMigration {
    @Override
    public void up(Schema schema) throws SQLException {
        schema.create("pos_product_images", table -> {
            table.uuid();
            table.foreignUuid("product_id").referencesTable("pos_products").onDeleteCascade();
            table.string("image_url", 500);
            table.bool("is_primary").defaultValue(false);
            table.integer("sort_order");
        });

    }

    @Override
    public void down(Schema schema) throws SQLException {
        schema.dropIfExists("pos_product_images");
    }
}
```

## Usage Guide

### 1. Extend `BaseMigration`

All migrations extend `BaseMigration` and implement `run(Schema schema)`:

```java
package database.migrations;

public class V1__CreateRolesTable extends BaseMigration {

    @Override
    protected void run(Schema schema) throws SQLException {
        schema.create("roles", table -> {
            table.id();
            table.string("name");
            table.timestamps();
        });
        log("Roles table created");
    }
}
```

---

### 2. Schema API

| Method | Description |
|---|---|
| `schema.create(table, blueprint -> {...})` | Create a new table |
| `schema.table(table, blueprint -> {...})` | Alter an existing table |
| `schema.dropIfExists(table)` | Drop table if it exists |

---

### 3. Blueprint — Column Types

| Method | SQL Type |
|---|---|
| `table.id()` | `BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY` |
| `table.string("col")` | `VARCHAR(255)` |
| `table.string("col", 100)` | `VARCHAR(100)` |
| `table.text("col")` | `TEXT` |
| `table.integer("col")` | `INT` |
| `table.bigInteger("col")` | `BIGINT` |
| `table.decimal("col", 10, 2)` | `DECIMAL(10, 2)` |
| `table.numeric("col", 10, 2)` | `NUMERIC(10, 2)` |
| `table.doubleColumn("col")` | `DOUBLE` |
| `table.date("col")` | `DATE` |
| `table.datetime("col")` | `DATETIME(6)` |
| `table.timeStamp("col")` | `TIMESTAMP` |
| `table.enumeration("col", "a", "b")` | `ENUM('a', 'b')` |
| `table.foreignId("col")` | `BIGINT UNSIGNED` |
| `table.timestamps()` | Adds `created_at` and `updated_at` |
| `table.softDeletes()` | Adds `deleted_at TIMESTAMP` |

---

### 4. Column Modifiers (chainable)

```java
table.string("email").unique().nullable();
table.integer("score").unsigned().defaultValue(0);
table.datetime("verified_at").nullable();
table.string("city").after("address");
```

| Modifier | Effect |
|---|---|
| `.nullable()` | Adds `DEFAULT NULL` |
| `.unique()` | Adds `UNIQUE` |
| `.defaultValue(val)` | Adds `DEFAULT val` |
| `.unsigned()` | Adds `UNSIGNED` (integers only) |
| `.after("col")` | Positions column after another (ALTER only) |

---

### 5. Foreign Key Constraints

```java
// Infers referenced table from column name: role_id → roles
table.foreignId("role_id").constrained().onUpdateCascade().onDeleteRestrict();

// Explicit referenced table (e.g. self-referential)
table.foreignId("parent_category_id").constrained("categories").onDeleteSetNull();
```

**Constraint rule options:** `.onUpdateCascade()`, `.onUpdateSetNull()`, `.onUpdateRestrict()`,  
`.onDeleteCascade()`, `.onDeleteSetNull()`, `.onDeleteRestrict()`

Constraint name follows the convention: `FK_{owningTable}_{column}`

---

### 6. Altering Tables

```java
schema.table("users", table -> {
    table.string("phone").nullable().after("email");
    table.dropColumn("display_name");
    table.dropForeign("role_id");       // Drops FK_users_role_id
});
```
## Project Structure

```
src/
└── main/
    ├── java/io/github/yourusername/flywaymigrations/
    │   ├── autoconfigure/
    │   │   ├── FlywayMigrationDslAutoConfiguration.java
    │   │   └── FlywayMigrationDslProperties.java
    │   └── library/
    │       ├── DataType.java
    │       ├── BaseMigration.java
    │       ├── Blueprint.java
    │       ├── ColumnDefination.java
    │       ├── ForeignColumnDefination.java
    │       ├── EnumDefinition.java
    │       ├── MigrationInitializer.java
    │       ├── MigrationRunner.java
    │       ├── Schema.java
    └── resources/META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```