package com.tastyhouse.core.domain.payment.domain.vo;

import java.util.UUID;

public record PgOrderId(String value) {

    public PgOrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PgOrderId는 비어있을 수 없습니다");
        }
    }

    public static PgOrderId generate() {
        return new PgOrderId("PG" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
