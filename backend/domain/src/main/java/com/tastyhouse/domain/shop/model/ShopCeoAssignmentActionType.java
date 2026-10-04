package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ShopCeoAssignmentActionType {

    GRANT("권한 부여"),
    REVOKE("권한 말소");

    private final String description;

    ShopCeoAssignmentActionType(String description) {
        this.description = description;
    }

    public static ShopCeoAssignmentActionType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_CEO_ASSIGNMENT_ACTION_UNKNOWN,
                DomainErrorCode.SHOP_CEO_ASSIGNMENT_ACTION_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
