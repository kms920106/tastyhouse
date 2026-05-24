package com.tastyhouse.core.domain.policy.domain.vo;

public record PolicyDocumentId(Long value) {

    public PolicyDocumentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid PolicyDocumentId: " + value);
        }
    }
}
