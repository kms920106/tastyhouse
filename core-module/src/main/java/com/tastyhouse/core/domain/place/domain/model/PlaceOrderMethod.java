package com.tastyhouse.core.domain.place.domain.model;

import com.tastyhouse.core.entity.BaseEntity;
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
@Table(name = "PLACE_ORDER_METHOD", uniqueConstraints = {@UniqueConstraint(columnNames = {"place_id", "order_method"})})
public class PlaceOrderMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private OrderMethod orderMethod; // 주문 방식 (COUNTER, KIOSK, TABLE_ORDER, APP 등)

    private PlaceOrderMethod(
        Long placeId,
        OrderMethod orderMethod
    ) {
        this.placeId = placeId;
        this.orderMethod = orderMethod;
    }

    public static PlaceOrderMethod of(
        Long placeId,
        OrderMethod orderMethod
    ) {
        return new PlaceOrderMethod(
            placeId,
            orderMethod
        );
    }
}
