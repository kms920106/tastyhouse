package com.tastyhouse.domain.policy.domain.vo;

public record PolicyDocumentId(Long value) {

    public PolicyDocumentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("PolicyDocumentId는 양수여야 합니다: " + value);
        }
    }

    public static PolicyDocumentId of(Long value) {
        return new PolicyDocumentId(value);
    }
}
