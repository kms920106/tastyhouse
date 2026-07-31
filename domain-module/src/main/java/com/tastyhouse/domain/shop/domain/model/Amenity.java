package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
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

    public static Amenity from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AMENITY_UNKNOWN,
                ErrorCode.AMENITY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
