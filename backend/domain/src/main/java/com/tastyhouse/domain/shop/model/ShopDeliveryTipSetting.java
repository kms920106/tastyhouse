package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryTipSetting {
    private final Long id;
    private final ShopId shopId;
    private DeliveryTipExtraType extraTipType;
    private Integer baseDistanceMeters;
    private DeliveryTipDistanceUnit surchargeUnit;
    private Integer surchargeAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopDeliveryTipSetting(
        Long id,
        ShopId shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.extraTipType = extraTipType;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = surchargeUnit;
        this.surchargeAmount = surchargeAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopDeliveryTipSetting of(ShopId shopId) {
        return new ShopDeliveryTipSetting(null, shopId, DeliveryTipExtraType.NONE, null, null, null, null, null);
    }

    public static ShopDeliveryTipSetting reconstitute(
        Long id,
        ShopId shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopDeliveryTipSetting(
            id, shopId, extraTipType, baseDistanceMeters, surchargeUnit, surchargeAmount, createdAt, updatedAt
        );
    }

    public void changeToDistance(int baseDistanceMeters, DeliveryTipDistanceUnit unit, int surchargeAmount) {
        if (!DeliveryTipPolicy.BASE_DISTANCE_OPTIONS.contains(baseDistanceMeters)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_BASE_INVALID,
                ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_BASE_INVALID.getDefaultMessage() + " 입력: " + baseDistanceMeters + "m");
        }
        if (unit == null) {
            throw new BusinessException(ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN);
        }
        unit.validateAmount(surchargeAmount);

        this.extraTipType = DeliveryTipExtraType.DISTANCE;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = unit;
        this.surchargeAmount = surchargeAmount;
    }

    public void changeToRegion() {
        this.extraTipType = DeliveryTipExtraType.REGION;
        this.baseDistanceMeters = null;
        this.surchargeUnit = null;
        this.surchargeAmount = null;
    }

    public void clearExtraTip() {
        this.extraTipType = DeliveryTipExtraType.NONE;
        this.baseDistanceMeters = null;
        this.surchargeUnit = null;
        this.surchargeAmount = null;
    }

    public int calculateDistanceSurcharge(double meters) {
        if (!usesDistance() || baseDistanceMeters == null || surchargeUnit == null || surchargeAmount == null) {
            return 0;
        }

        double excessMeters = meters - baseDistanceMeters;
        if (excessMeters <= 0) {
            return 0;
        }

        int units = (int) Math.ceil(excessMeters / surchargeUnit.getUnitMeters());
        int surcharge = units * surchargeAmount;
        return Math.min(surcharge, DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND);
    }

    public boolean usesDistance() {
        return this.extraTipType == DeliveryTipExtraType.DISTANCE;
    }

    public boolean usesRegion() {
        return this.extraTipType == DeliveryTipExtraType.REGION;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public DeliveryTipExtraType getExtraTipType() {
        return this.extraTipType;
    }

    public Integer getBaseDistanceMeters() {
        return this.baseDistanceMeters;
    }

    public DeliveryTipDistanceUnit getSurchargeUnit() {
        return this.surchargeUnit;
    }

    public Integer getSurchargeAmount() {
        return this.surchargeAmount;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
