package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 <b>유형</b>.
 *
 * <p>{@code CUP_DEPOSIT}은 환경부·자원순환보증금관리센터의 일회용컵 보증금제 대상 옵션그룹이다.
 * 이 유형의 옵션은 추가금이 아니라 <b>보증금</b>을 만들며, 그 금액은 최소주문금액·쿠폰·포인트 산정에서
 * 제외되고 반납 시 환급된다.
 *
 * <p><b>왜 {@code boolean isDeposit}이 아니라 enum인가</b>: 규제 유형은 하나로 끝나지 않는다(봉투
 * 보증금·다회용기 등). 배타적인 boolean이 하나씩 늘어나면 "둘 다 true인 상태"가 표현 가능해지고 그
 * 방어 코드가 계산 경로 전부에 퍼진다. enum 하나면 배타성이 타입으로 보장된다.
 *
 * <p><b>{@link ProductOptionType}(NORMAL/COMMON)과는 다른 축이다.</b> 그것은 "일반 옵션이냐 공통
 * 옵션이냐"를 나타내는 <b>요청 전용</b> enum으로 DB에 저장되지 않는다. 이름이 비슷해 혼동이 실제로
 * 생기므로, 여기에 {@code COMMON}을 섞거나 그쪽에 {@code DEPOSIT}을 섞지 않는다.
 */
public enum ProductOptionGroupType {

    NORMAL,
    CUP_DEPOSIT;

    /** 유형 미지정({@code null})은 일반 옵션그룹으로 본다 — 기존 데이터·기존 요청과 호환된다. */
    public static ProductOptionGroupType from(String code) {
        if (code == null || code.isBlank()) {
            return NORMAL;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_OPTION_GROUP_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public boolean isCupDeposit() {
        return this == CUP_DEPOSIT;
    }
}
