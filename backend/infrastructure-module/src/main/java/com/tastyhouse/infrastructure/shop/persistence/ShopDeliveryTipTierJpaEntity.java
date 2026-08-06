package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 기본(구간별) 배달팁 JPA 영속 모델.
 *
 * <p>구간 컬렉션은 replace-all로 통째 교체하므로 {@code applyChanges}가 없다 — 개별 행을 고치는 경로가
 * 존재하지 않는다(상세는 순수 도메인 모델 {@code ShopDeliveryTipTier} Javadoc).
 */
@Entity
@Table(name = "SHOP_DELIVERY_TIP_TIER")
public class ShopDeliveryTipTierJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "tier_order", nullable = false)
    private int tierOrder; // 구간 순서 (0=기본, 1~2=추가)

    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount; // 구간 하한 주문금액 (상품 할인 후 기준)

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount; // 배달팁 (0 이상 5,000 미만)

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
