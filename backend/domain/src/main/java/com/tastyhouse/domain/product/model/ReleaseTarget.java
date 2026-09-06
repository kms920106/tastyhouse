package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 품절·숨김 일괄 해제의 대상 구분.
 *
 * <p>DB에 저장되지 않는 요청 전용 enum이다 — 점주가 "무엇을 풀 것인가"를 고른 값이며 상품의 상태로
 * 남지 않으므로 {@code @Enumerated}·{@code columnDefinition} 규칙의 대상이 아니다.
 *
 * <p>HTTP 경계에서는 {@code String}으로 받고 api 서비스에서 {@link #from(String)}으로 승격한다
 * (도메인 enum 경계 규칙).
 */
public enum ReleaseTarget {

    /** 품절만 해제한다. 숨김 상태는 그대로 둔다. */
    SOLD_OUT,

    /** 숨김만 해제한다. 품절 상태는 그대로 둔다. */
    HIDDEN,

    /** 품절과 숨김을 함께 해제한다. 화면의 {@code 품절·숨김 해제}에 대응한다. */
    ALL;

    public static ReleaseTarget from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PRODUCT_RELEASE_TARGET_UNKNOWN,
                ErrorCode.PRODUCT_RELEASE_TARGET_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
