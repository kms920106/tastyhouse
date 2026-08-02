package com.tastyhouse.domain.shop.vo;

public record TagId(Long value) {

    public TagId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("TagId는 양수여야 합니다: " + value);
        }
    }

    public static TagId of(Long value) {
        return new TagId(value);
    }
}
