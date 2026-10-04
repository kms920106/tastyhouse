package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum RiderGuideActorType {

    CEO("점주"),
    ADMIN("관리자");

    private final String description;

    RiderGuideActorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static RiderGuideActorType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN,
                "알 수 없는 라이더 안내 변경 주체입니다: " + code);
        }
    }
}
