package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_DELIVERY_TIP_TIER")
public class ShopDeliveryTipTierJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "tier_order", nullable = false)
    private int tierOrder;

    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount;

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount;

    protected ShopDeliveryTipTierJpaEntity() {
    }

    private ShopDeliveryTipTierJpaEntity(Long shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        this.shopId = shopId;
        this.tierOrder = tierOrder;
        this.minOrderAmount = minOrderAmount;
        this.tipAmount = tipAmount;
    }

    static ShopDeliveryTipTierJpaEntity create(Long shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        return new ShopDeliveryTipTierJpaEntity(shopId, tierOrder, minOrderAmount, tipAmount);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public int getTierOrder() {
        return this.tierOrder;
    }

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
