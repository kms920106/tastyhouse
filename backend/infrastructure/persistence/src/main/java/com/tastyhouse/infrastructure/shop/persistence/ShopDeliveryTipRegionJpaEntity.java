package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_DELIVERY_TIP_REGION")
public class ShopDeliveryTipRegionJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "admin_dong_id", nullable = false)
    private Long adminDongId;

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount;

    protected ShopDeliveryTipRegionJpaEntity() {
    }

    private ShopDeliveryTipRegionJpaEntity(Long shopId, Long adminDongId, int tipAmount) {
        this.shopId = shopId;
        this.adminDongId = adminDongId;
        this.tipAmount = tipAmount;
    }

    static ShopDeliveryTipRegionJpaEntity create(Long shopId, Long adminDongId, int tipAmount) {
        return new ShopDeliveryTipRegionJpaEntity(shopId, adminDongId, tipAmount);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getAdminDongId() {
        return this.adminDongId;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
