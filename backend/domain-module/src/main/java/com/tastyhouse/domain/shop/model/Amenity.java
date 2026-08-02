package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum Amenity {

    PARKING("주차"),
    RESTROOM("내부화장실"),
    RESERVATION("예약"),
    BABY_CHAIR("아기의자"),
    PET_FRIENDLY("애견동반"),
    OUTLET("개별 콘센트"),
    TAKEOUT("포장"),
    DELIVERY("배달");

    private final String displayName;

    Amenity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static Amenity from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AMENITY_UNKNOWN,
                ErrorCode.AMENITY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
