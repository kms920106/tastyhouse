package com.tastyhouse.core.domain.ceo.domain.vo;

public record CeoId(Long value) {

    public CeoId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("CeoId는 양수여야 합니다: " + value);
        }
    }

    public static CeoId of(Long value) {
        return new CeoId(value);
    }
}
