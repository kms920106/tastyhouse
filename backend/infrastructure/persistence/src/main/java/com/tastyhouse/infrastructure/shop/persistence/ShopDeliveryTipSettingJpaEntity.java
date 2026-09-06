package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_DELIVERY_TIP_SETTING")
public class ShopDeliveryTipSettingJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_tip_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryTipExtraType extraTipType;

    @Column(name = "base_distance_meters")
    private Integer baseDistanceMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "surcharge_unit", length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryTipDistanceUnit surchargeUnit;

    @Column(name = "surcharge_amount")
    private Integer surchargeAmount;

    protected ShopDeliveryTipSettingJpaEntity() {
    }

    private ShopDeliveryTipSettingJpaEntity(
        Long shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
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
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount
    ) {
        return new ShopDeliveryTipSettingJpaEntity(
            shopId, extraTipType, baseDistanceMeters, surchargeUnit, surchargeAmount
        );
    }

    void applyChanges(
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
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
}
