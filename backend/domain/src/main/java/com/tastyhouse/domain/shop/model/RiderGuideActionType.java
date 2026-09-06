package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum RiderGuideActionType {
    UPDATE("등록·수정"),
    REVISION_REQUEST("수정 요청"),
    DELETION("삭제 조치");

    private final String description;

    RiderGuideActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static RiderGuideActionType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN,
                ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
