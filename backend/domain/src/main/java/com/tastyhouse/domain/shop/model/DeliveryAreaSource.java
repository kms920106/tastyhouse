package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum DeliveryAreaSource {

    MANUAL,

    POLYGON;

    public static DeliveryAreaSource from(String value) {
        if (value == null) {
            throw new DomainException(DomainErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }
    }
}
