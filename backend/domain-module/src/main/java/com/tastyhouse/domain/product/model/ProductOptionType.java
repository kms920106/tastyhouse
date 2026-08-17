package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션이 어느 갈래인지 구분한다.
 *
 * <p>일반 옵션({@code PRODUCT_OPTION})과 공통 옵션({@code PRODUCT_COMMON_OPTION})은 <b>다른 테이블·다른
 * id 시퀀스</b>라 id만으로는 대상을 특정할 수 없다. 일괄 처리 요청이 이 값을 함께 실어야 서버가 올바른
 * 리포지토리를 고를 수 있다.
 *
 * <p><b>이 enum이 필요한 이유</b>: 과거에는 api 모듈이 {@code !"COMMON".equals(type)}로 갈래를 나눴는데,
 * 그러면 {@code "COMMOM"} 같은 오타나 임의 문자열이 조용히 일반 옵션으로 분류돼 엉뚱한 테이블을 조회하고
 * 그 결과가 "대상 없음" 실패로만 드러났다. 문자열을 이 enum으로 승격시키면 알 수 없는 값이 그 자리에서
 * 400으로 거부된다.
 *
 * <p>DB에 저장되지 않는 요청 전용 enum이므로 {@code @Enumerated}·{@code columnDefinition} 규칙과 무관하다.
 */
public enum ProductOptionType {

    /** 일반 옵션 — 특정 메뉴에 직접 딸린 옵션. */
    NORMAL,

    /** 공통 옵션 — 여러 메뉴가 함께 쓰는 옵션. */
    COMMON;

    public static ProductOptionType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_OPTION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
