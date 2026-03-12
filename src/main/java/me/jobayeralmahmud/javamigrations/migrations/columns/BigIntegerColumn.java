package me.jobayeralmahmud.javamigrations.migrations.columns;

public class BigIntegerColumn extends IntegerLikeColumn<BigIntegerColumn> {
    public BigIntegerColumn(String name) {
        super(name);
    }

    @Override
    protected String sqlType() {
        return "BIGINT" + unsignedSuffix();
    }
}