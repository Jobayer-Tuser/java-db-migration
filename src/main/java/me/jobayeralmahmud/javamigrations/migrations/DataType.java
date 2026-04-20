package me.jobayeralmahmud.javamigrations.migrations;

public enum DataType {
    BIGINT, INT, TINYINT, VARCHAR, TEXT, BOOLEAN,
    DATE, DATETIME, TIMESTAMP, DECIMAL, DOUBLE, JSON, UUID, ENUM, BINARY;

    public static String BINARY(int number) {
        return String.format("BINARY(%d)", number);
    }

    public static String DECIMAL(int precision, int scale) {
        return String.format("DECIMAL(%d, %d)", precision, scale);
    }

    public static String NUMERIC(int precision, int scale) {
        return String.format("NUMERIC(%d, %d)", precision, scale);
    }

    public static String VARCHAR(int length) {
        return String.format("VARCHAR(%d)", length);
    }
}