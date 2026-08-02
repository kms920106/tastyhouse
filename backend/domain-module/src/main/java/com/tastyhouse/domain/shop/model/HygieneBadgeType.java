package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum HygieneBadgeType {

    FOOD_SAFETY_CERTIFIED("식품안심업소"),
    CESCO_BLUE("블루 세스코"),
    CESCO_WHITE("화이트 세스코");

    private final String description;

    HygieneBadgeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static HygieneBadgeType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.HYGIENE_BADGE_TYPE_UNKNOWN,
                ErrorCode.HYGIENE_BADGE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
