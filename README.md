# Flyway Migration DSL

A fluent, Laravel-inspired DSL for writing Flyway Java-based migrations.  
Write expressive, readable migrations without raw SQL strings.

```java
public class V2__CreateUsersTable extends BaseMigration {
    @Override
    protected void run(Schema schema) throws SQLException {
        schema.create("users", table -> {
            table.id();
            table.foreignId("role_id").constrained().onUpdateCascade().onDeleteRestrict();
            table.string("name");
            table.string("email").unique();
            table.string("password");
            table.datetime("email_verified_at").nullable();
            table.timestamps();
        });
    }
}
```

---

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.yourusername</groupId>
    <artifactId>flyway-migration-dsl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.yourusername:flyway-migration-dsl:1.0.0'
```

> **Note:** Flyway itself (`flyway-core`) is a `provided` dependency — your application must include it.

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.x |
| Flyway | 10.x |

---

## Spring Boot Auto-Configuration

The library registers itself automatically via Spring Boot's auto-configuration mechanism.  
No `@Import` or `@Bean` declarations are needed.

**Optional property** (in `application.properties`):
```properties
flyway-dsl.log-sql=true   # Print generated SQL during migrations (default: false)
```

---

## Usage Guide

### 1. Extend `BaseMigration`

All migrations extend `BaseMigration` and implement `run(Schema schema)`:

```java
package database.migrations;

import io.github.yourusername.flywaymigrations.library.BaseMigration;
import io.github.yourusername.flywaymigrations.library.Schema;

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

---

## Publishing to Maven Central

### Prerequisites
1. [Create a Sonatype Central account](https://central.sonatype.com/)
2. Verify ownership of your `groupId` namespace (e.g. `io.github.yourusername`)
3. Set up a GPG key and upload it to a public key server

### One-time Maven settings (`~/.m2/settings.xml`)
```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_SONATYPE_TOKEN_USERNAME</username>
      <password>YOUR_SONATYPE_TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

### Release commands
```bash
# Build and sign without publishing
mvn clean verify -Dgpg.passphrase=YOUR_PASSPHRASE

# Deploy to Central (review in portal before release)
mvn clean deploy -Dgpg.passphrase=YOUR_PASSPHRASE

# Skip signing locally during development
mvn clean install -Dgpg.skip=true
```

After deploying, log in to [central.sonatype.com](https://central.sonatype.com) and click **Publish** on the pending deployment.

---

## Project Structure

```
src/
└── main/
    ├── java/io/github/yourusername/flywaymigrations/
    │   ├── autoconfigure/
    │   │   ├── FlywayMigrationDslAutoConfiguration.java
    │   │   └── FlywayMigrationDslProperties.java
    │   └── library/
    │       ├── BaseMigration.java
    │       ├── Blueprint.java
    │       ├── Schema.java
    │       └── columns/
    │           ├── Column.java              (abstract base)
    │           ├── IntegerLikeColumn.java   (abstract for int types)
    │           ├── IntegerColumn.java
    │           ├── BigIntegerColumn.java
    │           ├── TinyIntColumn.java
    │           ├── DecimalColumn.java
    │           ├── NumericColumn.java
    │           ├── DoubleColumn.java
    │           ├── StringColumn.java
    │           ├── TextColumn.java
    │           ├── DateColumn.java
    │           ├── DateTimeColumn.java
    │           ├── TimeStampColumn.java
    │           ├── EnumColumn.java
    │           └── ForeignIdColumn.java
    └── resources/META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## License

MIT
