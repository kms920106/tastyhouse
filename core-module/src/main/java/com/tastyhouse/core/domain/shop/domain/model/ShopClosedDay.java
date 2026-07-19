package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_CLOSED_DAY")
public class ShopClosedDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_day_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private ClosedDayType closedDayType; // 정기 휴무 유형 (FIRST_MON, SECOND_SUN, EVERY_TUE 등)

    private ShopClosedDay(Long shopId, ClosedDayType closedDayType) {
        this.shopId = shopId;
        this.closedDayType = closedDayType;
    }

    public static ShopClosedDay of(Long shopId, ClosedDayType closedDayType) {
        return new ShopClosedDay(shopId, closedDayType);
    }
}
