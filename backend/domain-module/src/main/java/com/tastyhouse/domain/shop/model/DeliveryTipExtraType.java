package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 추가 배달팁 방식.
 *
 * <p>거리별({@link #DISTANCE})과 지역별({@link #REGION})은 <b>상호 배타</b>이며, 그 불변식의 물리적
 * 단일 소유자가 {@code SHOP_DELIVERY_TIP_SETTING.extra_tip_type}(가게당 1행)이다 — 배타성이 어느 행에도
 * 소유자 없이 서비스 코드에만 떠 있으면 동시 요청에 뚫린다.
 *
 * <p><b>상수 이름 자체가 DB 저장값이다</b>({@code EnumType.STRING}) — 이름을 바꾸지 않는다.
 */
public enum DeliveryTipExtraType {

    /** 추가 배달팁 미사용 (구간별 기본 배달팁만 부과). */
    NONE("미사용"),

    /** 거리별 추가 배달팁 (기본배달거리 초과분에 단위당 할증). */
    DISTANCE("거리별"),

    /** 지역별 추가 배달팁 (배달가능지역으로 설정된 행정동마다 금액). */
    REGION("지역별");

    private final String description;

    DeliveryTipExtraType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static DeliveryTipExtraType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DELIVERY_TIP_EXTRA_TYPE_UNKNOWN,
                ErrorCode.DELIVERY_TIP_EXTRA_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
