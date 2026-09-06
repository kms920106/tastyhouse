package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 정보에 대한 고객 의견의 유형.
 *
 * <p><b>왜 자유 서술만 받지 않고 유형을 고르게 하는가</b>: 서술만 받으면 점주가 무엇을 고쳐야 할지
 * 분류할 수 없고, "지난 한 주 동안 같은 유형으로 몇 건"이라는 주간 집계 자체가 불가능하다.
 * 집계 가능성이 이 열거의 존재 이유이므로 유형은 필수이고 {@link #ETC}만 서술을 요구한다.
 */
public enum ProductFeedbackType {

    /** 가격이 달라요 — 등록된 가격과 실제 판매가가 다르다. */
    PRICE,
    /** 이미지가 달라요 — 사진과 실물이 다르다. */
    IMAGE,
    /** 구성이 달라요 — 메뉴구성 설명과 실제 구성이 다르다. */
    COMPOSITION,
    /** 품절인데 판매 중으로 표시돼요. */
    SOLD_OUT,
    /** 기타 — 위 분류에 없는 제보. 이 유형만 서술 내용이 필수다. */
    ETC;

    /**
     * HTTP 경계에서 받은 문자열을 유형으로 승격한다. 알 수 없는 값은
     * {@code PRODUCT_FEEDBACK_TYPE_UNKNOWN}(400)으로 거절한다 — {@code IllegalArgumentException}이
     * 그대로 새어 나가면 500이 되어 클라이언트가 입력 오류임을 구분할 수 없다.
     */
    public static ProductFeedbackType from(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }
    }

    /** 서술 내용이 필수인 유형인지. 현재는 {@link #ETC}만 해당한다. */
    public boolean requiresContent() {
        return this == ETC;
    }
}
