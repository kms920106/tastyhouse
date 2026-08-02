package com.tastyhouse.domain.faq.vo;

public record FaqCategoryId(Long value) {

    public FaqCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FaqCategoryId는 양수여야 합니다: " + value);
        }
    }

    public static FaqCategoryId of(Long value) {
        return new FaqCategoryId(value);
    }
}
