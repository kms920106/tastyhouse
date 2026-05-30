package com.tastyhouse.core.domain.shop.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_ORDER_METHOD", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "order_method"})})
public class ShopOrderMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private OrderMethod orderMethod; // 주문 방식 (COUNTER, KIOSK, TABLE_ORDER, APP 등)

    private ShopOrderMethod(
        Long shopId,
        OrderMethod orderMethod
    ) {
        this.shopId = shopId;
        this.orderMethod = orderMethod;
    }

    public static ShopOrderMethod of(
        Long shopId,
        OrderMethod orderMethod
    ) {
        return new ShopOrderMethod(
            shopId,
            orderMethod
        );
    }
}
