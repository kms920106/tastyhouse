package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum AllergenType {
    MILK("우유"),
    EGG("난류"),
    BUCKWHEAT("메밀"),
    PEANUT("땅콩"),
    SOYBEAN("대두"),
    WHEAT("밀"),
    WALNUT("호두"),
    PEACH("복숭아"),
    TOMATO("토마토"),
    MACKEREL("고등어"),
    CRAB("게"),
    SHRIMP("새우"),
    SQUID("오징어"),
    OYSTER("굴"),
    ABALONE("전복"),
    MUSSEL("홍합"),
    SHELLFISH("조개류"),
    PORK("돼지고기"),
    CHICKEN("닭고기"),
    BEEF("쇠고기"),
    SULFITE("아황산류");

    private final String description;

    AllergenType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static AllergenType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_ALLERGEN_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_ALLERGEN_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
