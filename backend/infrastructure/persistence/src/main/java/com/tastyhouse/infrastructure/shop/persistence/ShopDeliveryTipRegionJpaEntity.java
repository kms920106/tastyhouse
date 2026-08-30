package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 지역별 추가 배달팁 JPA 영속 모델.
 *
 * <p>지역별 컬렉션도 replace-all 교체라 {@code applyChanges}가 없다.
 */
@Entity
@Table(name = "SHOP_DELIVERY_TIP_REGION")
public class ShopDeliveryTipRegionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "admin_dong_id", nullable = false)
    private Long adminDongId; // 행정동 ID (ADMIN_DONG.id 참조)

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount; // 추가 배달팁 (0~10,000)

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
