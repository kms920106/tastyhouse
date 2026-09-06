package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum DeliveryAreaSource {
    MANUAL,

    POLYGON;

    public static DeliveryAreaSource from(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }
    }
}
