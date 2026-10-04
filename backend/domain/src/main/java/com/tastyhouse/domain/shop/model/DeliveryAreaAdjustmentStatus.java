package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN,
                DomainErrorCode.DELIVERY_AREA_ADJUSTMENT_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
