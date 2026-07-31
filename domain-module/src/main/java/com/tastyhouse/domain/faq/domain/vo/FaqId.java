package com.tastyhouse.domain.faq.domain.vo;

public record FaqId(Long value) {

    public FaqId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FaqId는 양수여야 합니다: " + value);
        }
    }

    public static FaqId of(Long value) {
        return new FaqId(value);
    }
}
