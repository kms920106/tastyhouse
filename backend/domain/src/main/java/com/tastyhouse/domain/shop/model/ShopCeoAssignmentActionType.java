package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게-점주 접근권한 조치 유형.
 *
 * <p>재배정(A → B)은 한 행에 before/after를 담지 않고 {@code REVOKE}(A) + {@code GRANT}(B) 2행으로
 * 남긴다 — 한 행에 담으면 "언제부터 언제까지 권한이 있었는가"를 읽을 수 없다.
 *
 * <p>{@code from(String)}이 있는 이유는 이 enum이 조회 필터로 HTTP 경계를 넘어오기 때문이다
 * (도메인 enum 경계 규칙).
 */
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
            throw new BusinessException(ErrorCode.SHOP_CEO_ASSIGNMENT_ACTION_UNKNOWN,
                ErrorCode.SHOP_CEO_ASSIGNMENT_ACTION_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
