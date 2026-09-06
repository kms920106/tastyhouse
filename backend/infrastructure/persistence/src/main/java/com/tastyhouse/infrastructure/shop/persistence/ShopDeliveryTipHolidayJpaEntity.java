package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_DELIVERY_TIP_HOLIDAY")
public class ShopDeliveryTipHolidayJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount;

    protected ShopDeliveryTipHolidayJpaEntity() {
    }

    private ShopDeliveryTipHolidayJpaEntity(Long shopId, int tipAmount) {
        this.shopId = shopId;
        this.tipAmount = tipAmount;
    }

    static ShopDeliveryTipHolidayJpaEntity create(Long shopId, int tipAmount) {
        return new ShopDeliveryTipHolidayJpaEntity(shopId, tipAmount);
    }

    void applyChanges(int tipAmount) {
        this.tipAmount = tipAmount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
