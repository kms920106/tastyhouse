package com.tastyhouse.infrastructure.persistence.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_DELIVERY_TIP_SETTING")
class ShopDeliveryTipSettingJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "extra_tip_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String extraTipType;

    @Column(name = "base_distance_meters")
    private Integer baseDistanceMeters;

    @Column(name = "surcharge_unit", length = 20, columnDefinition = "VARCHAR(20)")
    private String surchargeUnit;

    @Column(name = "surcharge_amount")
    private Integer surchargeAmount;

    protected ShopDeliveryTipSettingJpaEntity() {
    }

    private ShopDeliveryTipSettingJpaEntity(
        Long shopId,
        String extraTipType,
        Integer baseDistanceMeters,
        String surchargeUnit,
        Integer surchargeAmount
    ) {
        this.shopId = shopId;
        this.extraTipType = extraTipType;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = surchargeUnit;
        this.surchargeAmount = surchargeAmount;
    }

    static ShopDeliveryTipSettingJpaEntity create(
        Long shopId,
        String extraTipType,
        Integer baseDistanceMeters,
        String surchargeUnit,
        Integer surchargeAmount
    ) {
        return new ShopDeliveryTipSettingJpaEntity(
            shopId, extraTipType, baseDistanceMeters, surchargeUnit, surchargeAmount
        );
    }

    void applyChanges(
        String extraTipType,
        Integer baseDistanceMeters,
        String surchargeUnit,
        Integer surchargeAmount
    ) {
        this.extraTipType = extraTipType;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = surchargeUnit;
        this.surchargeAmount = surchargeAmount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getExtraTipType() {
        return this.extraTipType;
    }

    public Integer getBaseDistanceMeters() {
        return this.baseDistanceMeters;
    }

    public String getSurchargeUnit() {
        return this.surchargeUnit;
    }

    public Integer getSurchargeAmount() {
        return this.surchargeAmount;
    }
}
