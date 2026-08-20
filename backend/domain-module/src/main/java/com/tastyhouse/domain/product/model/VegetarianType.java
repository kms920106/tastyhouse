package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 채식 단계.
 *
 * <p>점주가 요청하고 <b>관리자 승인 시에만</b> {@code PRODUCT.vegetarian_type}에 반영된다
 * ({@code Product#applyVegetarianType}). {@code null}이면 채식 메뉴가 아니다.
 *
 * <p><b>상수 이름 자체가 DB 저장값이다</b>({@code EnumType.STRING}) — 이름을 바꾸지 않는다.
 */
public enum VegetarianType {

    /** 비건 — 동물성 재료를 일절 쓰지 않는다. */
    VEGAN("비건"),

    /** 락토 — 우유·유제품까지 허용한다. */
    LACTO("락토"),

    /** 오보 — 달걀까지 허용한다. */
    OVO("오보"),

    /** 락토오보 — 우유·유제품과 달걀까지 허용한다. */
    LACTO_OVO("락토오보"),

    /** 페스코 — 락토오보에 더해 해산물까지 허용한다. */
    PESCO("페스코");

    private final String description;

    VegetarianType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static VegetarianType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_VEGETARIAN_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
