package com.tastyhouse.domain.ceo.model;

public enum CeoLoginFailureReason {
    BAD_CREDENTIALS("비밀번호 불일치"),
    ACCOUNT_INACTIVE("비활성 계정");

    private final String description;

    CeoLoginFailureReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
