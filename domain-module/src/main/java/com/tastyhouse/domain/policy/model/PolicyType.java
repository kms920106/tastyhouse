package com.tastyhouse.domain.policy.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum PolicyType {

    TERMS_OF_SERVICE("이용약관"),
    PRIVACY_POLICY("개인정보처리방침"),
    ELECTRONIC_FINANCIAL_TRANSACTIONS("전자금융거래"),
    AGE_VERIFICATION("만 14세 이상");

    private final String description;

    PolicyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static PolicyType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.POLICY_TYPE_UNKNOWN,
                ErrorCode.POLICY_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
