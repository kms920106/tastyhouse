package com.tastyhouse.domain.shared.model;

public enum ApprovalStatus {
    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("반려"),
    CANCELED("취소");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
