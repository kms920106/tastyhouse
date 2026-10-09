package com.tastyhouse.infrastructure.jpa.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_ORDER_METHOD", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "order_method"})})
class ShopOrderMethodJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String orderMethod;

    protected ShopOrderMethodJpaEntity() {
    }

    private ShopOrderMethodJpaEntity(Long shopId, String orderMethod) {
        this.shopId = shopId;
        this.orderMethod = orderMethod;
    }

    static ShopOrderMethodJpaEntity create(Long shopId, String orderMethod) {
        return new ShopOrderMethodJpaEntity(shopId, orderMethod);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getOrderMethod() {
        return this.orderMethod;
    }
}
