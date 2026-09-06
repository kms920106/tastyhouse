package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum DeliveryAreaAdjustmentStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    CANCELED;

    public static DeliveryAreaAdjustmentStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN,
                ErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
