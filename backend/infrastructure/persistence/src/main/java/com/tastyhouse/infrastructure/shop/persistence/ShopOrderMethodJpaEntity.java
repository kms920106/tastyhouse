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

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_ORDER_METHOD", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "order_method"})})
public class ShopOrderMethodJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private OrderMethod orderMethod;

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
