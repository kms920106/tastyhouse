package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum DeliveryTipDistanceUnit {
    PER_100M(100, 100, 300),

    PER_500M(500, 100, 1500);

    private final int unitMeters;
    private final int minAmount;
    private final int maxAmount;

    DeliveryTipDistanceUnit(int unitMeters, int minAmount, int maxAmount) {
        this.unitMeters = unitMeters;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public int getUnitMeters() {
        return this.unitMeters;
    }

    public void validateAmount(int amount) {
        if (amount < minAmount || amount > maxAmount) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE,
                ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE.getDefaultMessage()
                    + " " + name() + " 허용 범위: " + minAmount + "~" + maxAmount + "원, 입력: " + amount + "원");
        }
    }

    public static DeliveryTipDistanceUnit from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN,
                ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
