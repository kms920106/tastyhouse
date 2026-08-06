package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 공휴일 추가 배달팁 JPA 영속 모델 (가게당 1건).
 *
 * <p>금액 변경 경로가 있으므로 {@code applyChanges}를 둔다(load-copy-save).
 */
@Entity
@Table(name = "SHOP_DELIVERY_TIP_HOLIDAY")
public class ShopDeliveryTipHolidayJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount; // 추가 배달팁 (0~10,000)

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
