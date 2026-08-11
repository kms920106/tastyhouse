package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 변경이력 대분류.
 *
 * <p>점주 화면의 필터 드롭다운 1단계에 대응한다. 한글 라벨을 도메인이 보유하는 이유는 서버가 카탈로그
 * API로 라벨을 내려주어 프론트에 29개 중분류 라벨 상수를 복제하지 않기 위함이다.
 */
public enum ShopChangeCategory {

    OPERATION("운영 정보"),
    DELIVERY("배달 정보"),
    SHOP_INFO("가게 정보"),
    IMAGE("이미지·상표"),
    RIDER("라이더 안내");

    private final String description;

    ShopChangeCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static ShopChangeCategory from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_CHANGE_CATEGORY_UNKNOWN,
                ErrorCode.SHOP_CHANGE_CATEGORY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
