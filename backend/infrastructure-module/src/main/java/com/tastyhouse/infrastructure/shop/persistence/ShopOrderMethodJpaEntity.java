package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점-주문방식 배정 JPA 영속 모델. 순수 도메인 모델 {@code ShopOrderMethod}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_ORDER_METHOD", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "order_method"})})
public class ShopOrderMethodJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private OrderMethod orderMethod; // 주문 방식

    protected ShopOrderMethodJpaEntity() {
    }

    private ShopOrderMethodJpaEntity(Long shopId, OrderMethod orderMethod) {
        this.shopId = shopId;
        this.orderMethod = orderMethod;
    }

    static ShopOrderMethodJpaEntity create(Long shopId, OrderMethod orderMethod) {
        return new ShopOrderMethodJpaEntity(shopId, orderMethod);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }
}
